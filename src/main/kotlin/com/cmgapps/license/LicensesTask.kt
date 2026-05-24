/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.helper.uppercaseFirstChar
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.reporter.CsvReport
import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.LicensesSingleFileReport
import com.cmgapps.license.reporter.MarkdownReport
import com.cmgapps.license.reporter.ReportType
import com.cmgapps.license.reporter.TextReport
import com.cmgapps.license.reporter.XmlReport
import groovy.lang.Closure
import org.apache.maven.artifact.versioning.ComparableVersion
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URI
import java.util.function.IntFunction
import javax.inject.Inject

interface LicenseReportContainer : ReportContainer<LicensesSingleFileReport> {
    @get:Internal
    val plainText: TextReport

    @get:Internal
    val json: JsonReport

    @get:Internal
    val html: HtmlReport

    @get:Internal
    val xml: XmlReport

    @get:Internal
    val markdown: MarkdownReport

    @get:Internal
    val csv: CsvReport

    @get:Internal
    val custom: CustomReport
}

internal abstract class LicenseReportContainerImpl
    @Inject
    constructor(
        private val objects: ObjectFactory,
        layout: ProjectLayout,
        task: Task,
    ) : NamedDomainObjectSet<LicensesSingleFileReport> by objects.namedDomainObjectSet(LicensesSingleFileReport::class.java),
        LicenseReportContainer {
        init {
            addReporter(TextReport::class.java, layout, task)
            addReporter(JsonReport::class.java, layout, task)
            addReporter(HtmlReport::class.java, layout, task, task.logger, objects)
            addReporter(XmlReport::class.java, layout, task)
            addReporter(MarkdownReport::class.java, layout, task, task.logger)
            addReporter(CsvReport::class.java, layout, task)
            addReporter(CustomReport::class.java, layout, task, objects)
        }

        override fun getEnabled(): NamedDomainObjectSet<LicensesSingleFileReport> = enabledReports

        @Override
        override fun getEnabledReports(): MutableMap<String, LicensesSingleFileReport> = enabledReports.asMap

        private val enabledReports: NamedDomainObjectSet<LicensesSingleFileReport>
            get() = matching { element -> element.required.get() }

        override fun configure(cl: Closure<*>?): ReportContainer<LicensesSingleFileReport> {
            cl?.call(this)
            return this
        }

        private fun addReporter(
            clazz: Class<out LicensesSingleFileReport>,
            vararg params: Any,
        ) {
            add(objects.newInstance(clazz, *params))
        }

        override val plainText: TextReport
            get() = getByName(ReportType.TEXT.name) as TextReport

        override val json: JsonReport
            get() = getByName(ReportType.JSON.name) as JsonReport

        override val html: HtmlReport
            get() = getByName(ReportType.HTML.name) as HtmlReport

        override val xml: XmlReport
            get() = getByName(ReportType.XML.name) as XmlReport

        override val csv: CsvReport
            get() = getByName(ReportType.CSV.name) as CsvReport

        override val markdown: MarkdownReport
            get() = getByName(ReportType.MARKDOWN.name) as MarkdownReport

        override val custom: CustomReport
            get() = getByName(ReportType.CUSTOM.name) as CustomReport

        @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
        override fun <T> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?>? = super<LicenseReportContainer>.toArray(generator)

        @Suppress("UnstableApiUsage")
        override fun disallowChanges() {
            super<LicenseReportContainer>.disallowChanges()
        }
    }

@CacheableTask
abstract class LicensesTask
    @Inject
    constructor(
        objects: ObjectFactory,
    ) : DefaultTask(),
        Reporting<LicenseReportContainer> {
        private val reports: LicenseReportContainer =
            objects.newInstance(LicenseReportContainerImpl::class.java, this)

        @get:Input
        var additionalProjects: SetProperty<String> = objects.setProperty(String::class.java)

        @get:InputFiles
        @get:PathSensitive(PathSensitivity.NONE)
        abstract val pomFiles: ConfigurableFileCollection

        @get:InputFiles
        @get:PathSensitive(PathSensitivity.NONE)
        abstract val resolvedPomFiles: ConfigurableFileCollection

        @get:OutputFiles
        val outputFiles: Map<String, RegularFileProperty>
            get() = reports.enabledReports.mapValues { it.value.outputLocation }

        @Internal
        override fun getReports(): LicenseReportContainer = reports

        override fun reports(closure: Closure<*>): LicenseReportContainer =
            reports.apply {
                @Suppress("UNCHECKED_CAST")
                (closure as Closure<LicenseReportContainer>).delegate = this
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call(this)
            }

        override fun reports(configureAction: Action<in LicenseReportContainer>): LicenseReportContainer =
            reports.apply {
                configureAction.execute(this)
            }

        @TaskAction
        fun licensesReport() {
            val allPomFileSet: Set<File> = pomFiles.files
            val resolvedPomFileSet: Set<File> = resolvedPomFiles.files
            val libraries = generateLibraries(resolvedPomFileSet, allPomFileSet)
            createReport(libraries)
        }

        private fun generateLibraries(
            resolvedPomFileSet: Set<File>,
            allPomFileSet: Set<File>,
        ): List<Library> =
            resolvedPomFileSet
                .map { file ->
                    val model = getPomModel(file)
                    val licenses = model.findLicenses(allPomFileSet)

                    if (licenses.isEmpty()) {
                        logger.warn("${model.name} dependency does not have a license.")
                    }

                    Library(
                        MavenCoordinates(
                            model.findGroupId(allPomFileSet).orEmpty(),
                            model.findArtifactId(allPomFileSet).orEmpty(),
                            model.findVersion(allPomFileSet)?.let { version -> ComparableVersion(version) }
                                ?: ComparableVersion(""),
                        ),
                        model.name,
                        model.findDescription(allPomFileSet),
                        licenses,
                    )
                }.sortedWith(
                    compareBy<Library> { it.name ?: it.mavenCoordinates.identifierWithoutVersion }
                        .thenByDescending { it.mavenCoordinates.version },
                ).toList()

        private fun getPomModel(file: File): Model =
            MavenXpp3Reader().run {
                file.inputStream().use(::read)
            }

        private fun Model.findLicenses(pomFileSet: Set<File>): List<License> {
            if (licenses.isNotEmpty()) {
                return licenses.mapNotNull { license ->
                    val url = license.url
                    val name = license.name.trim().uppercaseFirstChar()
                    try {
                        // check for valid url
                        URI(url)
                    } catch (_: Exception) {
                        logger.warn("$name dependency has an invalid license URL; skipping license")
                        return@mapNotNull null
                    }

                    License(
                        getLicenseId(url, name),
                        name = name,
                        url = url,
                    )
                }
            }

            logger.info("Project $name has no license in POM file.")

            if (parent != null) {
                logger.info("Checking parent POM file.")
                return parent.getModel(pomFileSet).findLicenses(pomFileSet)
            }

            return emptyList()
        }

        private fun Parent.getModel(pomFileSet: Set<File>): Model {
            val pomFile =
                pomFileSet.find { file ->
                    file.name == "$artifactId-$version.pom" &&
                        file.path.contains(groupId.replace(".", "[./]").toRegex())
                } ?: error("Parent POM $groupId:$artifactId:$version not found in resolved POM files")
            return getPomModel(pomFile)
        }

        private fun Model.findVersion(pomFileSet: Set<File>): String? =
            when {
                version != null -> {
                    version
                }

                parent != null -> {
                    parent.version ?: parent.getModel(pomFileSet).findVersion(pomFileSet)
                }

                else -> {
                    null
                }
            }

        private fun Model.findDescription(pomFileSet: Set<File>): String? =
            when {
                description != null -> description
                parent != null -> parent.getModel(pomFileSet).findDescription(pomFileSet)
                else -> null
            }

        private fun Model.findGroupId(pomFileSet: Set<File>): String? =
            when {
                groupId != null -> {
                    groupId
                }

                parent != null -> {
                    parent.groupId ?: parent.getModel(pomFileSet).findGroupId(pomFileSet)
                }

                else -> {
                    null
                }
            }

        private fun Model.findArtifactId(pomFileSet: Set<File>): String? =
            when {
                artifactId != null -> {
                    artifactId
                }

                parent != null -> {
                    parent.artifactId ?: parent.getModel(pomFileSet).findArtifactId(pomFileSet)
                }

                else -> {
                    null
                }
            }

        private fun createReport(libraries: List<Library>) {
            if (libraries.isEmpty()) {
                return
            }

            reports.filter { it.required.get() }.forEach { report ->
                report.libraries = libraries
                report.write()
            }
        }

        private fun LicensesSingleFileReport.write() {
            with(outputLocation.get().asFile) {
                parentFile.mkdirs()
                outputStream().use {
                    writeLicenses(it)
                    logger.info("${this.name} report saved to $absolutePath")
                }
            }
        }
    }

private fun getLicenseId(
    licenseUrl: String,
    licenseName: String,
): LicenseId {
    val licenseMap = LicenseId.map
    return when {
        licenseMap.containsKey(licenseUrl) -> licenseMap.getOrDefault(licenseUrl, LicenseId.UNKNOWN)
        licenseMap.containsKey(licenseName) -> licenseMap.getOrDefault(licenseName, LicenseId.UNKNOWN)
        else -> LicenseId.UNKNOWN
    }
}
