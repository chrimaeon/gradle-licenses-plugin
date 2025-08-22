/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
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
class XmlReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun generateReport() {
        TestXmlReport(testLibraries).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                <?xml version="1.0" encoding="UTF-8" ?>
                <libraries xmlns="https://www.cmgapps.com" xsi:schemaLocation="https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                  <library id="test.group:test.artifact:1.0" version="1.0">
                    <name>
                      Test lib 1
                    </name>
                    <description>
                      proper description
                    </description>
                    <licenses>
                      <license spdx-license-identifier="Apache-2.0" url="https://www.apache.org/licenses/LICENSE-2.0.txt">
                        <name>
                          Apache 2.0
                        </name>
                      </license>
                      <license spdx-license-identifier="MIT" url="https://opensource.org/licenses/MIT">
                        <name>
                          MIT License
                        </name>
                      </license>
                    </licenses>
                  </library>
                  <library id="group.test2:artifact:2.3.4" version="2.3.4">
                    <name>
                      Test lib 2
                    </name>
                    <description>
                      descriptions of lib 2
                    </description>
                    <licenses>
                      <license spdx-license-identifier="Apache-2.0" url="https://www.apache.org/licenses/LICENSE-2.0.txt">
                        <name>
                          The Apache Software License, Version 2.0
                        </name>
                      </license>
                    </licenses>
                  </library>
                </libraries>

                """.trimIndent(),
            ),
        )
    }
}

private class TestXmlReport(
    override var libraries: List<Library>,
    project: Project = ProjectBuilder.builder().build(),
) : XmlReport(project, project.tasks.register("licenseReport").get()) {
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
