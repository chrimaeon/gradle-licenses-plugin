/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.repository.SpdxIdRepository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import java.io.OutputStream
import javax.inject.Inject

abstract class CsvReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.CSV, spdxIdRepository) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use { writer ->
                CSVPrinter(
                    writer,
                    CSVFormat.RFC4180
                        .builder()
                        .setHeader(*HEADER)
                        .get(),
                ).use { printer ->
                    libraries.forEach { (coordinates, library) ->
                        library.licenses.forEach { license ->
                            val spdxIds = spdxIdRepository.getSpdxIds(url = license.url, name = license.name)

                            if (spdxIds.isEmpty()) {
                                printer.printRecord(
                                    library.name,
                                    coordinates.version,
                                    coordinates.toString(),
                                    library.description,
                                    null,
                                    license.name,
                                    license.url,
                                )
                            } else {
                                for (spdxId in spdxIds) {
                                    printer.printRecord(
                                        library.name,
                                        coordinates.version,
                                        coordinates,
                                        library.description,
                                        spdxId.id,
                                        spdxId.name,
                                        spdxId.url,
                                    )
                                }
                            }
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
