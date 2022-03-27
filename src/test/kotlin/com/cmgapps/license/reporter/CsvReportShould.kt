/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.LibrariesHelper
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.MavenCoordinates
import org.apache.maven.artifact.versioning.ComparableVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class CsvReportShould {

    @Test
    fun `generate report`() {
        val result = CsvReport(LibrariesHelper.libraries).generate()
        assertThat(
            result,
            `is`(
                "Name,Version,MavenCoordinates,Description,License Name,License Url\r\n" +
                    "Test lib 1,1.0,test.group:test.artifact:1.0,proper description,Apache 2.0,https://www.apache.org/licenses/LICENSE-2.0.txt\r\n" +
                    "Test lib 2,2.3.4,group.test2:artifact:2.3.4,descriptions of lib 2,Apache 2.0,https://www.apache.org/licenses/LICENSE-2.0.txt\r\n"

            )
        )
    }

    @Test
    fun `escape strings in report`() {
        val license = License("License name with a \" in it", "just a plain url")
        val library =
            Library(
                MavenCoordinates("groupC", "articfactA", ComparableVersion("version with a \n in it")),
                name = "Name with a , in it",
                description = "description with \r in it", listOf(license)
            )
        val result = CsvReport(listOf(library)).generate()

        assertThat(
            result,
            `is`(
                "Name,Version,MavenCoordinates,Description,License Name,License Url\r\n" +
                    "\"Name with a , in it\",\"version with a \n in it\",\"groupC:articfactA:version with a \n in it\",\"description with \r in it\",\"License name with a \"\" in it\",just a plain url\r\n"
            )
        )
    }
}
