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

@Suppress("HttpUrlsUsage")
@ExtendWith(OutputStreamExtension::class)
class MarkdownReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun `generate Markdown report`() {
        TestMarkdownReport(testLibraries).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                    |# Open source licenses
                    |## Notice for packages
                    |* Test lib 1
                    |* Test lib 2
                    |```
                    |${getFileContent("apache-2.0.txt")}
                    |```
                    |
                    |* Test lib 1
                    |```
                    |${getFileContent("mit.txt")}
                    |```
                    |
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate Markdown report for libs without matching license`() {
        TestMarkdownReport(
            mapOf(
                MavenCoordinates("test.group", "test.artifact", "1.0") to
                    Library(
                        name = "Lib with invalid license",
                        description = null,
                        licenses =
                            setOf(
                                License(LicenseId.UNKNOWN, name = "foo", url = "http://www.license.foo"),
                            ),
                    ),
                MavenCoordinates("test.group2", "test.artifact2", "1.0") to
                    Library(
                        name = "Lib with invalid license 2",
                        description = null,
                        licenses =
                            setOf(
                                License(LicenseId.UNKNOWN, name = "foo2", url = "http://www.license2.foo"),
                            ),
                    ),
                MavenCoordinates("test.group3", "test.artifact3", "1.0") to
                    Library(
                        name = "Lib with invalid license 3",
                        description = null,
                        licenses =
                            setOf(
                                License(LicenseId.UNKNOWN, name = "foo2", url = "http://www.license2.foo"),
                            ),
                    ),
            ),
        ).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                    |# Open source licenses
                    |## Notice for packages
                    |* Lib with invalid license
                    |```
                    |foo
                    |http://www.license.foo
                    |```
                    |
                    |* Lib with invalid license 2
                    |* Lib with invalid license 3
                    |```
                    |foo2
                    |http://www.license2.foo
                    |```
                    |
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `report library without matching license`() {
        val logger = mock<Logger>()

        TestMarkdownReport(
            mapOf(
                MavenCoordinates("test.group", "test.artifact", "1.0") to
                    Library(
                        name = "Lib with invalid license",
                        description = null,
                        licenses =
                            setOf(
                                License(LicenseId.UNKNOWN, name = "foo", url = "http://www.license.foo"),
                            ),
                    ),
            ),
            logger,
        ).writeLicenses(outputStream)

        verify(logger).warn(
            """
               |No mapping found for license: 'foo' with url 'http://www.license.foo'
               |used by 'test.group:test.artifact:1.0'
               |
               |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
            """.trimMargin(),
        )
    }
}

private class TestMarkdownReport(
    override var libraries: Map<MavenCoordinates, Library>,
    logger: Logger = Logging.getLogger("TestMarkdownReport"),
    project: Project = ProjectBuilder.builder().build(),
) : MarkdownReport(project.layout, project.tasks.register("licenseReport").get(), logger) {
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
