/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
                    │  ├─ URL: http://www.apache.org/licenses/LICENSE-2.0.txt
                    │  ├─ License: MIT License
                    │  └─ URL: http://opensource.org/licenses/MIT
                    └─ Test lib 2:2.3.4
                       ├─ License: Apache 2.0
                       └─ URL: http://www.apache.org/licenses/LICENSE-2.0.txt
                """.trimIndent()
            )
        )
    }
}
