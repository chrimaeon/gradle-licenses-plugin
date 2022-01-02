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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.apache.maven.artifact.versioning.ComparableVersion

@Serializable
data class License(val name: String, val url: String)

@Serializable
data class Library(
    val name: String,
    @Serializable(ComparableVersionSerializer::class)
    val version: ComparableVersion,
    val description: String?,
    val licenses: List<License>
) {
    companion object {
        @Suppress("FunctionName")
        @JvmStatic
        fun Comparator(): Comparator<Library> =
            Comparator.comparing(Library::name).thenComparing(Library::version, reverseOrder())
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
