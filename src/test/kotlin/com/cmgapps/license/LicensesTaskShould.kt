/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
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
        project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir.toFile())
            .build()
        val mavenRepoUrl = javaClass.getResource("/maven")!!.toURI().toString()
        project.repositories.add(
            project.repositories.maven {
                it.setUrl(mavenRepoUrl)
            }
        )
        project.configurations.create("compile")
        project.dependencies.add("compile", "group:name:1.0.0")
    }

    @Test
    fun `generate HTML Report`() {

        val outputFile = File(reportFolder, "licenses.html")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = true
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
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
                    "</html>"
            )
        )
    }

    @Test
    fun `generate HTML Report wih custom CSS`() {

        val outputFile = File(reportFolder, "licenses.html")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = true
                it.html.stylesheet("body{}")
            }
        }

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
                    "</html>"
            )
        )
    }

    @Test
    fun `generate JSON Report`() {

        val outputFile = File(reportFolder, "licenses.json")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.json.enabled = true
            }
        }

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
                                "name": "Some license",
                                "url": "http://website.tld/"
                            }
                        ]
                    }
                ]
                """.trimIndent()
            )
        )
    }

    @Test
    fun `generate XML Report`() {

        val outputFile = File(reportFolder, "licenses.xml")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.xml.enabled = true
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                """
                    <?xml version="1.0" encoding="UTF-8" ?>
                    <libraries xmlns="https://www.cmgapps.com" xsi:schemaLocation="https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                      <library id="group:name:1.0.0" version="1.0.0">
                        <name>
                          Fake dependency name
                        </name>
                        <description>
                          Fake dependency description
                        </description>
                        <licenses>
                          <license url="http://website.tld/">
                            <name>
                              Some license
                            </name>
                          </license>
                        </licenses>
                      </library>
                    </libraries>
                    
                """.trimIndent()
            )
        )
    }

    @Test
    fun `generate Markdown Report`() {
        val outputFile = File(reportFolder, "licenses.md")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.markdown.enabled = true
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "# Open source licenses\n" +
                    "### Notice for packages:\n" +
                    "Fake dependency name _1.0.0_:\n" +
                    "* Some license (http://website.tld/)\n" +
                    "\n"
            )
        )
    }

    @Test
    fun `generate Plain text Report`() {
        val outputFile = File(reportFolder, "licenses.txt")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.text.enabled = true
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Licenses\n" +
                    "└─ Fake dependency name:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/"
            )
        )
    }

    @Test
    fun `generate CSV Report`() {
        val outputFile = File(reportFolder, "licenses.csv")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.csv.enabled = true
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Name,Version,MavenCoordinates,Description,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,Some license,http://website.tld/\r\n"
            )
        )
    }

    @Test
    fun `generate custom Report`() {
        val outputFile = File(reportFolder, "licenses")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.custom.enabled = true
                it.custom.generate { list ->
                    list.joinToString { lib ->
                        lib.name ?: lib.mavenCoordinates.identifierWithoutVersion
                    }
                }
            }
        }

        task.licensesReport()

        assertThat(outputFile.readText(), `is`("Fake dependency name"))
    }

    @Test
    fun `generate HTML by default`() {
        val outputFile = File(reportFolder, "licenses.html")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java)

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
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
                    "</html>"
            )
        )
    }

    @Test
    fun `generate all reports`() {
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.csv.enabled = true
                it.custom.enabled = true
                it.custom.generate { list ->
                    list.joinToString { lib ->
                        lib.name ?: lib.mavenCoordinates.identifierWithoutVersion
                    }
                }
                it.html.enabled = true
                it.json.enabled = true
                it.markdown.enabled = true
                it.text.enabled = true
                it.xml.enabled = true
            }
        }

        task.licensesReport()

        assertThat(File(reportFolder).listFiles()?.size, `is`(7))
    }

    @Test
    fun `sort libraries`() {
        val outputFile = File(reportFolder, "licenses.txt")

        project.dependencies.add("compile", "group:another:2.0.0")
        project.dependencies.add("compile", "group:other:1.0.0")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = false
                it.text {
                    it.enabled = true
                    it.destination = outputFile
                }
            }
        }

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
                    "   └─ URL: http://website.tld/"
            )
        )
    }
}
