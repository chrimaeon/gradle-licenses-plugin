/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.MavenCoordinates
import com.cmgapps.license.repository.SpdxIdRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import java.io.OutputStream
import javax.inject.Inject

abstract class JsonReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.JSON, spdxIdRepository) {
        private val json =
            Json {
                prettyPrint = true
            }

        @OptIn(ExperimentalSerializationApi::class)
        override fun writeLicenses(outputStream: OutputStream) {
            json.encodeToStream(
                libraries.map { (coordinates, library) ->

                    val licenses = mutableListOf<JsonLicense>()

                    for (license in library.licenses) {
                        spdxIdRepository
                            .getSpdxIds(url = license.url, name = license.name)
                            .mapTo(licenses) {
                                JsonLicense(spdxLicenseIdentifier = it.id, name = it.name, url = it.url)
                            }.ifEmpty {
                                licenses.add(
                                    JsonLicense(
                                        spdxLicenseIdentifier = null,
                                        name = license.name ?: "",
                                        url = license.url ?: "",
                                    ),
                                )
                            }
                    }

                    JsonLibrary(
                        coordinates,
                        library.name,
                        library.description,
                        licenses.toSet(),
                    )
                },
                outputStream,
            )
        }
    }

@Serializable
private class JsonLibrary(
    val mavenCoordinates: MavenCoordinates,
    val name: String?,
    val description: String?,
    val licenses: Set<
        JsonLicense,
    >,
)

@Serializable
private class JsonLicense(
    val spdxLicenseIdentifier: String?,
    val name: String,
    val url: String,
)
