/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle.spdx.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class SpdxLicensesJson(
    val licenses: List<SpdxLicenseJson>,
) {
    override fun toString(): String = "SpdxLicenses(licenses=$licenses)"
}

@Serializable
internal class SpdxLicenseJson(
    @SerialName("licenseId") val id: String,
    val name: String,
    @SerialName("reference") val spdxUrl: String,
    @SerialName("seeAlso") val otherUrls: List<String>,
    @SerialName("detailsUrl") val detailsUrl: String,
)
