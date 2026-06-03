/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import kotlinx.serialization.json.Json
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LibraryShould {
    private lateinit var json: Json

    @BeforeEach
    fun beforeEach() {
        json = Json { prettyPrint = false }
    }

    @Test
    fun serialize() {
        val jsonString =
            json.encodeToString(
                Library(
                    name = "Lib name",
                    description = "description",
                    licenses = setOf(License(LicenseId.UNKNOWN, "License name", "https://domain.com")),
                ),
            )
        assertThat(
            jsonString,
            `is`(
                "{" +
                    "\"name\":\"Lib name\"," +
                    "\"description\":\"description\"," +
                    "\"licenses\":[" +
                    "{" +
                    "\"spdxLicenseIdentifier\":null," +
                    "\"name\":\"License name\"," +
                    "\"url\":\"https://domain.com\"" +
                    "}" +
                    "]" +
                    "}",
            ),
        )
    }

    @Test
    fun deserialize() {
        val lib: Library =
            json.decodeFromString(
                // language=json
                """
                    |{
                    |  "name": "Lib name",
                    |  "description": "description",
                    |  "licenses": [
                    |    {
                    |      "spdxLicenseIdentifier": null,
                    |      "name":"License name",
                    |      "url":"https://domain.com"
                    |    }
                    |  ]
                    |}
                """.trimMargin(),
            )
        assertThat(
            lib,
            `is`(
                Library(
                    name = "Lib name",
                    description = "description",
                    licenses = setOf(License(LicenseId.UNKNOWN, "License name", "https://domain.com")),
                ),
            ),
        )
    }

    @Test
    fun `round trip from class`() {
        val lib =
            Library(
                name = "Lib name",
                description = "description",
                licenses = setOf(License(LicenseId.UNKNOWN, "License name", "https://domain.com")),
            )
        assertThat(json.decodeFromString<Library>(json.encodeToString(lib)), `is`(lib))
    }

    @Test
    fun `round trip from string`() {
        val lib =
            "{" +
                "\"name\":\"Lib name\"," +
                "\"description\":\"description\"," +
                "\"licenses\":[" +
                "{" +
                "\"spdxLicenseIdentifier\":null," +
                "\"name\":\"License name\"," +
                "\"url\":\"https://domain.com\"" +
                "}" +
                "]" +
                "}"
        assertThat(json.encodeToString(json.decodeFromString<Library>(lib)), `is`(lib))
    }
}
