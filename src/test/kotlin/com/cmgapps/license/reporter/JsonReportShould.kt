/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.LibrariesHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test

class JsonReportShould {

    @Test
    fun generateReport() {
        val report = JsonReport(LibrariesHelper.libraries).generate()
        assertThat(report, `is`("""[
                                  |  {
                                  |    "description": "proper description",
                                  |    "licenses": [
                                  |      {
                                  |        "name": "Apache 2.0",
                                  |        "url": "http://www.apache.org/licenses/LICENSE-2.0.txt"
                                  |      },
                                  |      {
                                  |        "name": "MIT License",
                                  |        "url": "http://opensource.org/licenses/MIT"
                                  |      }
                                  |    ],
                                  |    "name": "Test lib 1",
                                  |    "version": "1.0"
                                  |  }
                                  |]""".trimMargin()))
    }
}