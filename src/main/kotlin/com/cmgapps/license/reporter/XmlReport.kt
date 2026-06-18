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

abstract class XmlReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.XML, spdxIdRepository) {
        override fun writeLicenses(outputStream: OutputStream) {
            outputStream.bufferedWriter().use {
                it.write(
                    libraries {
                        for ((coordinates, library) in libraries) {
                            library(
                                id = coordinates.identifierWithoutVersion,
                                version = coordinates.version,
                            ) {
                                name {
                                    +(library.name ?: coordinates.toString())
                                }

                                description {
                                    +(library.description ?: "")
                                }

                                licenses {
                                    append(library.licenses, spdxIdRepository)
                                }
                            }
                        }
                    }.toString(),
                )
            }
        }
    }

private fun Licenses.append(
    licenses: Set<PomLicense>,
    spdxIdRepository: SpdxIdRepository,
) {
    for (license in licenses) {
        val spdxIds = spdxIdRepository.getSpdxIds(url = license.url, name = license.name)

        if (spdxIds.isEmpty()) {
            license(
                url = license.url ?: "",
            ) {
                name {
                    +(license.name ?: "")
                }
            }
        } else {
            for (spdxId in spdxIds) {
                license(
                    spdxLicenseIdentifier = spdxId.id,
                    url = spdxId.url,
                ) {
                    name {
                        +spdxId.name
                    }
                }
            }
        }
    }
}

internal class Libraries : Tag("libraries") {
    fun library(
        id: String,
        version: String,
        init: Library.() -> Unit,
    ): Library {
        val tag = initTag(Library(), init)
        tag.attributes["id"] = id
        tag.attributes["version"] = version
        return tag
    }

    override fun render(
        builder: StringBuilder,
        intent: String,
        format: Boolean,
    ) {
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
        if (format) {
            builder.append('\n')
        }
        super.render(builder, intent, format)
    }
}

internal class Library : Tag("library") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)

    fun description(init: Description.() -> Unit) = initTag(Description(), init)

    fun licenses(init: Licenses.() -> Unit) = initTag(Licenses(), init)
}

internal class Name : TagWithText("name")

internal class Version : TagWithText("version")

internal class Description : TagWithText("description")

internal class Licenses : Tag("licenses") {
    fun license(
        spdxLicenseIdentifier: String? = null,
        url: String,
        init: License.() -> Unit,
    ) {
        val tag = initTag(License(), init)
        tag.attributes["url"] = url
        if (spdxLicenseIdentifier != null) {
            tag.attributes["spdx-license-identifier"] = spdxLicenseIdentifier
        }
    }
}

internal class License : Tag("license") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)
}

internal fun libraries(init: Libraries.() -> Unit): Libraries {
    val libraries = Libraries()
    libraries.attributes["xmlns"] = "https://www.cmgapps.com"
    libraries.attributes["xmlns:xsi"] = "http://www.w3.org/2001/XMLSchema-instance"
    libraries.attributes["xsi:schemaLocation"] = "https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd"

    libraries.init()
    return libraries
}
