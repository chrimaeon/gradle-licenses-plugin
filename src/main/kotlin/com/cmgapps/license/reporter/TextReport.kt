/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.License
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.OutputStream
import javax.inject.Inject

abstract class TextReport
    @Inject
    constructor(
        project: Project,
        task: Task,
    ) : LicensesSingleFileReport(project, task, ReportType.TEXT) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use {
                it.write(
                    buildString {
                        append("Licenses\n")
                        val libLength = libraries.size
                        libraries.forEachIndexed { libIndex, library ->
                            appendPrefix(libIndex, libLength)
                            if (library.name == null) {
                                append(library.mavenCoordinates.toString())
                            } else {
                                append(library.name)
                                append(':')
                                append(library.mavenCoordinates.version)
                            }
                            appendLicenses(libIndex, libLength, library.licenses)
                            if (libIndex < libLength - 1) {
                                append('\n')
                            }
                        }
                    },
                )
            }
        }

        private fun StringBuilder.appendPrefix(
            index: Int,
            length: Int,
        ) {
            if (index < length - 1) {
                append("$ITEM_PREFIX ")
            } else {
                append("$LAST_ITEM_PREFIX ")
            }
        }

        private fun StringBuilder.appendLicenses(
            libIndex: Int,
            libLength: Int,
            licenses: List<License>,
        ) {
            val licensesLength = licenses.size

            if (licenses.isEmpty()) {
                if (libIndex < libLength - 1) {
                    append(LINE_PREFIX)
                } else {
                    append(LAST_LINE_PREFIX)
                }

                append(LAST_ITEM_PREFIX)

                append(" License: Undefined")
                return
            }

            licenses.forEachIndexed { index, license ->
                if (libIndex < libLength - 1) {
                    append(LINE_PREFIX)
                } else {
                    append(LAST_LINE_PREFIX)
                }

                append("$ITEM_PREFIX License: ")
                append(license.name)

                license.id.spdxLicenseIdentifier?.let {
                    if (libIndex < libLength - 1) {
                        append(LINE_PREFIX)
                    } else {
                        append(LAST_LINE_PREFIX)
                    }

                    append("$ITEM_PREFIX SPDX-License-Identifier: ")
                    append(license.id.spdxLicenseIdentifier)
                }

                if (libIndex < libLength - 1) {
                    append(LINE_PREFIX)
                } else {
                    append(LAST_LINE_PREFIX)
                }

                if (index < licensesLength - 1) {
                    append(ITEM_PREFIX)
                } else {
                    append(LAST_ITEM_PREFIX)
                }
                append(" URL: ")
                append(license.url)
            }
        }

        private companion object {
            private const val ITEM_PREFIX = "├─"
            private const val LAST_ITEM_PREFIX = "└─"
            private const val LINE_PREFIX = "\n│  "
            private const val LAST_LINE_PREFIX = "\n   "
        }
    }
