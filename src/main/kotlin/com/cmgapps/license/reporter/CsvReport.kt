/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringWriter

internal class CsvReport(libraries: List<Library>) : Report(libraries) {
    override fun generate(): String =
        StringWriter().use { writer ->
            CSVPrinter(writer, CSVFormat.RFC4180.builder().setHeader(*HEADER).build()).use { printer ->
                libraries.forEach { library ->
                    library.licenses.forEach { license ->
                        printer.printRecord(
                            library.name,
                            library.mavenCoordinates.version,
                            library.mavenCoordinates,
                            library.description,
                            license.id.spdxLicenseIdentifier,
                            license.name,
                            license.url,
                        )
                    }
                }
                printer.flush()
            }
            writer.toString()
        }

    companion object {
        @JvmStatic
        private val HEADER =
            arrayOf(
                "Name",
                "Version",
                "MavenCoordinates",
                "Description",
                "SPDX-License-Identifier",
                "License Name",
                "License Url",
            )
    }
}
