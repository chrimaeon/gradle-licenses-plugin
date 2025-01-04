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

import com.cmgapps.license.model.Library
import groovy.lang.Closure
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.SingleFileReport
import java.io.File
import java.io.OutputStream

interface LicenseReport {
    var libraries: List<Library>
}

abstract class LicensesSingleFileReport(
    project: Project,
    task: Task,
    private val type: ReportType,
) : LicenseReport,
    SingleFileReport {
    init {
        required.convention(false)
        outputLocation.convention(project.layout.buildDirectory.file("reports/licenses/${task.name}/licenses.${type.extension}"))
    }

    abstract fun writeLicenses(outputStream: OutputStream)

    override fun getName(): String = type.name

    override fun getDisplayName(): String = "License Report for ${type.name}"

    @Override
    @Deprecated("Deprecated in Java", replaceWith = ReplaceWith("getOutputLocation().set"))
    override fun setDestination(file: File) {
        outputLocation.fileValue(file)
    }

    override fun getOutputType(): Report.OutputType = Report.OutputType.FILE

    /**
     * Needed for 7.x
     */
    @Override
    fun setDestination(provider: Provider<File>) {
        outputLocation.fileProvider(provider)
    }

    /**
     * Needed for 7.x
     */
    @Override
    fun getDestination(): File = outputLocation.get().asFile

    /**
     * Needed for 7.x
     */
    @Override
    fun isEnabled(): Boolean = required.get()

    /**
     * Needed for 7.x
     */
    @Override
    fun setEnabled(enabled: Boolean) {
        required.set(enabled)
    }

    /**
     * Needed for 7.x
     */
    @Override
    fun setEnabled(enabled: Provider<Boolean>) = required.set(enabled)

    override fun configure(cl: Closure<*>): Report {
        cl.call(this)
        return this
    }
}

@FunctionalInterface
fun interface CustomReportGenerator {
    fun generate(libraries: List<Library>): String
}

enum class ReportType(
    val extension: String,
) {
    CSV("csv"),
    HTML("html"),
    JSON("json"),
    MARKDOWN("md"),
    TEXT("txt"),
    XML("xml"),
    CUSTOM(""),
}
