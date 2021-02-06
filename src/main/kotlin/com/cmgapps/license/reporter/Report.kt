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
import org.gradle.api.Task
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class Report(protected val libraries: List<Library>) {
    abstract fun generate(): String
}

interface LicensesReport {
    @get:Internal
    val name: String
    @get:OutputFile
    var destination: File
    @get:Input
    var enabled: Boolean
}

private open class LicenseReportImpl(type: ReportType, task: Task) : LicensesReport {
    final override var destination: File
    final override var enabled: Boolean = false
    final override val name = type.name

    init {
        val extension = if (type == ReportType.CUSTOM) "" else ".${type.extension}"
        destination = File("${task.project.buildDir}/reports/licenses/${task.name}/licenses$extension")
    }
}

interface CustomizableHtmlReport : LicensesReport {
    var stylesheet: TextResource?
}

private class CustomizableHtmlReportImpl(type: ReportType, task: Task) :
    LicenseReportImpl(type, task), CustomizableHtmlReport {
    override var stylesheet: TextResource? = null
}

interface CustomizableReport : LicensesReport {
    var action: CustomReportAction?
}

private class CustomizableReportImpl(type: ReportType, task: Task) : LicenseReportImpl(type, task), CustomizableReport {
    override var action: CustomReportAction? = null
}

typealias CustomReportAction = (List<Library>) -> String

interface LicensesReportsContainer {
    @get:Internal
    val csv: LicensesReport

    @get:Internal
    val html: CustomizableHtmlReport

    @get:Internal
    val json: LicensesReport

    @get:Internal
    val markdown: LicensesReport

    @get:Internal
    val text: LicensesReport

    @get:Internal
    val xml: LicensesReport

    @get:Internal
    val custom: CustomizableReport
}

internal class LicensesReportsContainerImpl(task: Task) : LicensesReportsContainer {
    private val reports = mutableMapOf<ReportType, LicensesReport>()

    init {
        with(reports) {
            add(ReportType.CSV, LicenseReportImpl::class.java, task)
            add(ReportType.HTML, CustomizableHtmlReportImpl::class.java, task)
            add(ReportType.JSON, LicenseReportImpl::class.java, task)
            add(ReportType.MARKDOWN, LicenseReportImpl::class.java, task)
            add(ReportType.TEXT, LicenseReportImpl::class.java, task)
            add(ReportType.XML, LicenseReportImpl::class.java, task)
            add(ReportType.CUSTOM, CustomizableReportImpl::class.java, task)
        }
    }

    override val csv: LicensesReport = checkNotNull(reports[ReportType.CSV])
    override val html: CustomizableHtmlReport = checkNotNull(reports[ReportType.HTML]) as CustomizableHtmlReport
    override val json: LicensesReport = checkNotNull(reports[ReportType.JSON])
    override val markdown: LicensesReport = checkNotNull(reports[ReportType.MARKDOWN])
    override val text: LicensesReport = checkNotNull(reports[ReportType.TEXT])
    override val xml: LicensesReport = checkNotNull(reports[ReportType.XML])
    override val custom: CustomizableReport = checkNotNull(reports[ReportType.CUSTOM]) as CustomizableReport

    private fun <T : LicensesReport> MutableMap<ReportType, LicensesReport>.add(
        reportType: ReportType,
        type: Class<T>,
        task: Task
    ) {
        val licenseReport = type.getConstructor(ReportType::class.java, Task::class.java).newInstance(reportType, task)
        put(reportType, licenseReport)
    }
}

enum class ReportType(val extension: String) {
    CSV("csv"),
    HTML("html"),
    JSON("json"),
    MARKDOWN("md"),
    TEXT("txt"),
    XML("xml"),
    CUSTOM("custom")
}
