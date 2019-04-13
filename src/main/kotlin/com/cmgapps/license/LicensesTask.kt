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
import com.cmgapps.license.reporter.*
import org.apache.maven.model.Model
import org.apache.maven.model.Parent
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.tasks.*
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
    var variant: String? = null

    @Optional
    @Input
    var buildType: String? = null

    @Optional
    @Internal
    var productFlavors: List<ProductFlavor>? = null

    @Optional
    @Input
    var bodyCss: String? = null

    @Optional
    @Input
    var preCss: String? = null

    @Internal
    val libraries = mutableListOf<Library>()

    @TaskAction
    fun licensesReport() {
        if (!this::outputFile.isInitialized) {
            throw IllegalStateException("outputFile must be set")
        }

        if (!this::outputType.isInitialized) {
            throw IllegalStateException("outputType must be set")
        }
        setupEnvironment()
        collectDependencies()
        generateLibraries()
        createReport()
        cleanUpEnvironment()
    }

    private fun setupEnvironment() {
        project.configurations.create(POM_CONFIGURATION)

        project.configurations.forEach {
            try {
                it.isCanBeResolved = true
            } catch (ignore: Exception) {
            }
        }
    }

    private fun collectDependencies() {
        val configurations = mutableSetOf<Configuration>()

        if (project.configurations.find { it.name == "compile" } != null) {
            configurations.add(project.configurations.getByName("compile"))
        }

        if (project.configurations.find { it.name == "api" } != null) {
            configurations.add(project.configurations.getByName("api"))
        }

        if (project.configurations.find { it.name == "implementation" } != null) {
            configurations.add(project.configurations.getByName("implementation"))
        }

        //Android project -> add additional configurations
        if (variant != null) {

            if (project.configurations.find { it.name == "compile" } != null) {
                configurations.add(project.configurations.getByName("${buildType}Compile"))
            }

            if (project.configurations.find { it.name == "api" } != null) {
                configurations.add(project.configurations.getByName("${buildType}Api"))
            }

            if (project.configurations.find { it.name == "implementation" } != null) {
                configurations.add(project.configurations.getByName("${buildType}Implementation"))
            }

            productFlavors?.forEach { flavor ->
                // Works for productFlavors and productFlavors with dimensions
                if (variant!!.capitalize().contains(flavor.name.capitalize())) {
                    if (project.configurations.find { it.name == "compile" } != null) {
                        configurations.add(project.configurations.getByName("${flavor.name}Compile"))
                    }
                    if (project.configurations.find { it.name == "api" } != null) {
                        configurations.add(project.configurations.getByName("${flavor.name}Api"))
                    }
                    if (project.configurations.find { it.name == "implementation" } != null) {
                        configurations.add(project.configurations.getByName("${flavor.name}Implementation"))
                    }
                }
            }
        }

        configurations.forEach { configuration ->
            configuration.incoming.dependencies.withType(ExternalModuleDependency::class.java).map { module ->
                "${module.group}:${module.name}:${module.version}@pom"
            }.forEach { pom ->
                project.configurations.getByName(POM_CONFIGURATION).dependencies.add(
                    project.dependencies.add(POM_CONFIGURATION, pom)
                )
            }
        }
    }

    private fun generateLibraries() {
        project.configurations.getByName(POM_CONFIGURATION).incoming.artifacts.forEach { pom ->

            val model = getPomModel(pom.file)

            var licenses = findLicenses(model)

            if (licenses == null) {
                logger.warn("${model.name} dependency does not have a license.")
                licenses = emptyList()
            }

            libraries.add(Library(model.name
                ?: "${model.groupId}:${model.artifactId}", model.version, model.description, licenses))
        }
    }

    private fun getPomModel(file: File): Model = MavenXpp3Reader().run {
        read(file.inputStream())
    }


    private fun findLicenses(pom: Model): List<License>? {

        if (!pom.licenses.isEmpty()) {
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

        return null
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

    private fun cleanUpEnvironment() {
        project.configurations.remove(project.configurations.getByName(POM_CONFIGURATION))
    }
}
