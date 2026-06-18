/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.util

import com.cmgapps.gradle.spdx.SpdxId
import com.cmgapps.gradle.spdx.SpdxId.Companion.Apache_20
import com.cmgapps.gradle.spdx.SpdxId.Companion.LGPL_20
import com.cmgapps.gradle.spdx.SpdxId.Companion.LGPL_20Plus
import com.cmgapps.gradle.spdx.SpdxId.Companion.LGPL_20_only
import com.cmgapps.gradle.spdx.SpdxId.Companion.LGPL_20_or_later
import com.cmgapps.gradle.spdx.SpdxId.Companion.MIT
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import com.cmgapps.license.model.PomLicense
import com.cmgapps.license.repository.SpdxIdRepository

internal val testLibraries =
    mapOf(
        MavenCoordinates("test.apache.mit", "apache.mit.artifact", "1.0") to
            PomLibrary(
                "Apache and MIT lib",
                "Apache and MIT lib description",
                setOf(
                    PomLicense(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt",
                    ),
                    PomLicense(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT",
                    ),
                ),
            ),
        MavenCoordinates("apache.test", "lib.artifact", "2.3.4") to
            PomLibrary(
                "Apache lib",
                "Apache lib description",
                setOf(
                    PomLicense(
                        name = "The Apache Software License, Version 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt",
                    ),
                ),
            ),
        MavenCoordinates("lgpl.test", "artifact.lib", "5.6") to
            PomLibrary(
                "LGPL lib",
                "LGPL lib description",
                setOf(
                    PomLicense(
                        name = "LGPL-2.0",
                        url = "https://www.gnu.org/licenses/old-licenses/lgpl-2.0-standalone.html",
                    ),
                ),
            ),
    )

class TestSpdxIdRepository : SpdxIdRepository {
    override fun getSpdxIds(
        url: String?,
        name: String?,
    ): List<SpdxId> =
        when {
            url?.equals("https://www.apache.org/licenses/LICENSE-2.0.txt") ?: false -> {
                listOf(Apache_20)
            }

            url?.equals("https://opensource.org/licenses/MIT") ?: false -> {
                listOf(MIT)
            }

            url?.equals("https://www.gnu.org/licenses/old-licenses/lgpl-2.0-standalone.html") ?: false -> {
                listOf(
                    LGPL_20,
                    LGPL_20Plus,
                    LGPL_20_only,
                    LGPL_20_or_later,
                )
            }

            else -> {
                emptyList()
            }
        }

    override fun SpdxId.licenseText(): String = "${this.id} LICENSE"
}
