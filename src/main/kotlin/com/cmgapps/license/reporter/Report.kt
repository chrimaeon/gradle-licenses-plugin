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
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.util.internal.ConfigureUtil
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.javaType

abstract class Report(protected val libraries: List<Library>) {
    abstract fun generate(): String
}

open class LicensesReport(internal val type: ReportType, task: Task) {
    @get:Internal
    internal val name: String = type.name

    @get:OutputFile
    val destination: RegularFileProperty = task.project.objects.fileProperty()

    @get:Input
    val enabled: Property<Boolean> = task.project.objects.property(Boolean::class.java).convention(false)

    init {
        val extension = if (type.extension.isBlank()) "" else ".${type.extension}"
        destination.set(
            task.project.buildDir.resolve("reports/licenses").resolve(task.name).resolve("licenses$extension")
        )
    }

    internal open fun configure(
        config: (Action<in LicensesReport>)? = null,
        configHtml: (Action<in CustomizableHtmlReport>)? = null,
        configCustom: (Action<in CustomizableReport>)? = null,
    ) {
        config?.execute(this)
    }
}

class CustomizableHtmlReport(type: ReportType, task: Task) : LicensesReport(type, task) {
    @get:Input
    var stylesheet: Property<TextResource?> = task.project.objects.property(TextResource::class.java)

    override fun configure(
        config: (Action<in LicensesReport>)?,
        configHtml: (Action<in CustomizableHtmlReport>)?,
        configCustom: (Action<in CustomizableReport>)?,
    ) {
        super.configure(config, configHtml, configCustom)
        configHtml?.execute(this)
    }
}

class CustomizableReport(type: ReportType, task: Task) : LicensesReport(type, task) {
    @get:Internal
    internal var action: CustomReportAction? = null
        private set

    fun generate(block: CustomReportAction? = null) {
        action = block
    }

    override fun configure(
        config: (Action<in LicensesReport>)?,
        configHtml: (Action<in CustomizableHtmlReport>)?,
        configCustom: (Action<in CustomizableReport>)?,
    ) {
        super.configure(config, configHtml, configCustom)
        configCustom?.execute(this)
    }
}

typealias CustomReportAction = (List<Library>) -> String

interface LicensesReportsContainer {
    @get:Internal
    val csv: LicensesReport

    fun csv(config: Action<LicensesReport>)
    fun csv(config: Closure<LicensesReport>)

    @get:Internal
    val html: CustomizableHtmlReport

    fun html(config: Action<CustomizableHtmlReport>)
    fun html(config: Closure<CustomizableHtmlReport>)

    @get:Internal
    val json: LicensesReport

    fun json(config: Action<LicensesReport>)
    fun json(config: Closure<LicensesReport>)

    @get:Internal
    val markdown: LicensesReport

    fun markdown(config: Action<LicensesReport>)
    fun markdown(config: Closure<LicensesReport>)

    @get:Internal
    val text: LicensesReport

    fun text(config: Action<LicensesReport>)
    fun text(config: Closure<LicensesReport>)

    @get:Internal
    val xml: LicensesReport

    fun xml(config: Action<LicensesReport>)
    fun xml(config: Closure<LicensesReport>)

    @get:Internal
    val custom: CustomizableReport

    fun custom(config: Action<CustomizableReport>)
    fun custom(config: Closure<CustomizableReport>)

    @Internal
    fun getAll(): List<LicensesReport>
}

internal class LicensesReportsContainerImpl(private val task: Task) : LicensesReportsContainer {
    override val csv: LicensesReport by LicenseReportDelegate(ReportType.CSV)
    override fun csv(config: Action<LicensesReport>) {
        csv.configure(config)
    }

    override fun csv(config: Closure<LicensesReport>) {
        ConfigureUtil.configure(config, csv)
    }

    override val html: CustomizableHtmlReport by LicenseReportDelegate(ReportType.HTML)

    override fun html(config: Action<CustomizableHtmlReport>) {
        html.configure(configHtml = config)
    }

    override fun html(config: Closure<CustomizableHtmlReport>) {
        ConfigureUtil.configure(config, html)
    }

    override val json: LicensesReport by LicenseReportDelegate(ReportType.JSON)
    override fun json(config: Action<LicensesReport>) {
        json.configure(config)
    }

    override fun json(config: Closure<LicensesReport>) {
        ConfigureUtil.configure(config, json)
    }

    override val markdown: LicensesReport by LicenseReportDelegate(ReportType.MARKDOWN)
    override fun markdown(config: Action<LicensesReport>) {
        markdown.configure(config)
    }

    override fun markdown(config: Closure<LicensesReport>) {
        ConfigureUtil.configure(config, markdown)
    }

    override val text: LicensesReport by LicenseReportDelegate(ReportType.TEXT)
    override fun text(config: Action<LicensesReport>) {
        text.configure(config)
    }

    override fun text(config: Closure<LicensesReport>) {
        ConfigureUtil.configure(config, text)
    }

    override val xml: LicensesReport by LicenseReportDelegate(ReportType.XML)
    override fun xml(config: Action<LicensesReport>) {
        xml.configure(config)
    }

    override fun xml(config: Closure<LicensesReport>) {
        ConfigureUtil.configure(config, xml)
    }

    override val custom: CustomizableReport by LicenseReportDelegate(ReportType.CUSTOM)
    override fun custom(config: Action<CustomizableReport>) {
        custom.configure(configCustom = config)
    }

    override fun custom(config: Closure<CustomizableReport>) {
        ConfigureUtil.configure(config, custom)
    }

    override fun getAll(): List<LicensesReport> = listOf(
        csv,
        html,
        json,
        markdown,
        text,
        xml,
        custom
    )

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
    CUSTOM("")
}
