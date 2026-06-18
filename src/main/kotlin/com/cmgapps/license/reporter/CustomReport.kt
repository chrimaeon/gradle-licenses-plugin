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

import com.cmgapps.license.repository.SpdxIdRepository
import org.gradle.api.Task
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import java.io.OutputStream
import javax.inject.Inject

abstract class CustomReport
    @Inject
    constructor(
        layout: ProjectLayout,
        task: Task,
        objects: ObjectFactory,
        spdxIdRepository: SpdxIdRepository,
    ) : LicensesSingleFileReport(layout, task, ReportType.CUSTOM, spdxIdRepository) {
        val generator: Property<CustomReportGenerator> = objects.property(CustomReportGenerator::class.java)

        override fun writeLicenses(outputStream: OutputStream) {
            check(generator.isPresent) { "CustomReport.generator not set" }
            outputStream.bufferedWriter().use { writer ->
                writer.write(
                    generator.get().generate(
                        libraries
                            .map { (coordinates, pomLibrary) ->
                                coordinates to
                                    CustomReportGenerator.Library(
                                        name = pomLibrary.name,
                                        description = pomLibrary.description,
                                        pomLibrary.licenses
                                            .map {
                                                CustomReportGenerator.License(
                                                    id =
                                                        spdxIdRepository
                                                            .getSpdxIds(name = it.name, url = it.url)
                                                            .firstOrNull()
                                                            ?.id,
                                                    name = it.name,
                                                    url = it.url,
                                                )
                                            }.toSet(),
                                    )
                            }.toMap(),
                    ),
                )
            }
        }
    }
