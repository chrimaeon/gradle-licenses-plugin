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
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class Report(protected val libraries: List<Library>) {
    abstract fun generate(): String
}

open class LicensesReport(internal val type: ReportType, task: Task, internal val project: Project) {
    @get:Internal
    internal val name: String = type.name

    private val _destination: RegularFileProperty = task.project.objects.fileProperty()

    @get:OutputFile
    var destination: File
        get() = _destination.get().asFile
        set(value) = _destination.set(value)

    private val _enabled: Property<Boolean> = task.project.objects.property(Boolean::class.java).convention(false)

    @get:Input
    var enabled: Boolean
        get() = _enabled.get()
        set(value) = _enabled.set(value)

    init {
        val extension = if (type.extension.isBlank()) "" else ".${type.extension}"
        destination =
            task.project.buildDir.resolve("reports/licenses").resolve(task.name).resolve("licenses$extension")
    }

    internal open fun configure(
        config: (Action<in LicensesReport>)? = null,
        configHtml: (Action<in CustomizableHtmlReport>)? = null,
        configCustom: (Action<in CustomizableReport>)? = null,
    ) {
        config?.execute(this)
    }

    override fun toString(): String {
        return "LicenseReport{name:$name,enabled:$enabled,destination:$destination}}"
    }
}

class CustomizableHtmlReport(type: ReportType, task: Task, project: Project) : LicensesReport(type, task, project) {

    internal val _stylesheet: Property<TextResource?> = task.project.objects.property(TextResource::class.java)

    @Input
    fun stylesheet(css: String) {
        _stylesheet.set(project.resources.text.fromString(css))
    }

    @Input
    fun stylesheet(css: File) {
        _stylesheet.set(project.resources.text.fromFile(css))
    }

    @Input
    val useDarkMode: Property<Boolean> = task.project.objects.property(Boolean::class.java).convention(true)

    override fun configure(
        config: (Action<in LicensesReport>)?,
        configHtml: (Action<in CustomizableHtmlReport>)?,
        configCustom: (Action<in CustomizableReport>)?,
    ) {
        super.configure(config, configHtml, configCustom)
        configHtml?.execute(this)
    }
}

class CustomizableReport(type: ReportType, task: Task, project: Project) : LicensesReport(type, task, project) {
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

enum class ReportType(val extension: String) {
    CSV("csv"),
    HTML("html"),
    JSON("json"),
    MARKDOWN("md"),
    TEXT("txt"),
    XML("xml"),
    CUSTOM(""),
}
