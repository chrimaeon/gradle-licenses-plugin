/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.helper.logLicenseWarning
import com.cmgapps.license.helper.toLicensesMap
import com.cmgapps.license.repository.SpdxIdRepository
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logger
import java.io.OutputStream
import javax.inject.Inject

abstract class MarkdownReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        private val logger: Logger,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.MARKDOWN, spdxIdRepository) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use { writer ->
                writer.write(
                    buildString {
                        append("# Open source licenses\n")
                        append("## Notice for packages")
                        libraries.toLicensesMap().forEach { (license, pairs) ->
                            pairs.asSequence().sortedBy { it.second.name }.forEach { (coordinates, library) ->
                                append('\n')
                                append("* ")
                                append(library.name ?: coordinates.identifierWithoutVersion)
                            }

                            append("\n```\n")

                            if (license.id == "UNKNOWN") {
                                logger.logLicenseWarning(pairs)
                                append(license.name)
                                append("\n")
                                append(license.url)
                            } else {
                                with(spdxIdRepository) {
                                    append(license.licenseText())
                                }
                            }
                            append("\n```\n")
                        }
                    },
                )
            }
        }
    }
