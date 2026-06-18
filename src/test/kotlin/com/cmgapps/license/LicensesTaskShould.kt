/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.ReportType
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class LicensesTaskShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var reportFolder: String
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        reportFolder = "$testProjectDir/build/reports/licenses/licensesReport"
        project =
            ProjectBuilder
                .builder()
                .withProjectDir(testProjectDir.toFile())
                .build()
        val mavenRepoUrl = javaClass.getResource("/maven")!!.toURI().toString()
        project.repositories.add(
            project.repositories.maven { it.setUrl(mavenRepoUrl) },
        )
        project.plugins.apply("java")
        project.dependencies.add("implementation", "group:name:1.0.0")
    }

    @Test
    fun `generate HTML Report`() {
        val outputFile = File(reportFolder, "licenses.html")

        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) { task ->
                    task.reports { container ->
                        container.html.required.set(true)
                        container.html.useDarkMode.set(false)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}" +
                    "pre{white-space:pre-wrap}" +
                    "</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "<ul><li>Fake dependency name</li></ul>" +
                    "<div class=\"license\">" +
                    "<p>Some license</p>" +
                    "<a href=\"http://website.tld/\">http://website.tld/</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate HTML Report wih custom CSS`() {
        val outputFile = File(reportFolder, "licenses.html")

        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.html.required.set(true)
                        container.html.css.set(project.resources.text.fromString("body{}"))
                        container.html.useDarkMode.set(false)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{}</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "<ul><li>Fake dependency name</li></ul>" +
                    "<div class=\"license\">" +
                    "<p>Some license</p>" +
                    "<a href=\"http://website.tld/\">http://website.tld/</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate JSON Report`() {
        val outputFile = File(reportFolder, "licenses.json")

        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.json.required.set(true)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                """
                [
                    {
                        "mavenCoordinates": {
                            "groupId": "group",
                            "artifactId": "name",
                            "version": "1.0.0"
                        },
                        "name": "Fake dependency name",
                        "description": "Fake dependency description",
                        "licenses": [
                            {
                                "spdxLicenseIdentifier": null,
                                "name": "Some license",
                                "url": "http://website.tld/"
                            }
                        ]
                    }
                ]
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `generate XML Report`() {
        val outputFile = File(reportFolder, "licenses.xml")

        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.xml.required.set(true)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                """
                |<?xml version="1.0" encoding="UTF-8" ?>
                |<libraries xmlns="https://www.cmgapps.com" xsi:schemaLocation="https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                |  <library id="group:name" version="1.0.0">
                |    <name>
                |      Fake dependency name
                |    </name>
                |    <description>
                |      Fake dependency description
                |    </description>
                |    <licenses>
                |      <license url="http://website.tld/">
                |        <name>
                |          Some license
                |        </name>
                |      </license>
                |    </licenses>
                |  </library>
                |</libraries>
                |
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate Markdown Report`() {
        val outputFile = File(reportFolder, "licenses.md")
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports.markdown.required
                        .set(true)
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                """
                # Open source licenses
                ## Notice for packages
                * Fake dependency name
                ```
                Some license
                http://website.tld/
                ```
                
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `generate Plain text Report`() {
        val outputFile = File(reportFolder, "licenses.txt")
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports.plainText.required
                        .set(true)
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Licenses\n" +
                    "└─ Fake dependency name:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/",
            ),
        )
    }

    @Test
    fun `generate CSV Report`() {
        val outputFile = File(reportFolder, "licenses.csv")
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.csv.required.set(true)
                        container.csv.outputLocation.set(outputFile)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/\r\n",
            ),
        )
    }

    @Test
    fun `generate custom Report`() {
        val outputFile = File(reportFolder, "licenses")
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.custom.required.set(true)
                        container.custom.outputLocation.set(outputFile)
                        container.custom.generator.set { libraries ->
                            libraries
                                .map { (coordinates, lib) ->
                                    lib.name ?: coordinates.identifierWithoutVersion
                                }.joinToString()
                        }
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("Fake dependency name"))
    }

    @Test
    fun `generate HTML by default`() {
        val outputFile = File(reportFolder, "licenses.html")
        val task = project.tasks.register("licensesReport", LicensesTask::class.java).get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"color-scheme\" content=\"dark light\">" +
                    "<style>" +
                    "body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}" +
                    "@media(prefers-color-scheme: dark){body{background-color: #303030}pre,.license {background-color: #242424}}" +
                    "</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "<ul><li>Fake dependency name</li></ul>" +
                    "<div class=\"license\">" +
                    "<p>Some license</p>" +
                    "<a href=\"http://website.tld/\">http://website.tld/</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate all reports`() {
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.csv.required.set(true)

                        container.custom.required.set(true)
                        container.custom.generator.set { libraries ->
                            libraries
                                .map { (coordinates, lib) ->
                                    lib.name ?: coordinates.identifierWithoutVersion
                                }.joinToString()
                        }

                        container.html.required.set(true)
                        container.json.required.set(true)
                        container.markdown.required.set(true)
                        container.plainText.required.set(true)
                        container.xml.required.set(true)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(File(reportFolder).listFiles()?.size, `is`(7))
    }

    @Test
    fun `sort libraries`() {
        val outputFile = File(reportFolder, "licenses.txt")

        project.dependencies.add("implementation", "group:another:2.0.0")
        project.dependencies.add("implementation", "group:other:1.0.0")
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.html.required.set(false)
                        container.plainText.required.set(true)
                        container.plainText.outputLocation.set(outputFile)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Licenses\n" +
                    "├─ Fake dependency another:2.0.0\n" +
                    "│  ├─ License: Some license\n" +
                    "│  └─ URL: http://website.tld/\n" +
                    "├─ Fake dependency name:1.0.0\n" +
                    "│  ├─ License: Some license\n" +
                    "│  └─ URL: http://website.tld/\n" +
                    "└─ Fake dependency other:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/",
            ),
        )
    }

    @Test
    fun `handle parent version`() {
        val outputFile = File(reportFolder, "licenses.csv")

        project.dependencies.add(
            "implementation",
            "group:has-parent:1.0.0",
        )

        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.html.required.set(false)

                        container.csv.required.set(true)
                        container.csv.outputLocation.set(outputFile)
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Has Parent,1.0.0,group:has-parent:1.0.0,,Apache-2.0,Apache License 2.0,https://spdx.org/licenses/Apache-2.0.html\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/\r\n",
            ),
        )
    }

    @Test
    fun `have outputFiles`() {
        val task =
            project.tasks
                .register("licensesReport", LicensesTask::class.java) {
                    it.reports { container ->
                        container.forEach { report ->
                            report.required.set(true)
                            if (report is CustomReport) {
                                report.generator.set { libraries ->
                                    libraries
                                        .map { (coordinates, lib) ->
                                            lib.name ?: coordinates.identifierWithoutVersion
                                        }.joinToString()
                                }
                            }
                        }
                    }
                }.get()

        task.configurationToCheck(project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME))
        task.licensesReport()

        assertThat(
            task.outputFiles.keys,
            containsInAnyOrder(*ReportType.entries.map { it.name }.toTypedArray()),
        )
    }
}
