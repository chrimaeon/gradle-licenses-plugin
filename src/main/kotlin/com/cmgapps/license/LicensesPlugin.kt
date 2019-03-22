/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
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

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
class LicensesPlugin : Plugin<Project> {

    companion object {
        private const val TASK_DESC = "Collect licenses from libraries"
        private const val TASK_GROUP = "Reporting"

        private const val APP_PLUGIN_ID = "com.android.application"
        private const val LIBRARY_PLUGIN_ID = "com.android.library"
        private const val FEATURE_PLUGIN_ID = "com.android.feature"
        private val ANDROID_IDS = listOf(APP_PLUGIN_ID, LIBRARY_PLUGIN_ID, FEATURE_PLUGIN_ID)

        @JvmStatic
        private fun configureJavaProject(project: Project, extension: LicensesExtension) {
            val taskName = "licenseReport"
            val path = "${project.buildDir}/reports/licenses/$taskName/"
            val outputType = extension.outputType ?: OutputType.HTML

            val task = project.tasks.create(taskName, LicensesTask::class.java)
            task.outputFile = project.file(path + getFileName(outputType))
            task.outputType = outputType
            task.description = TASK_DESC
            task.group = TASK_GROUP
            task.outputs.upToDateWhen { false }
        }

        @JvmStatic
        private fun configureAndroidProject(project: Project, extension: LicensesExtension) {
            getAndroidVariants(project)?.all { variant ->
                val taskName = "license${variant.name.capitalize()}Report"
                val path = "${project.buildDir}/reports/licenses/$taskName/"
                val outputType = extension.outputType ?: OutputType.HTML

                val task = project.tasks.create(taskName, LicensesTask::class.java)
                task.outputFile = project.file(path + getFileName(outputType))
                task.outputType = outputType
                task.description = TASK_DESC
                task.group = TASK_GROUP
                task.variant = variant.name
                task.buildType = variant.buildType.name
                task.productFlavors = variant.productFlavors

                task.outputs.upToDateWhen { false }
            }
        }

        @JvmStatic
        private fun getAndroidVariants(project: Project): DomainObjectSet<out BaseVariant>? {
            if (project.plugins.hasPlugin(AppPlugin::class.java)) {
                return project.extensions.getByType(AppExtension::class.java).applicationVariants
            }

            if (project.plugins.hasPlugin(LibraryPlugin::class.java)) {
                return project.extensions.getByType(LibraryExtension::class.java).libraryVariants
            }

            if (project.plugins.hasPlugin(FeaturePlugin::class.java)) {
                return project.extensions.getByType(FeatureExtension::class.java).featureVariants
            }

            return null
        }

        @JvmStatic
        private fun getFileName(type: OutputType): String {
            val filename = "licenses"
            return when (type) {
                OutputType.HTML -> "$filename.html"
                OutputType.XML -> "$filename.xml"
                OutputType.JSON -> "$filename.json"
                OutputType.TEXT -> "$filename.txt"
                OutputType.MD -> "$filename.md"
            }
        }
    }

    override fun apply(project: Project) {

        val extension = project.extensions.create("licenses", LicensesExtension::class.java)

        project.plugins.withId("java") { configureJavaProject(project, extension) }

        ANDROID_IDS.forEach { id ->
            project.plugins.withId(id) { configureAndroidProject(project, extension) }
        }
    }
}
