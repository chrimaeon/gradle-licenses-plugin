/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.testLibraries
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class JsonReportShould {

    @Test
    fun generateReport() {
        val report = JsonReport(testLibraries).generate()
        assertThat(
            report,
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
                """.trimIndent()
            )
        )
    }
}
