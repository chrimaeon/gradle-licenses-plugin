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
class JsonReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun generateReport() {
        TestJsonReport(testLibraries).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                """
                [
                    {
                        "mavenCoordinates": {
                            "groupId": "test.group",
                            "artifactId": "test.artifact",
                            "version": "1.0"
                        },
                        "name": "Test lib 1",
                        "description": "proper description",
                        "licenses": [
                            {
                                "spdxLicenseIdentifier": "Apache-2.0",
                                "name": "Apache 2.0",
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt"
                            },
                            {
                                "spdxLicenseIdentifier": "MIT",
                                "name": "MIT License",
                                "url": "https://opensource.org/licenses/MIT"
                            }
                        ]
                    },
                    {
                        "mavenCoordinates": {
                            "groupId": "group.test2",
                            "artifactId": "artifact",
                            "version": "2.3.4"
                        },
                        "name": "Test lib 2",
                        "description": "descriptions of lib 2",
                        "licenses": [
                            {
                                "spdxLicenseIdentifier": "Apache-2.0",
                                "name": "The Apache Software License, Version 2.0",
                                "url": "https://www.apache.org/licenses/LICENSE-2.0.txt"
                            }
                        ]
                    }
                ]
                """.trimIndent(),
            ),
        )
    }
}

private class TestJsonReport(
    override var libraries: List<Library>,
    project: Project = ProjectBuilder.builder().build(),
) : JsonReport(project, project.task("licenseReport")) {
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
