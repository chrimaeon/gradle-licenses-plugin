/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.LibrariesHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class TextReportShould {

    @Test
    fun `generate Text report`() {
        val report = TextReport(LibrariesHelper.libraries).generate()
        assertThat(
            report,
            `is`(
                """
                    Licenses
                    ├─ Test lib 1:1.0
                    │  ├─ License: Apache 2.0
                    │  ├─ URL: https://www.apache.org/licenses/LICENSE-2.0.txt
                    │  ├─ License: MIT License
                    │  └─ URL: https://opensource.org/licenses/MIT
                    └─ Test lib 2:2.3.4
                       ├─ License: Apache 2.0
                       └─ URL: https://www.apache.org/licenses/LICENSE-2.0.txt
                """.trimIndent()
            )
        )
    }
}
