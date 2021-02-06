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
import com.cmgapps.license.util.getFileContent
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class HtmlReportShould {

    @Test
    fun `generate HTML report`() {
        val result = HtmlReport(
            LibrariesHelper.libraries,
            null
        ).generate()

        assertThat(
            result,
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "<ul>" +
                    "<li>Test lib 1</li>" +
                    "</ul>" +
                    "<pre>" +
                    getFileContent("apache-2.0.txt") +
                    "</pre>" +
                    "</body>" +
                    "</html>"
            )
        )
    }
}
