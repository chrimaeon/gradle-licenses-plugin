/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
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
        reportFolder = "${testProjectDir}/build/reports/licenses/licenseReport"
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
}