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
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.MarkdownReport
import com.cmgapps.license.reporter.TextReport
import com.cmgapps.license.reporter.XmlReport
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.PrintStream
import java.net.URI
import java.net.URL

open class LicensesTask : DefaultTask() {

    companion object {
        const val DEFAULT_PRE_CSS = "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}"
        const val DEFAULT_BODY_CSS = "body{font-family:sans-serif;background-color:#eee}"

        private const val POM_CONFIGURATION = "poms"
        private const val TEMP_POM_CONFIGURATION = "tempPoms"

        private fun getClickableFileUrl(path: File) =
            URI("file", "", path.toURI().path, null, null).toString()
    }

    @OutputFile
    lateinit var outputFile: File

    @Input
    lateinit var outputType: OutputType

    @Optional
    @Input
    var bodyCss: String? = null

    @Optional
    @Input
    var preCss: String? = null

    @Internal
    @Input
    var projects: Set<String> = emptySet()

    @Internal
    private val libraries = mutableListOf<Library>()

    @Internal
    private lateinit var pomConfiguration: Configuration

    @TaskAction
    fun licensesReport() {
        check(this::outputFile.isInitialized) { "outputFile must be set" }
        check(this::outputType.isInitialized) { "outputType must be set" }

        pomConfiguration = project.configurations.create(POM_CONFIGURATION)
        collectDependencies()
        generateLibraries()
        createReport()
        project.configurations.remove(pomConfiguration)
    }

    protected open fun collectDependencies() {
        val configurations = mutableSetOf<Configuration>()

        getAllProjects().forEach { project ->
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

    protected fun getAllProjects(): List<Project> {
        val allProjects = project.rootProject.allprojects

        return listOf(project) + projects.map { moduleName ->
            allProjects.find {
                it.path == moduleName
            } ?: throw IllegalArgumentException("$moduleName not found")
        }
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

                libraries.add(Library(model.name
                    ?: "${model.groupId}:${model.artifactId}", model.version, model.description, licenses))
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
        outputFile.delete()
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()

        PrintStream(outputFile.outputStream()).use {
            val report = when (outputType) {
                OutputType.HTML -> {
                    HtmlReport(libraries, bodyCss ?: DEFAULT_BODY_CSS, preCss ?: DEFAULT_PRE_CSS)
                }
                OutputType.XML -> XmlReport(libraries)
                OutputType.JSON -> JsonReport(libraries)
                OutputType.TEXT -> TextReport(libraries)
                OutputType.MD -> MarkdownReport(libraries)
            }
            it.print(report.generate())
            it.flush()
        }

        logger.lifecycle("Wrote ${outputType.name} report to ${getClickableFileUrl(outputFile)}.")
    }
}

open class AndroidLicensesTask : LicensesTask() {

    @Optional
    @Input
    var variant: String? = null

    @Optional
    @Input
    var buildType: String? = null

    @Optional
    @Internal
    var productFlavors: List<ProductFlavor>? = null

    override fun collectDependencies() {

        super.collectDependencies()

        val configurations = mutableSetOf<Configuration>()

        getAllProjects().forEach { project ->

            variant?.let {

                project.configurations.find { it.name == "compile" }?.let {
                    configurations.add(project.configurations.getByName("${buildType}Compile"))
                }

                project.configurations.find { it.name == "api" }?.let {
                    configurations.add(project.configurations.getByName("${buildType}Api"))
                }

                project.configurations.find { it.name == "implementation" }?.let {
                    configurations.add(project.configurations.getByName("${buildType}Implementation"))
                }

                productFlavors?.forEach { flavor ->
                    // Works for productFlavors and productFlavors with dimensions
                    if (it.capitalize().contains(flavor.name.capitalize())) {
                        project.configurations.find { it.name == "compile" }?.let {
                            configurations.add(project.configurations.getByName("${flavor.name}Compile"))
                        }

                        project.configurations.find { it.name == "api" }?.let {
                            configurations.add(project.configurations.getByName("${flavor.name}Api"))
                        }

                        project.configurations.find { it.name == "implementation" }?.let {
                            configurations.add(project.configurations.getByName("${flavor.name}Implementation"))
                        }
                    }
                }
            }
        }

        addConfigurations(configurations)
    }
}
