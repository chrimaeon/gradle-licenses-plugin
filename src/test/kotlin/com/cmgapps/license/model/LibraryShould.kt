/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
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
                MavenCoordinates("lib.group", "my.artifact", ComparableVersion("1.0.0-alpha-4")),
                name = "Lib name",
                description = "description",
                licenses = listOf(License("License name", "http://domain.com"))
            )
        )
        assertThat(
            jsonString,
            `is`(
                "{" +
                    "\"mavenCoordinates\":{" +
                    "\"groupId\":\"lib.group\"," +
                    "\"artifactId\":\"my.artifact\"," +
                    "\"version\":\"1.0.0-alpha-4\"" +
                    "}," +
                    "\"name\":\"Lib name\"," +
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
                    "\"mavenCoordinates\":{" +
                    "\"groupId\":\"lib.group\"," +
                    "\"artifactId\":\"my.artifact\"," +
                    "\"version\":\"1.0.0-alpha-4\"" +
                    "}," +
                    "\"name\":\"Lib name\"," +
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
                    MavenCoordinates("lib.group", "my.artifact", ComparableVersion("1.0.0-alpha-4")),
                    name = "Lib name",
                    description = "description",
                    licenses = listOf(License("License name", "http://domain.com"))
                )
            )
        )
    }

    @Test
    fun `round trip from class`() {
        val lib = Library(
            MavenCoordinates("lib.group", "my.artifact", ComparableVersion("1.0.0-alpha-4")),
            name = "Lib name",
            description = "description",
            licenses = listOf(License("License name", "http://domain.com"))
        )
        assertThat(json.decodeFromString<Library>(json.encodeToString(lib)), `is`(lib))
    }

    @Test
    fun `round trip from string`() {
        val lib = "{" +
            "\"mavenCoordinates\":{" +
            "\"groupId\":\"lib.group\"," +
            "\"artifactId\":\"my.artifact\"," +
            "\"version\":\"1.0.0-alpha-4\"" +
            "}," +
            "\"name\":\"Lib name\"," +
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
            Library(
                MavenCoordinates("groupB", "articfactA", ComparableVersion("1.0")),
                name = "B",
                description = null,
                licenses = emptyList(),
            ),
            Library(
                MavenCoordinates("groupA", "articfactA", ComparableVersion("1.0")),
                name = "A",
                description = "desc",
                licenses = emptyList()
            ),
            Library(
                MavenCoordinates("groupA", "articfactC", ComparableVersion("2.0")),
                name = "A",
                description = null,
                licenses = emptyList(),
            ),
            Library(
                MavenCoordinates("groupA", "articfactB", ComparableVersion("0.3-alpha4")),
                name = "A",
                description = null,
                licenses = emptyList()
            ),
            Library(
                MavenCoordinates("groupC", "articfactA", ComparableVersion("13")),
                name = "C",
                description = "desc",
                licenses = listOf(
                    License(
                        "license",
                        "http://domain.tld"
                    )
                )
            ),
        ).sortedWith(Library.NameComparator())
        assertThat(
            sortedList,
            contains(
                Library(
                    MavenCoordinates("groupA", "articfactC", ComparableVersion("2.0")),
                    name = "A",
                    description = null,
                    licenses = emptyList(),
                ),
                Library(
                    MavenCoordinates("groupA", "articfactA", ComparableVersion("1.0")),
                    name = "A",
                    description = "desc",
                    licenses = emptyList()
                ),
                Library(
                    MavenCoordinates("groupA", "articfactB", ComparableVersion("0.3-alpha4")),
                    name = "A",
                    description = null,
                    licenses = emptyList()
                ),
                Library(
                    MavenCoordinates("groupB", "articfactA", ComparableVersion("1.0")),
                    name = "B",
                    description = null,
                    licenses = emptyList(),
                ),
                Library(
                    MavenCoordinates("groupC", "articfactA", ComparableVersion("13")),
                    name = "C",
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

    @Test
    fun `sort by mavenCoordinates`() {
        val sortedList = listOf(
            Library(
                MavenCoordinates("groupB", "articfactA", ComparableVersion("1.0")),
                name = "B",
                description = null,
                licenses = emptyList(),
            ),
            Library(
                MavenCoordinates("groupA", "articfactA", ComparableVersion("1.0")),
                name = "A",
                description = "desc",
                licenses = emptyList()
            ),
            Library(
                MavenCoordinates("groupA", "articfactC", ComparableVersion("2.0")),
                name = "A",
                description = null,
                licenses = emptyList(),
            ),
            Library(
                MavenCoordinates("groupA", "articfactC", ComparableVersion("3.0.1")),
                name = "A",
                description = null,
                licenses = emptyList(),
            ),
            Library(
                MavenCoordinates("groupA", "articfactB", ComparableVersion("0.3-alpha4")),
                name = "A",
                description = null,
                licenses = emptyList()
            ),
            Library(
                MavenCoordinates("groupC", "articfactA", ComparableVersion("13")),
                name = "C",
                description = "desc",
                licenses = listOf(
                    License(
                        "license",
                        "http://domain.tld"
                    )
                )
            ),
        ).sortedWith(Library.MavenCoordinatesComparator())
        assertThat(
            sortedList,
            contains(
                Library(
                    MavenCoordinates("groupA", "articfactA", ComparableVersion("1.0")),
                    name = "A",
                    description = "desc",
                    licenses = emptyList()
                ),
                Library(
                    MavenCoordinates("groupA", "articfactB", ComparableVersion("0.3-alpha4")),
                    name = "A",
                    description = null,
                    licenses = emptyList()
                ),
                Library(
                    MavenCoordinates("groupA", "articfactC", ComparableVersion("3.0.1")),
                    name = "A",
                    description = null,
                    licenses = emptyList(),
                ),
                Library(
                    MavenCoordinates("groupA", "articfactC", ComparableVersion("2.0")),
                    name = "A",
                    description = null,
                    licenses = emptyList(),
                ),
                Library(
                    MavenCoordinates("groupB", "articfactA", ComparableVersion("1.0")),
                    name = "B",
                    description = null,
                    licenses = emptyList(),
                ),
                Library(
                    MavenCoordinates("groupC", "articfactA", ComparableVersion("13")),
                    name = "C",
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
