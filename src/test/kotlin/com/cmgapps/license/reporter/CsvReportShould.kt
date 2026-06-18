/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.model.PomLicense
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
class CsvReportShould {
    @TestStream
    lateinit var outputStream: ByteArrayOutputStream

    @Test
    fun `generate report`() {
        TestCsvReport(testLibraries).writeLicenses(outputStream)
        assertThat(
            outputStream.asString(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Apache and MIT lib," +
                    "1.0," +
                    "test.apache.mit:apache.mit.artifact:1.0," +
                    "Apache and MIT lib description," +
                    "Apache-2.0," +
                    "Apache License 2.0," +
                    "https://spdx.org/licenses/Apache-2.0.html" +
                    "\r\n" +
                    "Apache and MIT lib," +
                    "1.0," +
                    "test.apache.mit:apache.mit.artifact:1.0," +
                    "Apache and MIT lib description," +
                    "MIT," +
                    "MIT License," +
                    "https://spdx.org/licenses/MIT.html" +
                    "\r\n" +
                    "Apache lib," +
                    "2.3.4," +
                    "apache.test:lib.artifact:2.3.4," +
                    "Apache lib description," +
                    "Apache-2.0," +
                    "Apache License 2.0," +
                    "https://spdx.org/licenses/Apache-2.0.html" +
                    "\r\n" +
                    "LGPL lib," +
                    "5.6," +
                    "lgpl.test:artifact.lib:5.6," +
                    "LGPL lib description," +
                    "LGPL-2.0," +
                    "GNU Library General Public License v2 only," +
                    "https://spdx.org/licenses/LGPL-2.0.html" +
                    "\r\n" +
                    "LGPL lib," +
                    "5.6," +
                    "lgpl.test:artifact.lib:5.6," +
                    "LGPL lib description," +
                    "LGPL-2.0+," +
                    "GNU Library General Public License v2 or later," +
                    "https://spdx.org/licenses/LGPL-2.0+.html" +
                    "\r\n" +
                    "LGPL lib," +
                    "5.6," +
                    "lgpl.test:artifact.lib:5.6," +
                    "LGPL lib description," +
                    "LGPL-2.0-only," +
                    "GNU Library General Public License v2 only," +
                    "https://spdx.org/licenses/LGPL-2.0-only.html" +
                    "\r\n" +
                    "LGPL lib," +
                    "5.6," +
                    "lgpl.test:artifact.lib:5.6," +
                    "LGPL lib description," +
                    "LGPL-2.0-or-later," +
                    "GNU Library General Public License v2 or later," +
                    "https://spdx.org/licenses/LGPL-2.0-or-later.html" +
                    "\r\n",
            ),
        )
    }

    @Test
    fun `escape strings in report`() {
        val license = PomLicense(name = "License name with a \" in it", url = "just a plain url")
        val pomLibrary =
            PomLibrary(
                name = "Name with a , in it",
                description = "description with \r in it",
                setOf(license),
            )

        TestCsvReport(
            mapOf(
                MavenCoordinates(
                    "groupC",
                    "articfactA",
                    "version with a \n in it",
                ) to pomLibrary,
            ),
        ).writeLicenses(outputStream)

        assertThat(
            outputStream.asString(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "\"Name with a , in it\"," +
                    "\"version with a \n in it\"," +
                    "\"groupC:articfactA:version with a \n in it\"," +
                    "\"description with \r in it\"," +
                    "," +
                    "\"License name with a \"\" in it\"," +
                    "just a plain url" +
                    "\r\n",
            ),
        )
    }
}

private class TestCsvReport(
    override var libraries: Map<MavenCoordinates, PomLibrary>,
    project: Project = ProjectBuilder.builder().build().project,
) : CsvReport(
        project.layout,
        project.tasks.register("licensesTask").get(),
        TestSpdxIdRepository(),
    ) {
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
