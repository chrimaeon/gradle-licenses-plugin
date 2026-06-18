/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestSpdxIdRepository
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
                |<?xml version="1.0" encoding="UTF-8" ?>
                |<libraries xmlns="https://www.cmgapps.com" xsi:schemaLocation="https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                |  <library id="test.apache.mit:apache.mit.artifact" version="1.0">
                |    <name>
                |      Apache and MIT lib
                |    </name>
                |    <description>
                |      Apache and MIT lib description
                |    </description>
                |    <licenses>
                |      <license spdx-license-identifier="Apache-2.0" url="https://spdx.org/licenses/Apache-2.0.html">
                |        <name>
                |          Apache License 2.0
                |        </name>
                |      </license>
                |      <license spdx-license-identifier="MIT" url="https://spdx.org/licenses/MIT.html">
                |        <name>
                |          MIT License
                |        </name>
                |      </license>
                |    </licenses>
                |  </library>
                |  <library id="apache.test:lib.artifact" version="2.3.4">
                |    <name>
                |      Apache lib
                |    </name>
                |    <description>
                |      Apache lib description
                |    </description>
                |    <licenses>
                |      <license spdx-license-identifier="Apache-2.0" url="https://spdx.org/licenses/Apache-2.0.html">
                |        <name>
                |          Apache License 2.0
                |        </name>
                |      </license>
                |    </licenses>
                |  </library>
                |  <library id="lgpl.test:artifact.lib" version="5.6">
                |    <name>
                |      LGPL lib
                |    </name>
                |    <description>
                |      LGPL lib description
                |    </description>
                |    <licenses>
                |      <license spdx-license-identifier="LGPL-2.0" url="https://spdx.org/licenses/LGPL-2.0.html">
                |        <name>
                |          GNU Library General Public License v2 only
                |        </name>
                |      </license>
                |      <license spdx-license-identifier="LGPL-2.0+" url="https://spdx.org/licenses/LGPL-2.0+.html">
                |        <name>
                |          GNU Library General Public License v2 or later
                |        </name>
                |      </license>
                |      <license spdx-license-identifier="LGPL-2.0-only" url="https://spdx.org/licenses/LGPL-2.0-only.html">
                |        <name>
                |          GNU Library General Public License v2 only
                |        </name>
                |      </license>
                |      <license spdx-license-identifier="LGPL-2.0-or-later" url="https://spdx.org/licenses/LGPL-2.0-or-later.html">
                |        <name>
                |          GNU Library General Public License v2 or later
                |        </name>
                |      </license>
                |    </licenses>
                |  </library>
                |</libraries>
                |
                """.trimMargin(),
            ),
        )
    }
}

private class TestXmlReport(
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    project: Project = ProjectBuilder.builder().build(),
) : XmlReport(project.layout, project.tasks.register("licenseReport").get(), TestSpdxIdRepository()) {
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
