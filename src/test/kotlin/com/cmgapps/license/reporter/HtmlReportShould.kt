/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.gradle.spdx.SpdxId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.model.PomLicense
import com.cmgapps.license.repository.SpdxIdRepository
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.testLibraries
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
import com.cmgapps.license.model.PomLibrary as LibraryModel

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
                    "<li>Apache and MIT lib</li>" +
                    "<li>Apache lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "Apache-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>Apache and MIT lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "MIT LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0+ LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-only LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-or-later LICENSE" +
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
                    "<li>Apache and MIT lib</li>" +
                    "<li>Apache lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "Apache-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>Apache and MIT lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "MIT LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0+ LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-only LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-or-later LICENSE" +
                    "</pre>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate HTML report with unknown licenses`() {
        TestHtmlReport(
            testLibraries +
                mapOf(
                    MavenCoordinates("test.group.invalid", "test.artifact", "1.0") to
                        LibraryModel(
                            name = "Lib with invalid license",
                            description = null,
                            licenses =
                                setOf(
                                    PomLicense(name = "foo", url = "https://www.license.foo"),
                                ),
                        ),
                    MavenCoordinates("test.group2.invalid", "test.artifact2", "1.0") to
                        LibraryModel(
                            name = "Lib with invalid license 2",
                            description = null,
                            licenses =
                                setOf(
                                    PomLicense(name = "foo2", url = "https://www.license2.foo"),
                                ),
                        ),
                    MavenCoordinates("test.group3.invalid", "test.artifact3", "1.0") to
                        LibraryModel(
                            name = "Lib with invalid license 3",
                            description = null,
                            licenses =
                                setOf(
                                    PomLicense(name = "foo2", url = "https://www.license2.foo"),
                                ),
                        ),
                ),
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
                    "<li>Apache and MIT lib</li>" +
                    "<li>Apache lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "Apache-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>Apache and MIT lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "MIT LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0 LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0+ LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-only LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>LGPL lib</li>" +
                    "</ul>" +
                    "<pre>" +
                    "LGPL-2.0-or-later LICENSE" +
                    "</pre>" +
                    "<ul>" +
                    "<li>Lib with invalid license</li>" +
                    "</ul>" +
                    "<div class=\"license\"><p>foo</p><a href=\"https://www.license.foo\">https://www.license.foo</a></div>" +
                    "<ul>" +
                    "<li>Lib with invalid license 2</li>" +
                    "<li>Lib with invalid license 3</li>" +
                    "</ul>" +
                    "<div class=\"license\"><p>foo2</p><a href=\"https://www.license2.foo\">https://www.license2.foo</a></div>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `report library without matching license`() {
        val logger = mock<Logger>()

        TestHtmlReport(
            mapOf(
                MavenCoordinates("test.group", "test.artifact", "1.0") to
                    LibraryModel(
                        name = "Lib with invalid license",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo", url = "https://www.license.foo"),
                            ),
                    ),
                MavenCoordinates("test.group2", "test.artifact2", "1.0") to
                    LibraryModel(
                        name = "Lib with invalid license 2",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo2", url = "https://www.license2.foo"),
                            ),
                    ),
                MavenCoordinates("test.group3", "test.artifact3", "1.0") to
                    LibraryModel(
                        name = "Lib with invalid license 3",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo2", url = "https://www.license2.foo"),
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

        verify(logger).warn(
            """
               |No mapping found for license: 'foo2' with url 'https://www.license2.foo'
               |used by 'test.group2:test.artifact2:1.0', 'test.group3:test.artifact3:1.0'
               |
               |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
            """.trimMargin(),
        )
    }
}

private class TestHtmlSpdxIdRepository : SpdxIdRepository {
    override fun getSpdxIds(
        url: String?,
        name: String?,
    ): List<SpdxId> =
        when {
            url?.contains("apache") ?: false -> listOf(SpdxId.Apache_20)
            url?.contains("mit") ?: false -> listOf(SpdxId.MIT)
            else -> emptyList()
        }

    override fun SpdxId.licenseText(): String = "$id LICENSE"
}

private class TestHtmlReport(
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    logger: Logger = Logging.getLogger("TestHtmlReport"),
    project: Project = ProjectBuilder.builder().build(),
) : HtmlReport(
        logger = logger,
        objects =
            project
                .objects,
        layout = project.layout,
        task = project.tasks.register("licenseReport").get(),
        spdxIdRepository = TestHtmlSpdxIdRepository(),
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
