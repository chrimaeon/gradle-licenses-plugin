/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@kotlinx.serialization.Serializable
@SerialName("license")
private class LicenseSurrogate(val spdxLicenseIdentifier: String?, val name: String, val url: String)

object LicenseSerializer : KSerializer<License> {

    override val descriptor: SerialDescriptor = LicenseSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: License) {
        val surrogate = LicenseSurrogate(
            spdxLicenseIdentifier = value.id.spdxLicenseIdentifier,
            name = value.name,
            url = value.url,
        )

        encoder.encodeSerializableValue(LicenseSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): License {
        val surrogate = decoder.decodeSerializableValue(LicenseSurrogate.serializer())
        return License(
            id = LicenseId.fromSpdxLicenseIdentifier(surrogate.spdxLicenseIdentifier),
            name = surrogate.name,
            url = surrogate.url,
        )
    }
}
