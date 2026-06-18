/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import kotlinx.serialization.Serializable
import org.apache.maven.artifact.versioning.ComparableVersion

@Serializable
data class MavenCoordinates(
    val groupId: String,
    val artifactId: String,
    val version: String,
) : Comparable<MavenCoordinates>,
    java.io.Serializable {
    override fun compareTo(other: MavenCoordinates): Int = COMPARATOR.compare(this, other)

    val identifierWithoutVersion = "$groupId:$artifactId"

    override fun toString(): String = identifierWithoutVersion + if (version.isNotEmpty()) ":$version" else ""

    fun pomCoordinate() = "$groupId:$artifactId:$version@pom"

    companion object {
        @JvmStatic
        private val COMPARATOR =
            compareBy(MavenCoordinates::groupId)
                .thenBy(MavenCoordinates::artifactId)
                .thenByDescending { ComparableVersion(it.version) }
    }
}

data class PomLicense(
    val name: String?,
    val url: String?,
) : java.io.Serializable

data class PomLibrary(
    val name: String?,
    val description: String?,
    val licenses: Set<PomLicense>,
) : java.io.Serializable
