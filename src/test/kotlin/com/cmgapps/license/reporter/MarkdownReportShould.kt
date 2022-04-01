/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.testLibraries
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.util.getFileContent
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class MarkdownReportShould {

    @Test
    fun `generate Markdown report`() {
        val logger: Logger = Logging.getLogger("TestLogger")

        val result = MarkdownReport(testLibraries, logger).generate()
        assertThat(
            result,
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
                """.trimMargin()
            )
        )
    }

    @Test
    fun `generate Markdown report for libs without matching license`() {
        val logger: Logger = Logging.getLogger("TestLogger")

        val result = MarkdownReport(
            listOf(
                Library(
                    MavenCoordinates("test.group", "test.artifact", ComparableVersion("1.0")),
                    name = "Lib with invalid license",
                    description = null,
                    licenses = listOf(
                        License(LicenseId.UNKNOWN, name = "foo", url = "http://www.license.foo")
                    ),
                )
            ),
            logger
        ).generate()
        assertThat(
            result,
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
                """.trimMargin()
            )
        )
    }

    @Test
    fun `report library without matching license`() {
        val logger = mock<Logger>()

        MarkdownReport(
            listOf(
                Library(
                    MavenCoordinates("test.group", "test.artifact", ComparableVersion("1.0")),
                    name = "Lib with invalid license",
                    description = null,
                    licenses = listOf(
                        License(LicenseId.UNKNOWN, name = "foo", url = "http://www.license.foo")
                    ),
                )
            ),
            logger
        ).generate()

        verify(logger).warn(
            """
               |No mapping found for license: 'foo' with url 'http://www.license.foo'
               |used by 'test.group:test.artifact:1.0'
               |
               |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
            """.trimMargin()
        )
    }
}
