/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.PomLicense
import com.cmgapps.license.repository.SpdxIdRepository
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import java.io.OutputStream
import javax.inject.Inject

abstract class TextReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.TEXT, spdxIdRepository) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use {
                it.write(
                    buildString {
                        append("Licenses\n")
                        val libLength = libraries.size
                        libraries.entries.forEachIndexed { libIndex, (coordinates, library) ->
                            appendPrefix(libIndex, libLength)
                            if (library.name == null) {
                                append(coordinates.toString())
                            } else {
                                append(library.name)
                                append(':')
                                append(coordinates.version)
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
            licenses: Collection<PomLicense>,
        ) {
            val linePrefix = if (libIndex < libLength - 1) LINE_PREFIX else LAST_LINE_PREFIX

            if (licenses.isEmpty()) {
                append(linePrefix)
                append(LAST_ITEM_PREFIX)
                append(" License: Undefined")
                return
            }

            val entries =
                licenses.flatMap { license ->
                    val spdxIds = spdxIdRepository.getSpdxIds(url = license.url, name = license.name)
                    if (spdxIds.isEmpty()) {
                        listOf(Triple(license.name, null, license.url))
                    } else {
                        spdxIds.map { Triple(it.name, it.id, it.url) }
                    }
                }
            val total = entries.size

            entries.forEachIndexed { index, (name, spdxId, url) ->
                append(linePrefix)
                append("$ITEM_PREFIX License: ")
                append(name)

                if (spdxId != null) {
                    append(linePrefix)
                    append("$ITEM_PREFIX SPDX-License-Identifier: ")
                    append(spdxId)
                }

                append(linePrefix)
                if (index < total - 1) {
                    append(ITEM_PREFIX)
                } else {
                    append(LAST_ITEM_PREFIX)
                }
                append(" URL: ")
                append(url)
            }
        }

        private companion object {
            private const val ITEM_PREFIX = "├─"
            private const val LAST_ITEM_PREFIX = "└─"
            private const val LINE_PREFIX = "\n│  "
            private const val LAST_LINE_PREFIX = "\n   "
        }
    }
