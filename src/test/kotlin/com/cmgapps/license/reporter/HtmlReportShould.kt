/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.getFileContent
import com.cmgapps.license.util.testLibraries
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.ByteArrayOutputStream
import com.cmgapps.license.model.Library as LibraryModel

@ExtendWith(OutputStreamExtension::class)
class HtmlReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun `generate HTML report without dark mode`() {
        TestHtmlReport(
            testLibraries,
        ).apply {
            useDarkMode.set(false)
        }.writeLicenses(outputStream)

        assertThat(
            outputStream.asString(),
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
                    "<ul>" +
                    "<li>Test lib 1</li>" +
                    "<li>Test lib 2</li>" +
                    "</ul>" +
                    "<pre>" +
                    getFileContent("apache-2.0.txt") +
                    "</pre>" +
                    "<ul>" +
                    "<li>Test lib 1</li>" +
                    "</ul>" +
                    "<pre>" +
                    getFileContent("mit.txt") +
                    "</pre>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate HTML report with dark mode`() {
        TestHtmlReport(
            testLibraries,
        ).writeLicenses(outputStream)

        assertThat(
            outputStream.asString(),
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
                    "<ul>" +
                    "<li>Test lib 1</li>" +
                    "<li>Test lib 2</li>" +
                    "</ul>" +
                    "<pre>" +
                    getFileContent("apache-2.0.txt") +
                    "</pre>" +
                    "<ul>" +
                    "<li>Test lib 1</li>" +
                    "</ul>" +
                    "<pre>" +
                    getFileContent("mit.txt") +
                    "</pre>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `report library without matching license`() {
        val logger = mock<Logger>()

        TestHtmlReport(
            listOf(
                LibraryModel(
                    MavenCoordinates("test.group", "test.artifact", ComparableVersion("1.0")),
                    name = "Lib with invalid license",
                    description = null,
                    licenses =
                        listOf(
                            License(LicenseId.UNKNOWN, name = "foo", url = "https://www.license.foo"),
                        ),
                ),
            ),
            logger = logger,
        ).writeLicenses(outputStream)

        verify(logger).warn(
            """
               |No mapping found for license: 'foo' with url 'https://www.license.foo'
               |used by 'test.group:test.artifact:1.0'
               |
               |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
            """.trimMargin(),
        )
    }
}

private class TestHtmlReport(
    override var libraries: List<Library>,
    logger: Logger = Logging.getLogger("TestHtmlReport"),
    project: Project = ProjectBuilder.builder().build(),
) : HtmlReport(
        logger = logger,
        objects =
            project
                .objects,
        project = project,
        task = project.task("licenseReport"),
    ) {
    override fun getRequired(): Property<Boolean> =
        ProjectBuilder
            .builder()
            .build()
            .objects
            .property(Boolean::class.java)

    override fun getOutputLocation(): RegularFileProperty =
        ProjectBuilder
            .builder()
            .build()
            .objects
            .fileProperty()
}
