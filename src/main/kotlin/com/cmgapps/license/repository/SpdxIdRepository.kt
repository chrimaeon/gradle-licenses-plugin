/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.repository

import com.cmgapps.gradle.spdx.SpdxId

interface SpdxIdRepository {
    fun getSpdxIds(
        url: String?,
        name: String?,
    ): List<SpdxId>

    fun SpdxId.licenseText(): String
}
