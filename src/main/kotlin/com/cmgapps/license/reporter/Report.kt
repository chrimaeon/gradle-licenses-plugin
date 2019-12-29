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

open class SimpleLicenseReport(final override val name: String, task: Task) : LicensesReport {
    final override var destination: File
    override var enabled: Boolean = false

    init {
        val name = if (name == "custom") "" else ".$name"
        destination = File("${task.project.buildDir}/reports/licenses/${task.name}/licenses$name")
    }
}

class CustomizableHtmlReport(name: String, task: Task) : SimpleLicenseReport(name, task) {
    var stylesheet: TextResource? = null
}

class CustomizableReport(name: String, task: Task) : SimpleLicenseReport(name, task) {
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

internal class LicensesReportsContainerImpl(task: Task) : LicensesReportsContainer {
    val reports = mutableMapOf<String, LicensesReport>()

    init {
        reports["csv"] = SimpleLicenseReport("csv", task)
        reports["html"] = CustomizableHtmlReport("html", task)
        reports["json"] = SimpleLicenseReport("json", task)
        reports["md"] = SimpleLicenseReport("md", task)
        reports["txt"] = SimpleLicenseReport("txt", task)
        reports["xml"] = SimpleLicenseReport("xml", task)
        reports["custom"] = CustomizableReport("custom", task)
    }

    override val csv: LicensesReport = checkNotNull(reports["csv"])
    override val html: CustomizableHtmlReport = checkNotNull(reports["html"]) as CustomizableHtmlReport
    override val json: LicensesReport = checkNotNull(reports["json"])
    override val markdown: LicensesReport = checkNotNull(reports["md"])
    override val text: LicensesReport = checkNotNull(reports["txt"])
    override val xml: LicensesReport = checkNotNull(reports["xml"])
    override val custom: CustomizableReport = checkNotNull(reports["custom"]) as CustomizableReport
}
