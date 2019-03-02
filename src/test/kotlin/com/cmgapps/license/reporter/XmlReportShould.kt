/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.LibrariesHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

class XmlReportShould {

    @Test
    fun generateReport() {
        val report = XmlReport(LibrariesHelper.libraries).generate()
        assertThat(report, `is`("""<?xml version="1.0" encoding="UTF-8" ?>
                                  |<libraries>
                                  |  <library>
                                  |    <name>
                                  |      Test lib 1
                                  |    </name>
                                  |    <version>
                                  |      1.0
                                  |    </version>
                                  |    <description>
                                  |      proper description
                                  |    </description>
                                  |    <licenses>
                                  |      <license>
                                  |        <name>
                                  |          Apache 2.0
                                  |        </name>
                                  |        <url>
                                  |          http://www.apache.org/licenses/LICENSE-2.0.txt
                                  |        </url>
                                  |      </license>
                                  |      <license>
                                  |        <name>
                                  |          MIT License
                                  |        </name>
                                  |        <url>
                                  |          http://opensource.org/licenses/MIT
                                  |        </url>
                                  |      </license>
                                  |    </licenses>
                                  |  </library>
                                  |</libraries>
                                  |""".trimMargin()))
    }
}