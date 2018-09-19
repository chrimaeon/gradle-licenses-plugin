/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.*

class LicensesTask extends DefaultTask {
    final static def POM_CONFIGURATION = 'poms'
    final static def TEMP_POM_CONFIGURATION = 'tempPoms'

    @Internal
    final List<Library> libraries = []

    @OutputFile
    File htmlFile

    @Optional
    @Input
    String variant

    @Optional
    @Input
    String buildType

    @Optional
    @Internal
    def productFlavors

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    def licensesReport() {
        setupEnvironment()
        collectDependencies()
        generatePomInfo()
        createHtmlReport()
    }

    private def setupEnvironment() {
        project.configurations.create(POM_CONFIGURATION)

        project.configurations.every {
            try {
                it.canBeResolved = true
            } catch (Exception ignore) {

            }
        }
    }

    private def collectDependencies() {
        // Add POM information to our POM configuration
        final Set<Configuration> configurations = new LinkedHashSet<>()

        if (project.configurations.find { it.name == "compile" }) configurations << project.configurations."compile"
        if (project.configurations.find { it.name == "api" }) configurations << project.configurations."api"
        if (project.configurations.find {
            it.name == "implementation"
        }) configurations << project.configurations."implementation"

        // If Android project, add extra configurations
        if (variant) {

            if (project.configurations.find {
                it.name == "compile"
            }) configurations << project.configurations."${buildType}Compile"

            if (project.configurations.find {
                it.name == "api"
            }) configurations << project.configurations."${buildType}Api"

            if (project.configurations.find {
                it.name == "implementation"
            }) configurations << project.configurations."${buildType}Implementation"

            productFlavors.each { flavor ->
                // Works for productFlavors and productFlavors with dimensions
                if (variant.capitalize().contains(flavor.name.capitalize())) {
                    if (project.configurations.find {
                        it.name == "compile"
                    }) configurations << project.configurations."${flavor.name}Compile"
                    if (project.configurations.find {
                        it.name == "api"
                    }) configurations << project.configurations."${flavor.name}Api"
                    if (project.configurations.find {
                        it.name == "implementation"
                    }) configurations << project.configurations."${flavor.name}Implementation"
                }
            }
        }

        configurations.each { configuration ->
            configuration.canBeResolved &&
                    configuration.resolvedConfiguration.lenientConfiguration.artifacts*.moduleVersion.id.collect { id ->
                        "$id.group:$id.name:$id.version@pom"
                    }.each { pom ->
                        project.configurations."$POM_CONFIGURATION".dependencies.add(
                                project.dependencies.add("$POM_CONFIGURATION", pom)
                        )
                    }
        }
    }

    private def generatePomInfo() {
        project.configurations."$POM_CONFIGURATION".resolvedConfiguration.lenientConfiguration.artifacts.each { pom ->
            final File pomFile = pom.file
            final Node pomText = new XmlParser().parse(pomFile)

            final def name = getName(pomText)
            final def version = pomText.version?.text()?.trim()
            final def description = pomText.description?.text()?.trim()

            def licenses = findLicenses(pomFile)

            if (!licenses) {
                logger.log(LogLevel.WARN, "${name} dependency does not have a license.")
                licenses = []
            }

            libraries << new Library(name: name, version: version, description: description, licenses: licenses)
        }
    }

    static String getName(def pomText) {
        def name = pomText.name?.text() ? pomText.name?.text() : pomText.artifactId?.text()
        return name?.trim()
    }

    List<License> findLicenses(File pomFile) {
        if (!pomFile) {
            return null
        }

        final def pomText = new XmlParser().parse(pomFile)

        // If the POM is missing a name, do not record it
        final def name = getName(pomText)

        if (!name) {
            logger.log(LogLevel.WARN, "POM file is missing a name: ${pomFile}")
            return null
        }

        // License information found
        if (pomText.licenses) {
            List<License> licenses = []
            pomText.licenses[0].license.each { license ->
                def licenseName = license.name?.text()
                def licenseUrl = license.url?.text()
                try {
                    //noinspection GroovyResultOfObjectAllocationIgnored
                    new URL(licenseUrl)
                    licenseName = licenseName?.trim()?.capitalize()
                    licenseUrl = licenseUrl?.trim()
                    licenses << new License(name: licenseName, url: licenseUrl)
                } catch (Exception ignore) {
                    logger.log(LogLevel.WARN, "${name} dependency has an invalid license URL; skipping license")
                }
            }
            return licenses
        }
        logger.log(LogLevel.INFO, "Project, ${name}, has no license in POM file.")

        final def hasParent = pomText.parent != null
        if (hasParent) {
            final def parentPomFile = getParentPomFile(pomText)
            return findLicenses(parentPomFile)
        }
        return null
    }

    /**
     * Use Parent POM information when individual dependency license information is missing.
     */
    private File getParentPomFile(def pomText) {
        // Get parent POM information
        def groupId = pomText?.parent?.groupId?.text()
        def artifactId = pomText?.parent?.artifactId?.text()
        def version = pomText?.parent?.version?.text()
        def dependency = "$groupId:$artifactId:$version@pom"

        // Add dependency to temporary configuration
        project.configurations.create(TEMP_POM_CONFIGURATION)
        project.configurations."$TEMP_POM_CONFIGURATION".dependencies.add(
                project.dependencies.add(TEMP_POM_CONFIGURATION, dependency)
        )

        def pomFile = (File) project.configurations."$TEMP_POM_CONFIGURATION".resolvedConfiguration.lenientConfiguration.artifacts?.file[0]

        // Reset dependencies in temporary configuration
        project.configurations.remove(project.configurations."$TEMP_POM_CONFIGURATION")

        return pomFile
    }

    private def createHtmlReport() {
        project.file(htmlFile).delete()

        htmlFile.parentFile.mkdirs()
        htmlFile.createNewFile()
        htmlFile.withOutputStream { outputStream ->
            final def printStream = new PrintStream(outputStream)
            printStream.print(new HtmlReport(libraries).generate())
        }

        logger.log(LogLevel.LIFECYCLE, "Wrote HTML report to ${getClickableFileUrl(htmlFile)}.")
    }

    private static def getClickableFileUrl(File path) {
        new URI("file", "", path.toURI().getPath(), null, null).toString()
    }
}
