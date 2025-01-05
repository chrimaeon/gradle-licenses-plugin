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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.OutputStream
import javax.inject.Inject

abstract class JsonReport
    @Inject
    constructor(
        project: Project,
        task: Task,
    ) : LicensesSingleFileReport(project, task, ReportType.JSON) {
        private val json =
            Json {
                prettyPrint = true
            }

        @OptIn(ExperimentalSerializationApi::class)
        override fun writeLicenses(outputStream: OutputStream) {
            json.encodeToStream(libraries, outputStream)
        }
    }
