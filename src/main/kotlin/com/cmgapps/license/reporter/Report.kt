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
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.javaType

abstract class Report(protected val libraries: List<Library>) {
    abstract fun generate(): String
}

open class LicensesReport(type: ReportType, task: Task) {
    @get:Internal
    val name: String = type.name

    @get:OutputFile
    internal val destination: RegularFileProperty = task.project.objects.fileProperty()

    @get:Input
    val enabled: Property<Boolean> = task.project.objects.property(Boolean::class.java).convention(false)

    init {
        val extension = if (type == ReportType.CUSTOM) "" else ".${type.extension}"
        destination.set(
            task.project.buildDir.resolve("reports/licenses").resolve(task.name).resolve("licenses$extension")
        )
    }
}

class CustomizableHtmlReport(type: ReportType, task: Task) : LicensesReport(type, task) {
    var stylesheet: TextResource? = null
}

class CustomizableReport(type: ReportType, task: Task) : LicensesReport(type, task) {
    var action: CustomReportAction? = null
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

internal class LicensesReportsContainerImpl(private val task: Task) : LicensesReportsContainer {
    override val csv: LicensesReport by LicenseReportDelegate(ReportType.CSV)
    override val html: CustomizableHtmlReport by LicenseReportDelegate(ReportType.HTML)
    override val json: LicensesReport by LicenseReportDelegate(ReportType.JSON)
    override val markdown: LicensesReport by LicenseReportDelegate(ReportType.MARKDOWN)
    override val text: LicensesReport by LicenseReportDelegate(ReportType.TEXT)
    override val xml: LicensesReport by LicenseReportDelegate(ReportType.XML)
    override val custom: CustomizableReport by LicenseReportDelegate(ReportType.CUSTOM)

    private inner class LicenseReportDelegate<T : LicensesReport>(private val reportType: ReportType) :
        ReadOnlyProperty<Any?, T> {

        private lateinit var value: T

        @Suppress("UNCHECKED_CAST")
        @OptIn(ExperimentalStdlibApi::class)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return if (::value.isInitialized) {
                value
            } else {
                (
                    Class.forName(property.returnType.javaType.typeName)
                        .getConstructor(ReportType::class.java, Task::class.java)
                        .newInstance(reportType, task) as T
                    ).also { value = it }
            }
        }
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
