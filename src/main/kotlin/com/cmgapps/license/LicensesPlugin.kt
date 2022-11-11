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
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Action
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

@Suppress("unused")
class LicensesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create("licenses", LicensesExtension::class.java, project).also { extension ->
            project.plugins.withId("java") {
                configureJavaProject(project, extension)
            }

            project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
                configureMultiplatformProject(project, extension)
            }

            ANDROID_IDS.forEach { id ->
                project.plugins.withId(id) {
                    configureAndroidProject(project, extension)
                }
            }
        }
    }

    companion object {
        private const val TASK_DESC = "Collect licenses from project"
        private const val TASK_GROUP = "Reporting"

        private val ANDROID_IDS = listOf(
            "com.android.application",
            "com.android.library",
            "com.android.feature",
            "com.android.dynamic-feature",
        )

        @JvmStatic
        private fun configureJavaProject(project: Project, extension: LicensesExtension) {
            val taskName = "licenseReport"

            val configuration = Action<LicensesTask> { task ->
                task.addBasicConfiguration(extension)
            }

            project.tasks.register(taskName, LicensesTask::class.java, configuration)
        }

        @JvmStatic
        private fun configureAndroidProject(project: Project, extension: LicensesExtension) {
            // check for AGP 7.0+ 'androidComponent' extension
            if (findClass("com.android.build.api.variant.AndroidComponentsExtension") != null) {
                configureAgp7Project(project, extension)
            } else {
                configureApg4Project(project, extension)
            }
        }

        @JvmStatic
        private fun configureAgp7Project(project: Project, extension: LicensesExtension) {
            project.logger.info("Using AGP 7.0+ AndroidComponentsExtension")
            project.extensions.getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
                .onVariants { variant ->
                    val configuration = Action<AndroidLicensesTask> { task ->
                        task.addBasicConfiguration(extension)
                        task.variant = variant.name
                        task.buildType = variant.buildType!!
                        task.productFlavors = variant.productFlavors.map { it.second }
                    }

                    project.tasks.register(
                        "license${variant.name.capitalize()}Report",
                        AndroidLicensesTask::class.java,
                        configuration,
                    )
                }
        }

        @JvmStatic
        private fun configureApg4Project(project: Project, extension: LicensesExtension) {
            project.logger.info("Using appication/libraryVariants")
            getAndroidVariants(project)?.all { androidVariant ->
                val configuration = Action<AndroidLicensesTask> { task ->
                    task.addBasicConfiguration(extension)
                    task.variant = androidVariant.name
                    task.buildType = androidVariant.buildType.name
                    task.productFlavors = androidVariant.productFlavors.map { it.name }
                }

                project.tasks.register(
                    "license${androidVariant.name.capitalize()}Report",
                    AndroidLicensesTask::class.java,
                    configuration,
                )
            }
        }

        @JvmStatic
        private fun configureMultiplatformProject(project: Project, extension: LicensesExtension) {
            val kotlinMultiplatformExtension = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension

            kotlinMultiplatformExtension.targets.all { target ->
                if (target.platformType == KotlinPlatformType.common) {
                    return@all
                }

                val configuration = Action<KotlinMultiplatformTask> { task ->
                    task.addBasicConfiguration(extension)
                    task.targetNames = listOf("common", target.name)
                }

                project.tasks.register(
                    "licenseMultiplatform${target.name.capitalize()}Report",
                    KotlinMultiplatformTask::class.java,
                    configuration,
                )
            }

            val targetNames = mutableListOf("common")
            kotlinMultiplatformExtension.targets.all { target ->
                if (target.platformType == KotlinPlatformType.common) {
                    return@all
                }

                targetNames.add(target.name)
            }

            val configuration = Action<KotlinMultiplatformTask> { task ->
                task.addBasicConfiguration(extension)
                task.targetNames = targetNames
            }

            project.tasks.register(
                "licenseMultiplatformReport",
                KotlinMultiplatformTask::class.java,
                configuration,
            )
        }

        @JvmStatic
        private fun LicensesTask.addBasicConfiguration(extension: LicensesExtension) {
            additionalProjects = extension.additionalProjects
            description = TASK_DESC
            group = TASK_GROUP
            reports(extension.reports)
        }

        @Suppress("DEPRECATION")
        @JvmStatic
        private fun getAndroidVariants(project: Project): DomainObjectSet<out BaseVariant>? = when {
            project.plugins.hasPlugin(AppPlugin::class.java) ||
                project.plugins.hasPlugin(DynamicFeaturePlugin::class.java) ->
                project.extensions.getByType(AppExtension::class.java).applicationVariants

            project.plugins.hasPlugin(LibraryPlugin::class.java) ->
                project.extensions.getByType(LibraryExtension::class.java).libraryVariants

            else -> null
        }
    }
}

fun findClass(fqName: String) = try {
    Class.forName(fqName)
} catch (ex: ClassNotFoundException) {
    null
}
