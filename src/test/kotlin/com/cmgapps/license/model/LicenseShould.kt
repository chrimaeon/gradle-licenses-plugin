/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class LicenseShould {
    @Test
    fun `check license with same id but different name`() {
        val result =
            License(LicenseId.APACHE, name = "Apache License 2.0", url = "http://www.apache.com") ==
                License(
                    LicenseId.APACHE,
                    name = "The Apache License 2.0",
                    url = "http://www.theapache.com",
                )
        assertThat(
            """
            License(
                 LicenseId.APACHE,
                 name = "Apache License 2.0",
                 url = "http://www.apache.com"
            )
            
            equals
            
            License(
                LicenseId.APACHE,
                name = "The Apache License 2.0",
                url = "http://www.theapache.com",
            )
            """.trimIndent(),
            result,
            `is`(true),
        )
    }

    @Test
    fun `check license with same id but different url`() {
        assertThat(
            """
            License(
              LicenseId.APACHE,
              name = "Apache License 2.0",
              url = "http://www.apache.com",
            ) 
            ==
            License(
                LicenseId.APACHE,
                name = "Apache License 2.0",
                url = "http://www.theapache.com",
            )
            """.trimIndent(),
            License(
                LicenseId.APACHE,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ) ==
                License(
                    LicenseId.APACHE,
                    name = "Apache License 2.0",
                    url = "http://www.theapache.com",
                ),
        )
    }

    @Test
    fun `check unknown license by name and url`() {
        assertThat(
            """
            License(
                LicenseId.UNKNOWN,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ) 
            ==
            License(
                LicenseId.UNKNOWN,
                name = "Apache License 2.0",
                url = "http://www.apache.com"
            )
            """.trimIndent(),
            License(
                LicenseId.UNKNOWN,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ) ==
                License(
                    LicenseId.UNKNOWN,
                    name = "Apache License 2.0",
                    url = "http://www.apache.com",
                ),
        )
    }

    @Test
    fun `use id as hashcode for non UNKNOWN licenses`() {
        assertThat(
            """
            License(
                LicenseId.APACHE,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ).hashCode()
             
            ==
             
            License(
                LicenseId.APACHE,
                name = "The Apache License 2.0",
                url = "http://www.the apache.com",
            ).hashCode()
            """.trimIndent(),
            License(
                LicenseId.APACHE,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ).hashCode() ==
                License(
                    LicenseId.APACHE,
                    name = "The Apache License 2.0",
                    url = "http://www.the apache.com",
                ).hashCode(),
        )
    }

    @Test
    fun `use all properties as hashcode for UNKNOWN licenses`() {
        assertThat(
            """
            License(
                LicenseId.UNKNOWN,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ).hashCode()
             
            !=
             
            License(
                LicenseId.UNKNOWN,
                name = "The Apache License 2.0",
                url = "http://www.theapache.com",
            ).hashCode()
            """.trimIndent(),
            License(
                LicenseId.UNKNOWN,
                name = "Apache License 2.0",
                url = "http://www.apache.com",
            ).hashCode() !=
                License(
                    LicenseId.UNKNOWN,
                    name = "The Apache License 2.0",
                    url = "http://www.theapache.com",
                ).hashCode(),
        )
    }
}
