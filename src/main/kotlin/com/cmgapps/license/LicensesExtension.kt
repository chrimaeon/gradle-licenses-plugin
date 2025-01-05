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

package com.cmgapps.license

import com.cmgapps.license.reporter.CustomReportGenerator
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.resources.TextResource
import java.io.File
import javax.inject.Inject

@Suppress("unused")
abstract class LicensesExtension {
    var additionalProjects = emptySet<String>()
        private set

    fun additionalProjects(vararg modules: String) {
        additionalProjects = setOf(*modules)
    }

    fun additionalProjects(modules: Collection<String>) {
        additionalProjects =
            when (modules) {
                is Set -> modules
                else -> modules.toSet()
            }
    }
}

abstract class LicenseReportsExtension
    @Inject
    constructor(
        project: Project,
        objects: ObjectFactory,
    ) {
        val plainText: Reporter =
            Reporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
            )

        fun plainText(action: Action<in Reporter>) = action.execute(plainText)

        val csv: Reporter =
            Reporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
            )

        fun csv(action: Action<in Reporter>) = action.execute(csv)

        val json: Reporter =
            Reporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
            )

        fun json(action: Action<in Reporter>) = action.execute(json)

        val html: HtmlReporter =
            HtmlReporter(
                enabled = objects.property(Boolean::class.java).convention(true),
                outputFile = objects.fileProperty(),
                useDarkMode = objects.property(Boolean::class.java).convention(true),
                css = objects.property(TextResource::class.java),
                project = project,
            )

        fun html(action: Action<in HtmlReporter>) = action.execute(html)

        val markdown: Reporter =
            Reporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
            )

        fun markdown(action: Action<in Reporter>) = action.execute(markdown)

        val xml: Reporter =
            Reporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
            )

        fun xml(action: Action<in Reporter>) = action.execute(xml)

        val custom: CustomReporter =
            CustomReporter(
                enabled = objects.property(Boolean::class.java).convention(false),
                outputFile = objects.fileProperty(),
                generator = objects.property(CustomReportGenerator::class.java),
            )

        fun custom(action: Action<in CustomReporter>) = action.execute(custom)
    }

open class Reporter(
    val enabled: Property<Boolean>,
    val outputFile: RegularFileProperty,
)

class HtmlReporter(
    enabled: Property<Boolean>,
    outputFile: RegularFileProperty,
    val useDarkMode: Property<Boolean>,
    internal val css: Property<TextResource>,
    private val project: Project,
) : Reporter(enabled, outputFile) {
    fun stylesheet(css: String) {
        this.css.set(project.resources.text.fromString(css))
    }

    fun stylesheet(css: File) {
        this.css.set(project.resources.text.fromFile(css))
    }
}

class CustomReporter(
    enabled: Property<Boolean>,
    outputFile: RegularFileProperty,
    val generator: Property<CustomReportGenerator>,
) : Reporter(enabled, outputFile)

@Suppress("unused")
enum class OutputType {
    HTML,
    XML,
    JSON,
    TEXT,
    MD,
    CSV,
    CUSTOM,
}
