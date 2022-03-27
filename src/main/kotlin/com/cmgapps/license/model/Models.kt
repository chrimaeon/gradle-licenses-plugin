/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.apache.maven.artifact.versioning.ComparableVersion

@Serializable
data class MavenCoordinates(
    val groupId: String,
    val artifactId: String,
    @Serializable(ComparableVersionSerializer::class) val version: ComparableVersion,
) : Comparable<MavenCoordinates> {

    override fun compareTo(other: MavenCoordinates): Int = COMPARATOR.compare(this, other)

    val identifierWithoutVersion = "$groupId:$artifactId"

    override fun toString(): String =
        "$groupId:$artifactId" + if (version.toString().isNotEmpty()) ":$version" else ""

    companion object {
        @JvmStatic
        private val COMPARATOR = Comparator
            .comparing(MavenCoordinates::groupId)
            .thenComparing(MavenCoordinates::artifactId)
            .thenComparing(MavenCoordinates::version, reverseOrder())
    }
}

@Serializable
data class License(val name: String, val url: String)

@Serializable
data class Library(
    val mavenCoordinates: MavenCoordinates,
    val name: String?,
    val description: String?,
    val licenses: List<License>,
) {
    companion object {
        @Suppress("FunctionName")
        @JvmStatic
        fun NameComparator(): Comparator<Library> =
            Comparator.comparing<Library, String> {
                it.name
                    ?: it.mavenCoordinates.identifierWithoutVersion
            }.thenComparing({ it.mavenCoordinates.version }, reverseOrder())

        @Suppress("FunctionName")
        @JvmStatic
        fun MavenCoordinatesComparator(): Comparator<Library> = Comparator.comparing { it.mavenCoordinates }
    }
}

object ComparableVersionSerializer : KSerializer<ComparableVersion> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ComparableVersion", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ComparableVersion {
        return ComparableVersion(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: ComparableVersion) {
        encoder.encodeString(value.toString())
    }
}
