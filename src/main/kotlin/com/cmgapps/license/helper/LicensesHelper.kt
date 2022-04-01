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
        LicenseId.UNKNOWN -> ""
        LicenseId.EPL_2 -> "epl-2.0.txt"
        LicenseId.GPL_2 -> "gpl-2.0.txt"
        LicenseId.GPL_3 -> "gpl-3.0.txt"
        LicenseId.LGPL_2_1 -> "lgpl-2.1.txt"
        LicenseId.LGPL_3 -> "lgpl-3.0.txt"
        LicenseId.MIT -> "mit.txt"
        LicenseId.MPL_2 -> "mpl-2.0.txt"
    }

internal val LicenseId.text: String?
    get() = this::class.java.getResource("/licenses/${this.filename}")?.readText()

/**
 * Map License name or URL to license id.
 *
 * Based on "popular and widely-used or with strong communities" found here: https://opensource.org/licenses/category.
 * License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
 */
@JvmField
internal val LICENSE_MAP = mapOf(
    // Apache License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/apache-2.0.txt
    "Apache-2.0" to LicenseId.APACHE,
    "Apache 2.0" to LicenseId.APACHE,
    "Apache v2" to LicenseId.APACHE,
    "Apache License 2.0" to LicenseId.APACHE,
    "Apache License, Version 2.0" to LicenseId.APACHE,
    "The Apache Software License" to LicenseId.APACHE,
    "The Apache Software License, Version 2.0" to LicenseId.APACHE,
    "http://www.apache.org/licenses/LICENSE-2.0.txt" to LicenseId.APACHE,
    "https://www.apache.org/licenses/LICENSE-2.0.txt" to LicenseId.APACHE,
    "http://opensource.org/licenses/Apache-2.0" to LicenseId.APACHE,
    "https://opensource.org/licenses/Apache-2.0" to LicenseId.APACHE,
    "http://www.apache.org/licenses/LICENSE-2.0" to LicenseId.APACHE,
    "https://www.apache.org/licenses/LICENSE-2.0" to LicenseId.APACHE,

    // BSD 2-Clause "Simplified" License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-2-clause.txt
    "BSD-2-Clause" to LicenseId.BSD_2,
    "BSD 2-Clause \"Simplified\" License" to LicenseId.BSD_2,
    "http://opensource.org/licenses/BSD-2-Clause" to LicenseId.BSD_2,
    "https://opensource.org/licenses/BSD-2-Clause" to LicenseId.BSD_2,

    // BSD 3-Clause "New" or "Revised" License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-3-clause.txt
    "BSD-3-Clause" to LicenseId.BSD_3,
    "BSD 3-Clause \"New\" or \"Revised\" License" to LicenseId.BSD_3,
    "http://opensource.org/licenses/BSD-3-Clause" to LicenseId.BSD_3,
    "https://opensource.org/licenses/BSD-3-Clause" to LicenseId.BSD_3,

    // Eclipse Public License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-2.0.txt
    "EPL-2.0" to LicenseId.EPL_2,
    "Eclipse Public License 2.0" to LicenseId.EPL_2,
    "http://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to LicenseId.EPL_2,
    "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to LicenseId.EPL_2,
    "http://opensource.org/licenses/EPL-2.0" to LicenseId.EPL_2,
    "https://opensource.org/licenses/EPL-2.0" to LicenseId.EPL_2,

    // GNU General Public License v2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-2.0.txt
    "GPL-2.0" to LicenseId.GPL_2,
    "GNU General Public License v2.0" to LicenseId.GPL_2,
    "http://www.gnu.org/licenses/gpl-2.0.txt" to LicenseId.GPL_2,
    "https://www.gnu.org/licenses/gpl-2.0.txt" to LicenseId.GPL_2,
    "http://opensource.org/licenses/GPL-2.0" to LicenseId.GPL_2,
    "https://opensource.org/licenses/GPL-2.0" to LicenseId.GPL_2,

    // GNU General Public License v3.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-3.0.txt
    "GPL-3.0" to LicenseId.GPL_3,
    "GNU General Public License v3.0" to LicenseId.GPL_3,
    "https//www.gnu.org/licenses/gpl-3.0.txt" to LicenseId.GPL_3,
    "https://www.gnu.org/licenses/gpl-3.0.txt" to LicenseId.GPL_3,
    "http://opensource.org/licenses/GPL-3.0" to LicenseId.GPL_3,
    "https://opensource.org/licenses/GPL-3.0" to LicenseId.GPL_3,

    // GNU Lesser General Public License v2.1
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-2.1.txt
    "LGPL-2.1" to LicenseId.LGPL_2_1,
    "GNU Lesser General Public License v2.1" to LicenseId.LGPL_2_1,
    "http://www.gnu.org/licenses/lgpl-2.1.txt" to LicenseId.LGPL_2_1,
    "https://www.gnu.org/licenses/lgpl-2.1.txt" to LicenseId.LGPL_2_1,
    "http://opensource.org/licenses/LGPL-2.1" to LicenseId.LGPL_2_1,
    "https://opensource.org/licenses/LGPL-2.1" to LicenseId.LGPL_2_1,

    // GNU Lesser General Public License v3.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-3.0.txt
    "LGPL-3.0" to LicenseId.LGPL_3,
    "GNU Lesser General Public License v3.0" to LicenseId.LGPL_3,
    "http://www.gnu.org/licenses/lgpl-3.0.txt" to LicenseId.LGPL_3,
    "https://www.gnu.org/licenses/lgpl-3.0.txt" to LicenseId.LGPL_3,
    "http://opensource.org/licenses/LGPL-3.0" to LicenseId.LGPL_3,
    "https://opensource.org/licenses/LGPL-3.0" to LicenseId.LGPL_3,

    // MIT License
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit.txt
    "MIT" to LicenseId.MIT,
    "MIT License" to LicenseId.MIT,
    "http://opensource.org/licenses/MIT" to LicenseId.MIT,
    "https://opensource.org/licenses/MIT" to LicenseId.MIT,

    // Mozilla Public License 2.0
    // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mpl-2.0.txt
    "MPL-2.0" to LicenseId.MPL_2,
    "Mozilla Public License 2.0" to LicenseId.MPL_2,
    "http://www.mozilla.org/media/MPL/2.0/index.txt" to LicenseId.MPL_2,
    "https://www.mozilla.org/media/MPL/2.0/index.txt" to LicenseId.MPL_2,
    "http://opensource.org/licenses/MPL-2.0" to LicenseId.MPL_2,
    "https://opensource.org/licenses/MPL-2.0" to LicenseId.MPL_2,

    // Common Development and Distribution License 1.0
    "CDDL-1.0" to LicenseId.CDDL,
    "Common Development and Distribution License 1.0" to LicenseId.CDDL,
    "http://opensource.org/licenses/cddl1" to LicenseId.CDDL,
    "https://opensource.org/licenses/cddl1" to LicenseId.CDDL
)

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
