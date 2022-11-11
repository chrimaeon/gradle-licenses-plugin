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
            },
        )
        project.plugins.apply("java")

        project.configurations.create("api")
        project.configurations.create("compile")
        project.dependencies.add("implementation", "group:name:1.0.0")
    }

    @Test
    fun `generate HTML Report`() {
        val outputFile = File(reportFolder, "licenses.html")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = true
                it.html.useDarkMode.set(false)
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate HTML Report wih custom CSS`() {
        val outputFile = File(reportFolder, "licenses.html")

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = true
                it.html.stylesheet("body{}")
                it.html.useDarkMode.set(false)
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
                    "</html>",
            ),
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
                    
                """.trimIndent(),
            ),
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
                    "   └─ URL: http://website.tld/",
            ),
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
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/\r\n",
            ),
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

        project.dependencies.add("api", "group:another:2.0.0")
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
                    "   └─ URL: http://website.tld/",
            ),
        )
    }

    @Test
    fun `handle android project`() {
        val outputFile = File(reportFolder, "licenses.txt")

        val task = project.tasks.create("licensesReport", AndroidLicensesTask::class.java) { task ->
            task.buildType = "debug"
            task.variant = "google"
            task.productFlavors = listOf("google", "amazon")
            task.reports {
                it.html.enabled = false
                it.csv {
                    it.enabled = true
                    it.destination = outputFile
                }
            }
        }

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
    fun `handle multiplatform project`() {
        val outputFile = File(reportFolder, "licenses.csv")

        val task = project.tasks.create("licensesReport", KotlinMultiplatformTask::class.java) { task ->
            task.targetNames = listOf("jvm", "js")
            task.reports {
                it.html.enabled = false
                it.csv {
                    it.enabled = true
                    it.destination = outputFile
                }
            }
        }

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
    fun `handle parent version`() {
        val outputFile = File(reportFolder, "licenses.csv")

        project.dependencies.add(
            "implementation",
            "group:has-parent:1.0.0",
        )

        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.html.enabled = false
                it.csv {
                    it.enabled = true
                    it.destination = outputFile
                }
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/\r\n" +
                    "Has Parent,1.0.0,group:has-parent:1.0.0,,Apache-2.0,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt\r\n",
            ),
        )
    }
}
