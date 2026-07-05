/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.helper

import com.cmgapps.gradle.spdx.SpdxId
import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.model.PomLibrary
import org.gradle.api.logging.Logger
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private fun unknown(
    name: String,
    url: String,
) = SpdxId(id = "UNKNOWN", name = name, url = url, detailsUrl = "")

internal fun Map<MavenCoordinates, PomLibrary>.toLicensesMap(): Map<SpdxId, List<Pair<MavenCoordinates, PomLibrary>>> =
    this
        .asSequence()
        .flatMap { (coordinates, library) ->
            library.licenses.flatMap { license ->
                val spdxIds =
                    SpdxId.getSpdxIds(
                        license.url,
                        license.name,
                    )
                if (spdxIds.isEmpty()) {
                    listOf(unknown(name = license.name ?: "", url = license.url ?: "") to (coordinates to library))
                } else {
                    spdxIds.map {
                        it to (coordinates to library)
                    }
                }
            }
        }.groupBy({ (license, _) -> license }, { (_, pair) -> pair })

internal fun Logger.logLicenseWarning(libraries: List<Pair<MavenCoordinates, PomLibrary>>) {
    val license =
        libraries
            .firstOrNull()
            ?.second
            ?.licenses
            ?.firstOrNull() ?: return

    val issuesTrackerUrl =
        buildString {
            append("https://github.com/chrimaeon/gradle-licenses-plugin/issues/new?labels=")
            append(URLEncoder.encode("missing license", StandardCharsets.UTF_8))
            append("&title=Missing+")
            append(URLEncoder.encode(license.name ?: license.url ?: "", StandardCharsets.UTF_8))
            append("&body=")
            append(
                URLEncoder.encode(
                    "Missing license\n\n`${license.name}` with url `${license.url}`",
                    StandardCharsets.UTF_8,
                ),
            )
        }

    this.warn(
        """
        |No mapping found for license: '${license.name}' with url '${license.url}'
        |used by ${libraries.map { it.first }.joinToString { "'$it'" }}
        |
        |If it is a valid Open Source License, please report to
        |$issuesTrackerUrl
        """.trimMargin(),
    )
}
