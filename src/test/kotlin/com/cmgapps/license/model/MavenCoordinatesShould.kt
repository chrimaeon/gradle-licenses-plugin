/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import org.apache.maven.artifact.versioning.ComparableVersion
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class MavenCoordinatesShould {
    @Test
    fun `add version to toString`() {
        assertThat(
            MavenCoordinates(
                groupId = "groupId",
                artifactId = "artifactId",
                version = ComparableVersion("1.0.0"),
            ).toString(),
            `is`("groupId:artifactId:1.0.0"),
        )
    }

    @Test
    fun `omit version part if not defined`() {
        assertThat(
            MavenCoordinates(
                groupId = "groupId",
                artifactId = "artifactId",
                version = ComparableVersion(""),
            ).toString(),
            `is`("groupId:artifactId"),
        )
    }

    @Test
    fun `compare version descending`() {
        assertThat(
            MavenCoordinates(
                groupId = "groupId",
                artifactId = "artifactId",
                version = ComparableVersion("1.0"),
            ),
            greaterThan(
                MavenCoordinates(
                    groupId = "groupId",
                    artifactId = "artifactId",
                    version = ComparableVersion("2.0"),
                ),
            ),
        )
    }
}
