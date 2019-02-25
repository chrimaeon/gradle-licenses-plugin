/*
 * Copyright (c)  2018. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import com.android.builder.model.ProductFlavor
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.JsonReport
import com.cmgapps.license.reporter.XmlReport
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
        private const val POM_CONFIGURATION = "poms"
        private const val TEMP_POM_CONFIGURATION = "tempPoms"

        private fun getClickableFileUrl(path: File) =
                URI("file", "", path.toURI().path, null, null).toString()
    }

    @Internal
    val libraries = mutableListOf<Library>()

    @OutputFile
    lateinit var outputFile: File

    @Optional
    @Input
    var variant: String? = null

    @Optional
    @Input
    var buildType: String? = null

    @Input
    lateinit var outputType: OutputType

    @Optional
    @Internal
    var productFlavors: List<ProductFlavor>? = null


    @Suppress("unused")
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
        generatePomInfo()
        createHtmlReport()
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
        // Add POM information to our POM configuration
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

        // If Android project, add extra configurations
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

    private fun generatePomInfo() {
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

        logger.info("Project, $name, has no license in POM file.")

        if (pom.parent != null) {
            val parentPom = getParentPomFile(pom.parent)
            return findLicenses(parentPom)
        }

        return null
    }

    /**
     * Use Parent POM information when individual dependency license information is missing.
     */
    private fun getParentPomFile(parent: Parent): Model {
        // Get parent POM information

        val dependency = "${parent.groupId}:${parent.artifactId}:${parent.version}@pom"

        // Add dependency to temporary configuration
        project.configurations.create(TEMP_POM_CONFIGURATION).dependencies.add(
                project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
        )

        val pomFile = project.configurations.getByName(TEMP_POM_CONFIGURATION).incoming
                .artifacts.artifactFiles.singleFile

        // Reset dependencies in temporary configuration
        project.configurations.remove(project.configurations.getByName(TEMP_POM_CONFIGURATION))

        return getPomModel(pomFile)
    }

    private fun createHtmlReport() {
        outputFile.delete()
        outputFile.parentFile.mkdirs()
        outputFile.createNewFile()

        PrintStream(outputFile.outputStream()).run {
            val report = when (outputType) {
                OutputType.HTML -> HtmlReport(libraries)
                OutputType.XML -> XmlReport(libraries)
                OutputType.JSON -> JsonReport(libraries)
            }
            print(report.generate())
        }

        logger.lifecycle("Wrote ${outputType.name} report to ${getClickableFileUrl(outputFile)}.")
    }
}
