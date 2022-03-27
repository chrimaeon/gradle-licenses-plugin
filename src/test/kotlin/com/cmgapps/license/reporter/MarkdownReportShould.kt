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

class MarkdownReportShould {

    @Test
    fun `generate Markdown report`() {
        val result = MarkdownReport(LibrariesHelper.libraries).generate()
        assertThat(
            result,
            `is`(
                """
                    # Open source licenses
                    ### Notice for packages:
                    Test lib 1 _1.0_:
                    * Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0.txt)
                    * MIT License (https://opensource.org/licenses/MIT)
                    
                    Test lib 2 _2.3.4_:
                    * Apache 2.0 (https://www.apache.org/licenses/LICENSE-2.0.txt)
                    
                    
                """.trimIndent()
            )
        )
    }
}
