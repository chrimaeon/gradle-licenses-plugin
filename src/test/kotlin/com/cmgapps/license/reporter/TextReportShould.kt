/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.testLibraries
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.ByteArrayOutputStream

@ExtendWith(OutputStreamExtension::class)
class TextReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun `generate Text report`() {
        TestTextReport(testLibraries).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                Licenses
                ├─ Test lib 1:1.0
                │  ├─ License: Apache 2.0
                │  ├─ SPDX-License-Identifier: Apache-2.0
                │  ├─ URL: https://www.apache.org/licenses/LICENSE-2.0.txt
                │  ├─ License: MIT License
                │  ├─ SPDX-License-Identifier: MIT
                │  └─ URL: https://opensource.org/licenses/MIT
                └─ Test lib 2:2.3.4
                   ├─ License: The Apache Software License, Version 2.0
                   ├─ SPDX-License-Identifier: Apache-2.0
                   └─ URL: https://www.apache.org/licenses/LICENSE-2.0.txt
                """.trimIndent(),
            ),
        )
    }

    @Test
    fun `generate report with undefined licenses`() {
        TestTextReport(
            listOf(
                Library(
                    MavenCoordinates("test.group", "test.artifact", ComparableVersion("1.0")),
                    "Test lib 1",
                    "proper description",
                    emptyList(),
                ),
                Library(
                    MavenCoordinates("group.test2", "artifact", ComparableVersion("2.3.4")),
                    "Test lib 2",
                    "descriptions of lib 2",
                    emptyList(),
                ),
            ),
        ).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                Licenses
                ├─ Test lib 1:1.0
                │  └─ License: Undefined
                └─ Test lib 2:2.3.4
                   └─ License: Undefined
                """.trimIndent(),
            ),
        )
    }
}

private class TestTextReport(
    override var libraries: List<Library>,
    project: Project = ProjectBuilder.builder().build(),
) : TextReport(project, project.tasks.register("licenseReport").get()) {
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
