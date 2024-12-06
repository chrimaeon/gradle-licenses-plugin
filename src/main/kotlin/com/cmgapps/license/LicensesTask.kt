/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.reporting.ReportContainer
import org.gradle.api.reporting.Reporting
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.namedDomainObjectSet
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.io.File
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

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

internal class LicenseReportContainerImpl(
    private val project: Project,
    task: Task,
) : NamedDomainObjectSet<LicensesSingleFileReport> by project.objects.namedDomainObjectSet(LicensesSingleFileReport::class),
    LicenseReportContainer {
    init {
        addReporter(TextReport::class.java, project, task)
        addReporter(JsonReport::class.java, project, task)
        addReporter(HtmlReport::class.java, project, task, project.logger, project.objects)
        addReporter(XmlReport::class.java, project, task)
        addReporter(MarkdownReport::class.java, project, task, project.logger)
        addReporter(CsvReport::class.java, project, task)
        addReporter(CustomReport::class.java, project, task)
    }

    override fun getEnabled(): NamedDomainObjectSet<LicensesSingleFileReport> = enabled

    @Override
    override fun getEnabledReports(): MutableMap<String, LicensesSingleFileReport> = enabled.asMap

    private val enabled: NamedDomainObjectSet<LicensesSingleFileReport> =
        matching { element -> element.required.get() }

    override fun configure(cl: Closure<*>?): ReportContainer<LicensesSingleFileReport> {
        cl?.invoke(this)
        return this
    }

    private fun addReporter(
        clazz: Class<out LicensesSingleFileReport>,
        vararg params: Any,
    ) {
        add(project.objects.newInstance(clazz, *params))
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
}

abstract class LicensesTask :
    DefaultTask(),
    Reporting<LicenseReportContainer> {
    private val tempConfigurationNameCounter = AtomicInteger(1)

    companion object {
        private const val POM_CONFIGURATION = "licensesPoms"
        private const val TEMP_POM_CONFIGURATION = "licensesTempPoms"
    }

    @Input
    var additionalProjects: Set<String> = emptySet()

    private lateinit var pomConfiguration: Configuration

    @Suppress("ktlint:standard:backing-property-naming")
    private val _allProjects: Set<Project> by lazy {
        val allProjects = project.rootProject.allprojects

        setOf(project) +
            additionalProjects
                .map { moduleName ->
                    allProjects.find {
                        it.path == moduleName
                    } ?: throw IllegalArgumentException("$moduleName not found")
                }.toSet()
    }

    @get:Internal
    protected val allProjects: Set<Project>
        get() = _allProjects

    private val reports: LicenseReportContainer =
        LicenseReportContainerImpl(project, this)

    @Internal
    override fun getReports(): LicenseReportContainer = reports

    override fun reports(closure: Closure<*>): LicenseReportContainer =
        reports.apply {
            project.configure(this, closure)
        }

    override fun reports(configureAction: Action<in LicenseReportContainer>): LicenseReportContainer =
        reports.apply {
            configureAction.execute(this)
        }

    @TaskAction
    fun licensesReport() {
        pomConfiguration =
            project.configurations.create(POM_CONFIGURATION).apply {
                isCanBeResolved = true
                isCanBeConsumed = false
            }

        collectDependencies()
        val libraries = generateLibraries()
        createReport(libraries)
        project.configurations.remove(pomConfiguration)
    }

    protected open fun collectDependencies() {
        buildSet {
            allProjects.forEach { project ->
                project.configurations.findByName("compile")?.let {
                    add(project.configurations.getByName(it.name))
                }

                project.configurations.findByName("api")?.let {
                    add(project.configurations.getByName(it.name))
                }

                project.configurations.findByName("implementation")?.let {
                    add(project.configurations.getByName(it.name))
                }
            }
        }.let {
            addConfigurations(it)
        }
    }

    protected fun addConfigurations(configurations: Set<Configuration>) {
        configurations.forEach { configuration ->
            configuration.incoming.dependencies
                .withType(ExternalDependency::class.java)
                .map { dep ->
                    "${dep.group}:${dep.name}:${dep.version}@pom"
                }.forEach { pom ->
                    pomConfiguration.dependencies.add(
                        project.dependencies.add(POM_CONFIGURATION, pom),
                    )
                }
        }
    }

    private fun generateLibraries(): List<Library> =
        pomConfiguration.resolvedConfiguration.lenientConfiguration.artifacts
            .map {
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
                    licenses,
                )
            }.sortedWith(Library.NameComparator())
            .toList()

    private fun getPomModel(file: File): Model =
        MavenXpp3Reader().run {
            file.inputStream().use {
                read(it)
            }
        }

    private fun Model.findLicenses(): List<License> {
        if (licenses.isNotEmpty()) {
            return licenses.mapNotNull { license ->
                try {
                    val url = license.url
                    val name = license.name.trim().uppercaseFirstChar()
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

        logger.lifecycle("Project $name has no license in POM file.")

        if (parent != null) {
            logger.lifecycle("Checking parent POM file.")
            return parent.getModel().findLicenses()
        }

        return emptyList()
    }

    private fun Parent.getModel(): Model {
        val dependency = "$groupId:$artifactId:$version@pom"

        val configName = TEMP_POM_CONFIGURATION + tempConfigurationNameCounter.getAndIncrement()
        val configuration =
            project.configurations
                .create(configName)
                .apply {
                    isCanBeResolved = true
                    isCanBeConsumed = false
                    isTransitive = false
                }

        project.dependencies.add(configName, dependency)

        val pomFile =
            configuration.incoming
                .artifacts.artifactFiles.singleFile

        project.configurations.remove(configuration)

        return getPomModel(pomFile)
    }

    private fun Model.findVersion(): String? =
        when {
            version != null -> version
            parent != null ->
                if (parent.version != null) {
                    parent.version
                } else {
                    parent.getModel().findVersion()
                }

            else -> null
        }

    private fun Model.findDescription(): String? =
        when {
            description != null -> description
            parent != null -> parent.getModel().findDescription()
            else -> null
        }

    private fun Model.findGroupId(): String? =
        when {
            groupId != null -> groupId
            parent != null ->
                if (parent.groupId != null) {
                    parent.groupId
                } else {
                    parent.getModel().findGroupId()
                }

            else -> null
        }

    private fun Model.findArtifactId(): String? =
        when {
            artifactId != null -> artifactId
            parent != null ->
                if (parent.artifactId != null) {
                    parent.artifactId
                } else {
                    parent.getModel().findArtifactId()
                }

            else -> null
        }

    private fun createReport(libraries: List<Library>) {
        if (libraries.isEmpty()) {
            return
        }

        reports.filter { it.required.get() }.forEach { report ->
            report.libraries = libraries
            (report as LicensesSingleFileReport).write()
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

abstract class AndroidLicensesTask : LicensesTask() {
    @Input
    lateinit var variant: String

    @Input
    lateinit var buildType: String

    @Internal
    lateinit var productFlavors: List<String>

    override fun collectDependencies() {
        super.collectDependencies()

        buildSet {
            allProjects.forEach { project ->
                addAll(addConfiguration(project, buildType))

                productFlavors.forEach { flavor ->
                    // Works for productFlavors and productFlavors with dimensions
                    if (variant.uppercaseFirstChar().contains(flavor.uppercaseFirstChar())) {
                        addAll(addConfiguration(project, flavor))
                    }
                }
            }
        }.let {
            addConfigurations(it)
        }
    }

    private fun addConfiguration(
        project: Project,
        type: String,
    ) = buildSet {
        project.configurations.find { it.name == "${type}Compile" }?.let {
            add(it)
        }

        project.configurations.find { it.name == "${type}Api" }?.let {
            add(it)
        }

        project.configurations.find { it.name == "${type}Implementation" }?.let {
            add(it)
        }
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
