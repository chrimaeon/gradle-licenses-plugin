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
import org.apache.commons.csv.CSVFormat
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
        identifierWithoutVersion + if (version.toString().isNotEmpty()) ":$version" else ""

    companion object {
        @JvmStatic
        private val COMPARATOR = Comparator
            .comparing(MavenCoordinates::groupId)
            .thenComparing(MavenCoordinates::artifactId)
            .thenComparing(MavenCoordinates::version, reverseOrder())
    }
}

enum class LicenseId(val spdxLicenseIdentifier: String?) {
    APACHE("Apache-2.0"),
    BSD_2("BSD-2-Clause"),
    BSD_3("BSD-3-Clause"),
    CDDL("CDDL-1.0"),
    EPL_2("EPL-2.0"),
    GPL_2("GPL-2.0-only"),
    GPL_3("GPL-3.0-only"),
    LGPL_2_1("LGPL-2.1-only"),
    LGPL_3("LGPL-3.0-only"),
    MIT("MIT"),
    MPL_2("MPL-2.0"),
    UNKNOWN(null),
    ;

    companion object {
        @JvmStatic
        fun fromSpdxLicenseIdentifier(spdxLicenseIdentifier: String?): LicenseId = when (spdxLicenseIdentifier) {
            "Apache-2.0" -> APACHE
            "BSD-2-Clause" -> BSD_2
            "BSD-3-Clause" -> BSD_3
            "CDDL-1.0" -> CDDL
            "EPL-2.0" -> EPL_2
            "GPL-2.0-only" -> GPL_2
            "GPL-3.0-only" -> GPL_3
            "LGPL-2.1-only" -> LGPL_2_1
            "LGPL-3.0-only" -> LGPL_3
            "MIT" -> MIT
            "MPL-2.0" -> MPL_2
            else -> UNKNOWN
        }

        /**
         * Map License name or URL to license id.
         *
         * Based on "popular and widely-used or with strong communities" found here: https://opensource.org/licenses/category.
         * License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
         */
        internal val map: Map<String, LicenseId> by lazy {
            CSVFormat.DEFAULT.parse(this::class.java.getResourceAsStream("/license_map.csv")?.bufferedReader())
                .associate {
                    it.get(0) to LicenseId.valueOf(it.get(1))
                }
        }
    }
}

@Serializable(with = LicenseSerializer::class)
data class License(val id: LicenseId, val name: String, val url: String) {

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as License

        if (id != other.id) return false

        return true
    }
}

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
