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
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class CsvReportShould {

    @Test
    fun `generate report`() {
        val result = CsvReport(LibrariesHelper.libraries).generate()
        assertThat(
            result, `is`(
                "name,version,description,license name,license url\r\n" +
                    "Test lib 1,1.0,proper description,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt\r\n"
            )
        )
    }

    @Test
    fun `escape strings in report`() {
        val license = License("License name with a \" in it", "just a plain url")
        val library =
            Library("Name with a , in it", "version with a \n in it", "description with \r in it", listOf(license))
        val result = CsvReport(listOf(library)).generate()

        assertThat(
            result, `is`(
                "name,version,description,license name,license url\r\n" +
                    "\"Name with a , in it\",\"version with a \n in it\",\"description with \r in it\",\"License name with a \"\" in it\",just a plain url\r\n"
            )
        )
    }
}
