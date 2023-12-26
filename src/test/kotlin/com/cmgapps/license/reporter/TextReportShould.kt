/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.util.testLibraries
import org.apache.maven.artifact.versioning.ComparableVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class TextReportShould {
    @Test
    fun `generate Text report`() {
        val report = TextReport(testLibraries).generate()
        assertThat(
            report,
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
        val report =
            TextReport(
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
            ).generate()
        assertThat(
            report,
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
