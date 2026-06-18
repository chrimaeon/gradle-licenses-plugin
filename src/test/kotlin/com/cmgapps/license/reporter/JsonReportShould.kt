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
                |[
                |    {
                |        "mavenCoordinates": {
                |            "groupId": "test.apache.mit",
                |            "artifactId": "apache.mit.artifact",
                |            "version": "1.0"
                |        },
                |        "name": "Apache and MIT lib",
                |        "description": "Apache and MIT lib description",
                |        "licenses": [
                |            {
                |                "spdxLicenseIdentifier": "Apache-2.0",
                |                "name": "Apache License 2.0",
                |                "url": "https://spdx.org/licenses/Apache-2.0.html"
                |            },
                |            {
                |                "spdxLicenseIdentifier": "MIT",
                |                "name": "MIT License",
                |                "url": "https://spdx.org/licenses/MIT.html"
                |            }
                |        ]
                |    },
                |    {
                |        "mavenCoordinates": {
                |            "groupId": "apache.test",
                |            "artifactId": "lib.artifact",
                |            "version": "2.3.4"
                |        },
                |        "name": "Apache lib",
                |        "description": "Apache lib description",
                |        "licenses": [
                |            {
                |                "spdxLicenseIdentifier": "Apache-2.0",
                |                "name": "Apache License 2.0",
                |                "url": "https://spdx.org/licenses/Apache-2.0.html"
                |            }
                |        ]
                |    },
                |    {
                |        "mavenCoordinates": {
                |            "groupId": "lgpl.test",
                |            "artifactId": "artifact.lib",
                |            "version": "5.6"
                |        },
                |        "name": "LGPL lib",
                |        "description": "LGPL lib description",
                |        "licenses": [
                |            {
                |                "spdxLicenseIdentifier": "LGPL-2.0",
                |                "name": "GNU Library General Public License v2 only",
                |                "url": "https://spdx.org/licenses/LGPL-2.0.html"
                |            },
                |            {
                |                "spdxLicenseIdentifier": "LGPL-2.0+",
                |                "name": "GNU Library General Public License v2 or later",
                |                "url": "https://spdx.org/licenses/LGPL-2.0+.html"
                |            },
                |            {
                |                "spdxLicenseIdentifier": "LGPL-2.0-only",
                |                "name": "GNU Library General Public License v2 only",
                |                "url": "https://spdx.org/licenses/LGPL-2.0-only.html"
                |            },
               |            {
                |                "spdxLicenseIdentifier": "LGPL-2.0-or-later",
                |                "name": "GNU Library General Public License v2 or later",
                |                "url": "https://spdx.org/licenses/LGPL-2.0-or-later.html"
                |            }
                |        ]
                |    }
                |]
                """.trimMargin(),
            ),
        )
    }
}

private class TestJsonReport(
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    project: Project = ProjectBuilder.builder().build(),
) : JsonReport(project.layout, project.tasks.register("licenseReport").get(), TestSpdxIdRepository()) {
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
