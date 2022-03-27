/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library

internal class MarkdownReport(libraries: List<Library>) : Report(libraries) {
    override fun generate() = buildString {
        append("# Open source licenses\n")
        append("### Notice for packages:\n")
        libraries.forEach { library ->
            append(library.name)
            append(' ')
            append('_')
            append(library.mavenCoordinates.version)
            append("_:")
            library.licenses.forEach { license ->
                append("\n* ")
                append(license.name)
                append(" (")
                append(license.url)
                append(")")
            }
            append("\n\n")
        }
    }
}
