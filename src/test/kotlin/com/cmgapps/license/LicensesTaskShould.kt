/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
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
                it.html.enabled.set(true)
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
                it.html.enabled.set(true)
                it.html.stylesheet.set(project.resources.text.fromString("body{}"))
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
                it.json.enabled.set(true)
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                """
                [
                    {
                        "name": "Fake dependency name",
                        "version": "1.0.0",
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
                it.xml.enabled.set(true)
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                    "<libraries>\n" +
                    "  <library>\n" +
                    "    <name>\n" +
                    "      Fake dependency name\n" +
                    "    </name>\n" +
                    "    <version>\n" +
                    "      1.0.0\n" +
                    "    </version>\n" +
                    "    <description>\n" +
                    "      Fake dependency description\n" +
                    "    </description>\n" +
                    "    <licenses>\n" +
                    "      <license>\n" +
                    "        <name>\n" +
                    "          Some license\n" +
                    "        </name>\n" +
                    "        <url>\n" +
                    "          http://website.tld/\n" +
                    "        </url>\n" +
                    "      </license>\n" +
                    "    </licenses>\n" +
                    "  </library>\n" +
                    "</libraries>\n"
            )
        )
    }

    @Test
    fun `generate Markdown Report`() {
        val outputFile = File(reportFolder, "licenses.md")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.markdown.enabled.set(true)
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
                it.text.enabled.set(true)
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
                it.csv.enabled.set(true)
            }
        }

        task.licensesReport()

        assertThat(
            outputFile.readText(),
            `is`(
                "Name,Version,Description,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,Fake dependency description,Some license,http://website.tld/\r\n"
            )
        )
    }

    @Test
    fun `generate custom Report`() {
        val outputFile = File(reportFolder, "licenses")
        val task = project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.reports {
                it.custom.enabled.set(true)
                it.custom.generate { list -> list.joinToString { lib -> lib.name } }
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
                it.csv.enabled.set(true)
                it.custom.enabled.set(true)
                it.custom.generate { list -> list.joinToString { lib -> lib.name } }
                it.html.enabled.set(true)
                it.json.enabled.set(true)
                it.markdown.enabled.set(true)
                it.text.enabled.set(true)
                it.xml.enabled.set(true)
            }
        }

        task.licensesReport()

        assertThat(File(reportFolder).listFiles()?.size, `is`(7))
    }
}
