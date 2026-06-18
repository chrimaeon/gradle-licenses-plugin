/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestSpdxIdRepository
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.testLibraries
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.ByteArrayOutputStream

@ExtendWith(OutputStreamExtension::class)
class CustomReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun `generate report`() {
        val project = ProjectBuilder.builder().build()
        object : CustomReport(
            project.layout,
            project.tasks.register("licenseReport").get(),
            project.objects,
            TestSpdxIdRepository(),
        ) {
            override var libraries = testLibraries

            override fun getRequired(): Property<Boolean> = project.objects.property(Boolean::class.java)

            override fun getOutputLocation(): RegularFileProperty = project.objects.fileProperty()
        }.apply {
            generator.set { libraries ->
                libraries
                    .map { (coordinates, library) ->
                        "Library(mavenCoordinates=$coordinates, name=${library.name}, description=${library.description}, licenses=${library.licenses})"
                    }.joinToString()
            }
        }.writeLicenses(outputStream)

        assertThat(
            outputStream.asString(),
            `is`(
                "Library(mavenCoordinates=test.apache.mit:apache.mit.artifact:1.0, name=Apache and MIT lib, description=Apache and MIT lib description, licenses=[License(id=Apache-2.0, name=Apache 2.0, url=https://www.apache.org/licenses/LICENSE-2.0.txt), License(id=MIT, name=MIT License, url=https://opensource.org/licenses/MIT)]), Library(mavenCoordinates=apache.test:lib.artifact:2.3.4, name=Apache lib, description=Apache lib description, licenses=[License(id=Apache-2.0, name=The Apache Software License, Version 2.0, url=https://www.apache.org/licenses/LICENSE-2.0.txt)]), Library(mavenCoordinates=lgpl.test:artifact.lib:5.6, name=LGPL lib, description=LGPL lib description, licenses=[License(id=LGPL-2.0, name=LGPL-2.0, url=https://www.gnu.org/licenses/old-licenses/lgpl-2.0-standalone.html)])",
            ),
        )
    }
}
