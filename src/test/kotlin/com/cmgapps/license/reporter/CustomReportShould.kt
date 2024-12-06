/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.testLibraries
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property
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
        object : CustomReport(project, project.task("licenseReport")) {
            override var libraries: List<Library> = testLibraries

            override fun getRequired(): Property<Boolean> = project.objects.property<Boolean>()

            override fun getOutputLocation(): RegularFileProperty = project.objects.fileProperty()
        }.apply {
            generator.set { libs ->
                libs.joinToString()
            }
        }.writeLicenses(outputStream)

        assertThat(
            outputStream.asString(),
            `is`(
                "Library(" +
                    "mavenCoordinates=test.group:test.artifact:1.0," +
                    " name=Test lib 1," +
                    " description=proper description," +
                    " licenses=[" +
                    "License(id=APACHE, name=Apache 2.0, " +
                    "url=https://www.apache.org/licenses/LICENSE-2.0.txt)," +
                    " License(id=MIT, name=MIT License, " +
                    "url=https://opensource.org/licenses/MIT)" +
                    "]), " +
                    "Library(" +
                    "mavenCoordinates=group.test2:artifact:2.3.4," +
                    " name=Test lib 2," +
                    " description=descriptions of lib 2," +
                    " licenses=[" +
                    "License(id=APACHE, name=The Apache Software License, Version 2.0," +
                    " url=https://www.apache.org/licenses/LICENSE-2.0.txt)" +
                    "])",
            ),
        )
    }
}
