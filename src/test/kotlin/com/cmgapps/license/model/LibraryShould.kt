/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.maven.artifact.versioning.ComparableVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
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
        val jsonString = json.encodeToString(
            Library(
                name = "Lib name",
                version = ComparableVersion("1.0.0-alpha-4"),
                description = "description",
                licenses = listOf(License("License name", "http://domain.com"))
            )
        )
        assertThat(
            jsonString,
            `is`(
                "{" +
                    "\"name\":\"Lib name\"," +
                    "\"version\":\"1.0.0-alpha-4\"," +
                    "\"description\":\"description\"," +
                    "\"licenses\":[" +
                    "{" +
                    "\"name\":\"License name\"," +
                    "\"url\":\"http://domain.com\"" +
                    "}" +
                    "]" +
                    "}"
            )
        )
    }

    @Test
    fun deserialize() {
        val lib: Library =
            json.decodeFromString(
                "{" +
                    "\"name\":\"Lib name\"," +
                    "\"version\":\"1.0.0-alpha-4\"," +
                    "\"description\":\"description\"," +
                    "\"licenses\":[" +
                    "{" +
                    "\"name\":\"License name\"," +
                    "\"url\":\"http://domain.com\"" +
                    "}" +
                    "]" +
                    "}"
            )
        assertThat(
            lib,
            `is`(
                Library(
                    name = "Lib name",
                    version = ComparableVersion("1.0.0-alpha-4"),
                    description = "description",
                    licenses = listOf(License("License name", "http://domain.com"))
                )
            )
        )
    }

    @Test
    fun `round trip from class`() {
        val lib = Library(
            name = "Lib name",
            version = ComparableVersion("1.0.0-alpha-4"),
            description = "description",
            licenses = listOf(License("License name", "http://domain.com"))
        )
        assertThat(json.decodeFromString<Library>(json.encodeToString(lib)), `is`(lib))
    }

    @Test
    fun `round trip from string`() {
        val lib = "{" +
            "\"name\":\"Lib name\"," +
            "\"version\":\"1.0.0-alpha-4\"," +
            "\"description\":\"description\"," +
            "\"licenses\":[" +
            "{" +
            "\"name\":\"License name\"," +
            "\"url\":\"http://domain.com\"" +
            "}" +
            "]" +
            "}"
        assertThat(json.encodeToString(json.decodeFromString<Library>(lib)), `is`(lib))
    }

    @Test
    fun `sort by name and version`() {
        val sortedList = listOf(
            Library("B", ComparableVersion("1.0"), description = null, licenses = emptyList()),
            Library("A", ComparableVersion("1.0"), description = "desc", licenses = emptyList()),
            Library("A", ComparableVersion("2.0"), description = null, licenses = emptyList()),
            Library("A", ComparableVersion("0.3-alpha4"), description = null, licenses = emptyList()),
            Library(
                "C",
                ComparableVersion("13"),
                description = "desc",
                licenses = listOf(
                    License(
                        "license",
                        "http://domain.tld"
                    )
                )
            ),
        ).sortedWith(Library.Comparator())
        assertThat(
            sortedList,
            contains(
                Library("A", ComparableVersion("2.0"), description = null, licenses = emptyList()),
                Library("A", ComparableVersion("1.0"), description = "desc", licenses = emptyList()),
                Library("A", ComparableVersion("0.3-alpha4"), description = null, licenses = emptyList()),
                Library("B", ComparableVersion("1.0"), description = null, licenses = emptyList()),
                Library(
                    "C",
                    ComparableVersion("13"),
                    description = "desc",
                    licenses = listOf(
                        License(
                            "license",
                            "http://domain.tld"
                        )
                    )
                ),
            )
        )
    }
}
