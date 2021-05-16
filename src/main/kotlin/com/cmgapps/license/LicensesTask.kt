/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license

import com.android.builder.model.ProductFlavor
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.reporter.CsvReport
import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.LicensesReport
import com.cmgapps.license.reporter.LicensesReportsContainer
import com.cmgapps.license.reporter.LicensesReportsContainerImpl
import com.cmgapps.license.reporter.MarkdownReport
import com.cmgapps.license.reporter.Report
import com.cmgapps.license.reporter.TextReport
import com.cmgapps.license.reporter.XmlReport
import groovy.lang.Closure
import groovy.lang.DelegatesTo
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import org.gradle.util.ClosureBackedAction
import java.io.File
import java.io.PrintStream
import java.net.URI
import java.net.URL

open class LicensesTask : DefaultTask() {

    companion object {
        private const val POM_CONFIGURATION = "poms"
        private const val TEMP_POM_CONFIGURATION = "tempPoms"

        private fun getClickableFileUrl(path: File) =
            URI("file", "", path.toURI().path, null, null).toString()
    }

    @Input
    var additionalProjects: Set<String> = emptySet()

    private val libraries = mutableListOf<Library>()

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

    fun reports(
        @DelegatesTo(
            value = LicensesReportsContainer::class,
            strategy = Closure.DELEGATE_FIRST
        ) closure: Closure<LicensesReportsContainer>
    ): LicensesReportsContainer {
        return reports(ClosureBackedAction(closure))
    }

    fun reports(configureAction: Action<in LicensesReportsContainer>): LicensesReportsContainer {
        configureAction.execute(reports)
        return reports
    }

    init {
        outputs.upToDateWhen { false }
        reports = LicensesReportsContainerImpl(this)
        reports.html.enabled.set(true)
    }

    @TaskAction
    fun licensesReport() {
        pomConfiguration = project.configurations.create(POM_CONFIGURATION)

        collectDependencies()
        generateLibraries()
        createReport()
        project.configurations.remove(pomConfiguration)
    }

    protected open fun collectDependencies() {
        val configurations = mutableSetOf<Configuration>()

        _allProjects.forEach { project ->
            project.configurations.find { it.name == "compile" }?.let {
                configurations.add(project.configurations.getByName("compile"))
            }

            project.configurations.find { it.name == "api" }?.let {
                configurations.add(project.configurations.getByName("api"))
            }

            project.configurations.find { it.name == "implementation" }?.let {
                configurations.add(project.configurations.getByName("implementation"))
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

    private fun generateLibraries() {
        pomConfiguration.resolvedConfiguration.lenientConfiguration.artifacts.forEach {

            getPomModel(it.file).let { model ->
                val licenses = findLicenses(model)

                if (licenses.isEmpty()) {
                    logger.warn("${model.name} dependency does not have a license.")
                }

                libraries.add(
                    Library(
                        model.name
                            ?: "${model.groupId}:${model.artifactId}",
                        model.version,
                        model.description,
                        licenses
                    )
                )
            }
        }
    }

    private fun getPomModel(file: File): Model = MavenXpp3Reader().run {
        read(file.inputStream())
    }

    private fun findLicenses(pom: Model): List<License> {

        if (pom.licenses.isNotEmpty()) {
            val licenses = mutableListOf<License>()
            pom.licenses.forEach { license ->
                try {
                    URL(license.url)
                    licenses.add(License(license.name.trim().capitalize(), license.url))
                } catch (ignore: java.lang.Exception) {
                    logger.warn("$name dependency has an invalid license URL; skipping license")
                }
            }
            return licenses
        }

        logger.info("Project $name has no license in POM file.")

        if (pom.parent != null) {
            logger.info("Checking parent POM file.")
            val parentPom = getParentPomFile(pom.parent)
            return findLicenses(parentPom)
        }

        return emptyList()
    }

    private fun getParentPomFile(parent: Parent): Model {

        val dependency = "${parent.groupId}:${parent.artifactId}:${parent.version}@pom"

        project.configurations.create(TEMP_POM_CONFIGURATION).dependencies.add(
            project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
        )

        val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION).incoming
            .artifacts.artifactFiles.singleFile

        project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

        return getPomModel(pomFile)
    }

    private fun createReport() {
        if (libraries.isEmpty()) {
            return
        }

        if (reports.html.enabled.get()) reports.html.writeFileReport(
            HtmlReport(
                libraries,
                reports.html.stylesheet,
                logger
            )
        )
        if (reports.csv.enabled.get()) reports.csv.writeFileReport(CsvReport(libraries))
        if (reports.json.enabled.get()) reports.json.writeFileReport(JsonReport(libraries))
        if (reports.markdown.enabled.get()) reports.markdown.writeFileReport(MarkdownReport(libraries))
        if (reports.text.enabled.get()) reports.text.writeFileReport(TextReport(libraries))
        if (reports.xml.enabled.get()) reports.xml.writeFileReport(XmlReport(libraries))
        val customReport = reports.custom.action
        if (reports.custom.enabled.get() && customReport != null) reports.custom.writeFileReport(
            CustomReport(
                libraries,
                customReport
            )
        )
    }

    private fun LicensesReport.writeFileReport(report: Report) {
        with(destination.get().asFile) {
            prepare()
            writeText(report.generate())
            logger.lifecycle("Wrote ${this@writeFileReport.name.toUpperCase()} report to ${getClickableFileUrl(this)}.")
        }
    }
}

open class AndroidLicensesTask : LicensesTask() {

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

private fun File.writeText(text: String) = PrintStream(outputStream()).use {
    it.print(text)
    it.flush()
}

private fun File.prepare() {
    delete()
    parentFile.mkdirs()
    createNewFile()
}
