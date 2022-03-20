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

class XmlReportShould {

    @Test
    fun generateReport() {
        val report = XmlReport(LibrariesHelper.libraries).generate()
        assertThat(
            report,
            `is`(
                """
                    <?xml version="1.0" encoding="UTF-8" ?>
                    <libraries xmlns="https://www.cmgapps.com" xsi:schemaLocation="https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                      <library id="Test_lib_1_1.0" version="1.0">
                        <name>
                          Test lib 1
                        </name>
                        <description>
                          proper description
                        </description>
                        <licenses>
                          <license url="http://www.apache.org/licenses/LICENSE-2.0.txt">
                            <name>
                              Apache 2.0
                            </name>
                          </license>
                          <license url="http://opensource.org/licenses/MIT">
                            <name>
                              MIT License
                            </name>
                          </license>
                        </licenses>
                      </library>
                      <library id="Test_lib_2_2.3.4" version="2.3.4">
                        <name>
                          Test lib 2
                        </name>
                        <description>
                          descriptions of lib 2
                        </description>
                        <licenses>
                          <license url="http://www.apache.org/licenses/LICENSE-2.0.txt">
                            <name>
                              Apache 2.0
                            </name>
                          </license>
                        </licenses>
                      </library>
                    </libraries>

                """.trimIndent()
            )
        )
    }
}
