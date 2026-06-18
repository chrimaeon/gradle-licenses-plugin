/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestSpdxIdRepository
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.testLibraries
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
            outputStream.toString(),
            `is`(
                """
                |Licenses
                |├─ Apache and MIT lib:1.0
                |│  ├─ License: Apache License 2.0
                |│  ├─ SPDX-License-Identifier: Apache-2.0
                |│  ├─ URL: https://spdx.org/licenses/Apache-2.0.html
                |│  ├─ License: MIT License
                |│  ├─ SPDX-License-Identifier: MIT
                |│  └─ URL: https://spdx.org/licenses/MIT.html
                |├─ Apache lib:2.3.4
                |│  ├─ License: Apache License 2.0
                |│  ├─ SPDX-License-Identifier: Apache-2.0
                |│  └─ URL: https://spdx.org/licenses/Apache-2.0.html
                |└─ LGPL lib:5.6
                |   ├─ License: GNU Library General Public License v2 only
                |   ├─ SPDX-License-Identifier: LGPL-2.0
                |   ├─ URL: https://spdx.org/licenses/LGPL-2.0.html
                |   ├─ License: GNU Library General Public License v2 or later
                |   ├─ SPDX-License-Identifier: LGPL-2.0+
                |   ├─ URL: https://spdx.org/licenses/LGPL-2.0+.html
                |   ├─ License: GNU Library General Public License v2 only
                |   ├─ SPDX-License-Identifier: LGPL-2.0-only
                |   ├─ URL: https://spdx.org/licenses/LGPL-2.0-only.html
                |   ├─ License: GNU Library General Public License v2 or later
                |   ├─ SPDX-License-Identifier: LGPL-2.0-or-later
                |   └─ URL: https://spdx.org/licenses/LGPL-2.0-or-later.html
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate report with undefined licenses`() {
        TestTextReport(
            mapOf(
                MavenCoordinates("test.group", "test.artifact", "1.0") to
                    PomLibrary(
                        "Test lib 1",
                        "proper description",
                        emptySet(),
                    ),
                MavenCoordinates("group.test2", "artifact", "2.3.4") to
                    PomLibrary(
                        "Test lib 2",
                        "descriptions of lib 2",
                        emptySet(),
                    ),
            ),
        ).writeLicenses(outputStream)
        assertThat(
            outputStream.toString(),
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
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    project: Project = ProjectBuilder.builder().build(),
) : TextReport(project.layout, project.tasks.register("licenseReport").get(), TestSpdxIdRepository()) {
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
