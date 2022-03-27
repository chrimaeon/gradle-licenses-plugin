/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.LicensesHelper
import com.cmgapps.license.helper.getLicenseText
import com.cmgapps.license.helper.logLicenseWarning
import com.cmgapps.license.helper.toLicensesMap
import com.cmgapps.license.model.Library
import org.gradle.api.logging.Logger

internal class MarkdownReport(
    libraries: List<Library>,
    private val logger: Logger,
) : Report(libraries) {
    override fun generate() = buildString {
        append("# Open source licenses\n")
        append("## Notice for packages")
        libraries.toLicensesMap().forEach { (license, libraries) ->
            libraries.asSequence().sortedBy { it.name }.forEach { library ->
                append('\n')
                append("* ")
                append(library.name ?: library.mavenCoordinates.identifierWithoutVersion)
            }

            val licenseUrl = license.url
            val licenseName = license.name

            append("\n```\n")
            when {
                LicensesHelper.LICENSE_MAP.containsKey(licenseUrl) -> {
                    append(LicensesHelper.LICENSE_MAP[licenseUrl].getLicenseText() ?: "")
                }
                LicensesHelper.LICENSE_MAP.containsKey(licenseName) -> {
                    append(LicensesHelper.LICENSE_MAP[licenseName].getLicenseText() ?: "")
                }
                else -> {
                    logger.logLicenseWarning(license, libraries)
                    append(licenseName)
                    append("\n")
                    append(licenseUrl)
                }
            }
            append("\n```\n")
        }
    }
}
