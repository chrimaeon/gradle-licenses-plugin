/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.OutputStream
import javax.inject.Inject

abstract class CsvReport
    @Inject
    constructor(
        project: Project,
        task: Task,
    ) : LicensesSingleFileReport(project, task, ReportType.CSV) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use { writer ->
                CSVPrinter(
                    writer,
                    CSVFormat.RFC4180
                        .builder()
                        .setHeader(*HEADER)
                        .get(),
                ).use { printer ->
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
            }
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
