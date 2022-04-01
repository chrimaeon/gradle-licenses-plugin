/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.android.builder.model.ProductFlavor
import com.cmgapps.license.helper.LICENSE_MAP
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.reporter.CsvReport
import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.CustomizableHtmlReport
import com.cmgapps.license.reporter.CustomizableReport
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.LicensesReport
import com.cmgapps.license.reporter.LicensesReportsContainer
import com.cmgapps.license.reporter.MarkdownReport
import com.cmgapps.license.reporter.Report
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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.PrintStream
import java.net.URI
import java.net.URL
import java.util.Locale
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.javaType

abstract class LicensesTask : DefaultTask() {

    companion object {
        private const val POM_CONFIGURATION = "poms"
        private const val TEMP_POM_CONFIGURATION = "tempPoms"

        private fun getClickableFileUrl(path: File) =
            URI("file", "", path.toURI().path, null, null).toString()
    }

    @Input
    var additionalProjects: Set<String> = emptySet()

    private lateinit var pomConfiguration: Configuration

    private val _allProjects: Set<Project> by lazy {
        val allProjects = project.rootProject.allprojects

        setOf(project) + additionalProjects.map { moduleName ->
            allProjects.find {
                it.path == moduleName
            } ?: throw IllegalArgumentException("$moduleName not found")
        }.toSet()
    }

    @Internal
    protected fun getAllProjects(): Set<Project> {
        return _allProjects
    }

    @Nested
    val reports: LicensesReportsContainer

    fun reports(configureAction: Action<in LicensesReportsContainer>): LicensesReportsContainer {
        configureAction.execute(reports)
        return reports
    }

    init {
        reports = LicensesReportsContainerImpl()
        reports.html.enabled = true
        reports.getAll().forEach {
            outputs.file(it.destination)
        }
    }

    @TaskAction
    fun licensesReport() {
        pomConfiguration = project.configurations.create(POM_CONFIGURATION)

        collectDependencies()
        val libraries = generateLibraries()
        createReport(libraries)
        project.configurations.remove(pomConfiguration)
    }

    protected open fun collectDependencies() {
        val configurations = mutableSetOf<Configuration>()

        _allProjects.forEach { project ->
            project.configurations.findByName("compile")?.let {
                configurations.add(project.configurations.getByName(it.name))
            }

            project.configurations.findByName("api")?.let {
                configurations.add(project.configurations.getByName(it.name))
            }

            project.configurations.findByName("implementation")?.let {
                configurations.add(project.configurations.getByName(it.name))
            }
        }

        addConfigurations(configurations)
    }

    protected fun addConfigurations(configurations: Set<Configuration>) {
        configurations.forEach { configuration ->
            configuration.incoming.dependencies.withType(ExternalDependency::class.java).map { dep ->
                "${dep.group}:${dep.name}:${dep.version}@pom"
            }.forEach { pom ->
                pomConfiguration.dependencies.add(
                    project.dependencies.add(POM_CONFIGURATION, pom)
                )
            }
        }
    }

    private fun generateLibraries(): List<Library> {
        return pomConfiguration.resolvedConfiguration.lenientConfiguration.artifacts.map {

            val model = getPomModel(it.file)
            val licenses = model.findLicenses()

            if (licenses.isEmpty()) {
                logger.warn("${model.name} dependency does not have a license.")
            }

            Library(
                MavenCoordinates(
                    model.findGroupId().orEmpty(),
                    model.findArtifactId().orEmpty(),
                    model.findVersion()?.let { version -> ComparableVersion(version) } ?: ComparableVersion(""),
                ),
                model.name,
                model.findDescription(),
                licenses
            )
        }
            .sortedWith(Library.NameComparator())
            .toList()
    }

    private fun getPomModel(file: File): Model = MavenXpp3Reader().run {
        file.inputStream().use {
            read(it)
        }
    }

    private fun Model.findLicenses(): List<License> {
        if (licenses.isNotEmpty()) {
            return licenses.mapNotNull { license ->
                try {
                    val url = license.url
                    val name = license.name.trim().capitalize()
                    // check for valid url
                    URL(url)

                    License(
                        getLicenseId(url, name),
                        name = name,
                        url = url,
                    )
                } catch (ignore: Exception) {
                    logger.warn("$name dependency has an invalid license URL; skipping license")
                    null
                }
            }
        }

        logger.info("Project $name has no license in POM file.")

        if (parent != null) {
            logger.info("Checking parent POM file.")
            return parent.getModel().findLicenses()
        }

        return emptyList()
    }

    private fun Parent.getModel(): Model {

        val dependency = "$groupId:$artifactId:$version@pom"

        project.configurations.create(TEMP_POM_CONFIGURATION).dependencies.add(
            project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
        )

        val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION).incoming
            .artifacts.artifactFiles.singleFile

        project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

        return getPomModel(pomFile)
    }

    private fun Model.findVersion(): String? = when {
        version != null -> version
        parent != null ->
            if (parent.version != null) {
                parent.version
            } else {
                parent.getModel().findVersion()
            }
        else -> null
    }

    private fun Model.findDescription(): String? = when {
        description != null -> description
        parent != null -> parent.getModel().findDescription()
        else -> null
    }

    private fun Model.findGroupId(): String? = when {
        groupId != null -> groupId
        parent != null -> if (parent.groupId != null) parent.groupId else parent.getModel().findGroupId()
        else -> null
    }

    private fun Model.findArtifactId(): String? = when {
        artifactId != null -> artifactId
        parent != null -> if (parent.artifactId != null) parent.artifactId else parent.getModel().findArtifactId()
        else -> null
    }

    private fun createReport(libraries: List<Library>) {
        if (libraries.isEmpty()) {
            return
        }

        reports.getAll().forEach { licenseReport ->
            if (licenseReport.enabled) {
                val report = when (licenseReport.type) {
                    ReportType.CSV -> CsvReport(libraries)
                    ReportType.CUSTOM -> {
                        val customReport = reports.custom.action
                        if (customReport != null)
                            CustomReport(
                                libraries,
                                customReport
                            )
                        else null
                    }
                    ReportType.HTML -> HtmlReport(
                        libraries,
                        reports.html._stylesheet.orNull,
                        logger
                    )
                    ReportType.JSON -> JsonReport(libraries)
                    ReportType.MARKDOWN -> MarkdownReport(libraries, logger)
                    ReportType.TEXT -> TextReport(libraries)
                    ReportType.XML -> XmlReport(libraries)
                }

                report?.let { licenseReport.writeFileReport(it) }
            }
        }
    }

    private fun LicensesReport.writeFileReport(report: Report) {
        with(destination) {
            prepare()
            writeText(report.generate())
            logger.lifecycle(
                "Wrote ${this@writeFileReport.name.uppercase(Locale.US)} report to ${getClickableFileUrl(this)}."
            )
        }
    }

    private inner class LicensesReportsContainerImpl : LicensesReportsContainer {
        override val csv: LicensesReport by LicenseReportDelegate(ReportType.CSV)
        override fun csv(config: Action<LicensesReport>) {
            csv.configure(config)
        }

        override fun csv(config: Closure<LicensesReport>) {
            project.configure(csv, config)
        }

        override val html: CustomizableHtmlReport by LicenseReportDelegate(ReportType.HTML)

        override fun html(config: Action<CustomizableHtmlReport>) {
            html.configure(configHtml = config)
        }

        override fun html(config: Closure<CustomizableHtmlReport>) {
            project.configure(html, config)
        }

        override val json: LicensesReport by LicenseReportDelegate(ReportType.JSON)
        override fun json(config: Action<LicensesReport>) {
            json.configure(config)
        }

        override fun json(config: Closure<LicensesReport>) {
            project.configure(json, config)
        }

        override val markdown: LicensesReport by LicenseReportDelegate(ReportType.MARKDOWN)
        override fun markdown(config: Action<LicensesReport>) {
            markdown.configure(config)
        }

        override fun markdown(config: Closure<LicensesReport>) {
            project.configure(markdown, config)
        }

        override val text: LicensesReport by LicenseReportDelegate(ReportType.TEXT)
        override fun text(config: Action<LicensesReport>) {
            text.configure(config)
        }

        override fun text(config: Closure<LicensesReport>) {
            project.configure(text, config)
        }

        override val xml: LicensesReport by LicenseReportDelegate(ReportType.XML)
        override fun xml(config: Action<LicensesReport>) {
            xml.configure(config)
        }

        override fun xml(config: Closure<LicensesReport>) {
            project.configure(xml, config)
        }

        override val custom: CustomizableReport by LicenseReportDelegate(ReportType.CUSTOM)
        override fun custom(config: Action<CustomizableReport>) {
            custom.configure(configCustom = config)
        }

        override fun custom(config: Closure<CustomizableReport>) {
            project.configure(custom, config)
        }

        override fun getAll(): List<LicensesReport> = listOf(
            csv,
            html,
            json,
            markdown,
            text,
            xml,
            custom
        )

        private inner class LicenseReportDelegate<T : LicensesReport>(private val reportType: ReportType) :
            ReadOnlyProperty<Any?, T> {

            private lateinit var value: T

            @Suppress("UNCHECKED_CAST")
            @OptIn(ExperimentalStdlibApi::class)
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return if (::value.isInitialized) {
                    value
                } else {
                    (
                        Class.forName(property.returnType.javaType.typeName)
                            .getConstructor(ReportType::class.java, Task::class.java, Project::class.java)
                            .newInstance(reportType, this@LicensesTask, project) as T
                        ).also { value = it }
                }
            }
        }
    }
}

abstract class AndroidLicensesTask : LicensesTask() {

    @Input
    lateinit var variant: String

    @Input
    lateinit var buildType: String

    @Internal
    lateinit var productFlavors: List<ProductFlavor>

    override fun collectDependencies() {

        super.collectDependencies()

        val configurations = mutableSetOf<Configuration>()

        getAllProjects().forEach { project ->
            project.configurations.find { it.name == "${buildType}Compile" }?.let {
                configurations.add(it)
            }

            project.configurations.find { it.name == "${buildType}Api" }?.let {
                configurations.add(it)
            }

            project.configurations.find { it.name == "${buildType}Implementation" }?.let {
                configurations.add(it)
            }

            productFlavors.forEach { flavor ->
                // Works for productFlavors and productFlavors with dimensions
                if (variant.capitalize().contains(flavor.name.capitalize())) {
                    project.configurations.find { it.name == "${flavor.name}Compile" }?.let {
                        configurations.add(it)
                    }

                    project.configurations.find { it.name == "${flavor.name}Api" }?.let {
                        configurations.add(it)
                    }

                    project.configurations.find { it.name == "${flavor.name}Implementation" }?.let {
                        configurations.add(it)
                    }
                }
            }
        }

        addConfigurations(configurations)
    }
}

abstract class KotlinMultiplatformTask : LicensesTask() {

    @get:Internal
    internal lateinit var targetNames: List<String>

    override fun collectDependencies() {
        super.collectDependencies()

        val configurations = mutableSetOf<Configuration>()

        targetNames.forEach { name ->
            project.configurations.find { it.name == "${name}MainApi" }?.let {
                configurations.add(it)
            }
            project.configurations.find { it.name == "${name}MainImplementation" }?.let {
                configurations.add(it)
            }
        }

        addConfigurations(configurations)
    }
}

private fun File.writeText(text: String) = PrintStream(outputStream()).use {
    it.print(text)
    it.flush()
}

private fun File.prepare() {
    delete()
    parentFile.mkdirs()
    createNewFile()
}

private fun getLicenseId(licenseUrl: String, licenseName: String): LicenseId = when {
    LICENSE_MAP.containsKey(licenseUrl) -> LICENSE_MAP[licenseUrl]!!
    LICENSE_MAP.containsKey(licenseName) -> LICENSE_MAP[licenseName]!!
    else -> LicenseId.UNKNOWN
}
