/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.util.OutputStreamExtension
import com.cmgapps.license.util.TestStream
import com.cmgapps.license.util.asString
import com.cmgapps.license.util.testLibraries
import org.apache.maven.artifact.versioning.ComparableVersion
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
                    "Test lib 1," +
                    "1.0," +
                    "test.group:test.artifact:1.0," +
                    "proper description," +
                    "Apache-2.0,Apache 2.0," +
                    "https://www.apache.org/licenses/LICENSE-2.0.txt" +
                    "\r\n" +
                    "Test lib 1," +
                    "1.0," +
                    "test.group:test.artifact:1.0," +
                    "proper description," +
                    "MIT," +
                    "MIT License,https://opensource.org/licenses/MIT" +
                    "\r\n" +
                    "Test lib 2," +
                    "2.3.4," +
                    "group.test2:artifact:2.3.4," +
                    "descriptions of lib 2," +
                    "Apache-2.0,\"The Apache Software License, Version 2.0\"," +
                    "https://www.apache.org/licenses/LICENSE-2.0.txt" +
                    "\r\n",
            ),
        )
    }

    @Test
    fun `escape strings in report`() {
        val license = License(LicenseId.UNKNOWN, "License name with a \" in it", "just a plain url")
        val library =
            Library(
                MavenCoordinates("groupC", "articfactA", ComparableVersion("version with a \n in it")),
                name = "Name with a , in it",
                description = "description with \r in it",
                listOf(license),
            )

        TestCsvReport(listOf(library)).writeLicenses(outputStream)

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
    override var libraries: List<Library>,
    project: Project = ProjectBuilder.builder().build().project,
) : CsvReport(
        project,
        project.tasks.register("licensesTask").get(),
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
