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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.DynamicFeaturePlugin
import com.android.build.gradle.FeatureExtension
import com.android.build.gradle.FeaturePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.util.GradleVersion

@Suppress("unused")
class LicensesPlugin : Plugin<Project> {

    companion object {
        private const val TASK_DESC = "Collect licenses from project"
        private const val TASK_GROUP = "Reporting"

        private val ANDROID_IDS = listOf(
            "com.android.application",
            "com.android.library",
            "com.android.feature",
            "com.android.dynamic-feature"
        )

        @JvmStatic
        private fun configureJavaProject(project: Project, extension: LicensesExtension) {
            val taskName = "licenseReport"

            val configuration = Action<LicensesTask> { task ->
                task.addBasicConfiguration(extension)
            }

            registerTask(project, LicensesTask::class.java, taskName, configuration)
        }

        @JvmStatic
        private fun configureAndroidProject(project: Project, extension: LicensesExtension) {
            getAndroidVariants(project)?.all { androidVariant ->
                val taskName = "license${androidVariant.name.capitalize()}Report"

                val configuration = Action<AndroidLicensesTask> { task ->
                    task.addBasicConfiguration(extension)
                    task.variant = androidVariant.name
                    task.buildType = androidVariant.buildType.name
                    task.productFlavors = androidVariant.productFlavors
                }

                registerTask(project, AndroidLicensesTask::class.java, taskName, configuration)
            }
        }

        @JvmStatic
        private fun LicensesTask.addBasicConfiguration(extension: LicensesExtension) {
            additionalProjects = extension.additionalProjects
            description = TASK_DESC
            group = TASK_GROUP
            reports(extension.reports)
        }

        @JvmStatic
        private fun <T : LicensesTask> registerTask(
            project: Project,
            type: Class<T>,
            taskName: String,
            configuration: Action<T>
        ) {
            if (GradleVersion.current() >= GradleVersion.version("4.9")) {
                project.tasks.register(taskName, type, configuration)
            } else {
                project.tasks.create(taskName, type, configuration)
            }
        }

        @JvmStatic
        private fun getAndroidVariants(project: Project): DomainObjectSet<out BaseVariant>? = when {
            project.plugins.hasPlugin(AppPlugin::class.java)
                || project.plugins.hasPlugin(DynamicFeaturePlugin::class.java) ->
                project.extensions.getByType(AppExtension::class.java).applicationVariants

            project.plugins.hasPlugin(FeaturePlugin::class.java) ->
                project.extensions.getByType(FeatureExtension::class.java).featureVariants

            project.plugins.hasPlugin(LibraryPlugin::class.java) ->
                project.extensions.getByType(LibraryExtension::class.java).libraryVariants

            else -> null
        }

        @JvmStatic
        private fun getFileName(type: OutputType) = when (type) {
            OutputType.HTML -> ".html"
            OutputType.XML -> ".xml"
            OutputType.JSON -> ".json"
            OutputType.TEXT -> ".txt"
            OutputType.MD -> ".md"
            OutputType.CSV -> ".csv"
            else -> ""
        }.let {
            "licenses$it"
        }
    }

    override fun apply(project: Project) {
        project.extensions.create("licenses", LicensesExtension::class.java).also {
            if (project.plugins.hasPlugin("java")) {
                configureJavaProject(project, it)
            }

            ANDROID_IDS.forEach { id ->
                if (project.plugins.hasPlugin(id)) {
                    configureAndroidProject(project, it)
                }
            }
        }
    }
}
