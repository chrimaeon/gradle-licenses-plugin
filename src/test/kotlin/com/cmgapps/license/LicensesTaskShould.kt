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
        reportFolder = "$testProjectDir/build/reports/licenses/licenseReport"
        project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir.toFile())
            .build()
    }

    @Test
    fun `generate HTML Report`() {

        val outputFile = File(reportFolder, "licensesReport.html")

        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.HTML
            task.outputFile = outputFile
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "</body>" +
            "</html>"))
    }

    @Test
    fun `generate HTML Report wih custom CSS`() {

        val outputFile = File(reportFolder, "licensesReport.html")

        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.HTML
            task.outputFile = outputFile
            task.bodyCss = "BODY CSS"
            task.preCss = "PRE CSS"
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>BODY CSSPRE CSS</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "</body>" +
            "</html>"))
    }

    @Test
    fun `generate JSON Report`() {

        val outputFile = File(reportFolder, "licensesReport.json")

        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.JSON
            task.outputFile = outputFile
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("[]"))
    }

    @Test
    fun `generate XML Report`() {

        val outputFile = File(reportFolder, "licensesReport.xml")

        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.XML
            task.outputFile = outputFile
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("""<?xml version="1.0" encoding="UTF-8" ?>
            |<libraries/>
            |""".trimMargin()))
    }

    @Test
    fun `generate Markdown Report`() {
        val outputFile = File(reportFolder, "licensesReport.md")
        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.MD
            task.outputFile = outputFile
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("""# Open source licenses
            |### Notice for packages:
            |""".trimMargin()))
    }

    @Test
    fun `generate Plain text Report`() {
        val outputFile = File(reportFolder, "licensesReport.txt")
        project.tasks.create("licensesReport", LicensesTask::class.java) { task ->
            task.outputType = OutputType.TEXT
            task.outputFile = outputFile
        }

        val task = project.tasks.getByName("licensesReport") as LicensesTask
        task.licensesReport()

        assertThat(outputFile.readText(), `is`("""""".trimMargin()))
    }
}
