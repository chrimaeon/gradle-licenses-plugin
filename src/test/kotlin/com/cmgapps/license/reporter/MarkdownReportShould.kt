/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.model.PomLicense
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestSpdxIdRepository
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
                |* Apache and MIT lib
                |* Apache lib
                |```
                |Apache-2.0 LICENSE
                |```
                |
                |* Apache and MIT lib
                |```
                |MIT LICENSE
                |```
                |
                |* LGPL lib
                |```
                |LGPL-2.0 LICENSE
                |```
                |
                |* LGPL lib
                |```
                |LGPL-2.0+ LICENSE
                |```
                |
                |* LGPL lib
                |```
                |LGPL-2.0-only LICENSE
                |```
                |
                |* LGPL lib
                |```
                |LGPL-2.0-or-later LICENSE
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
                    PomLibrary(
                        name = "Lib with invalid license",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo", url = "https://www.license.foo"),
                            ),
                    ),
                MavenCoordinates("test.group2", "test.artifact2", "1.0") to
                    PomLibrary(
                        name = "Lib with invalid license 2",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo2", url = "https://www.license2.foo"),
                            ),
                    ),
                MavenCoordinates("test.group3", "test.artifact3", "1.0") to
                    PomLibrary(
                        name = "Lib with invalid license 3",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo2", url = "https://www.license2.foo"),
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
                    |https://www.license.foo
                    |```
                    |
                    |* Lib with invalid license 2
                    |* Lib with invalid license 3
                    |```
                    |foo2
                    |https://www.license2.foo
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
                    PomLibrary(
                        name = "Lib with invalid license",
                        description = null,
                        licenses =
                            setOf(
                                PomLicense(name = "foo", url = "https://www.license.foo"),
                            ),
                    ),
            ),
            logger,
        ).writeLicenses(outputStream)

        verify(logger).warn(
            """
               |No mapping found for license: 'foo' with url 'https://www.license.foo'
               |used by 'test.group:test.artifact:1.0'
               |
               |If it is a valid Open Source License, please report to
               |https://github.com/chrimaeon/gradle-licenses-plugin/issues/new?labels=missing+license&title=Missing+foo&body=Missing+license%0A%0A%60foo%60+with+url+%60https%3A%2F%2Fwww.license.foo%60
            """.trimMargin(),
        )
    }
}

private class TestMarkdownReport(
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    logger: Logger = Logging.getLogger("TestMarkdownReport"),
    project: Project = ProjectBuilder.builder().build(),
) : MarkdownReport(project.layout, project.tasks.register("licenseReport").get(), logger, TestSpdxIdRepository()) {
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
