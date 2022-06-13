/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("HttpUrlsUsage")

package com.cmgapps.license.helper

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import org.gradle.api.logging.Logger

internal val LicenseId.filename: String
    get() = when (this) {
        LicenseId.APACHE -> "apache-2.0.txt"
        LicenseId.BSD_2 -> "bsd-2-clause.txt"
        LicenseId.BSD_3 -> "bsd-3-clause.txt"
        LicenseId.CDDL -> "cddl.txt"
        LicenseId.EPL_2 -> "epl-2.0.txt"
        LicenseId.GPL_2 -> "gpl-2.0.txt"
        LicenseId.GPL_3 -> "gpl-3.0.txt"
        LicenseId.LGPL_2_1 -> "lgpl-2.1.txt"
        LicenseId.LGPL_3 -> "lgpl-3.0.txt"
        LicenseId.MIT -> "mit.txt"
        LicenseId.MPL_2 -> "mpl-2.0.txt"
        else -> throw IllegalArgumentException("$this does not have a file associated")
    }

internal val LicenseId.text: String
    get() {
        if (this == LicenseId.UNKNOWN) {
            return ""
        }

        return this::class.java.getResource("/licenses/${this.filename}")!!.readText()
    }

@OptIn(ExperimentalStdlibApi::class)
fun List<Library>.toLicensesMap(): Map<License, List<Library>> = buildMap<License, MutableList<Library>> {
    this@toLicensesMap.forEach { library ->
        library.licenses.forEach { license ->
            get(license)?.add(library) ?: put(license, mutableListOf(library))
        }
    }
}

fun Logger.logLicenseWarning(license: License, libraries: List<Library>) = this.warn(
    """
        |No mapping found for license: '${license.name}' with url '${license.url}'
        |used by ${libraries.joinToString { "'${it.mavenCoordinates}'" }}
        |
        |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
    """.trimMargin()
)
