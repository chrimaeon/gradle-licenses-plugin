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

import com.cmgapps.license.helper.uppercaseFirstChar
import com.cmgapps.license.reporter.CustomReport
import com.cmgapps.license.reporter.HtmlReport
import com.cmgapps.license.reporter.ReportType
import org.apache.maven.artifact.versioning.ComparableVersion
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.SingleFileReport
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

private const val MIN_KOTLIN_VERSION = "2.0.0"

class LicensesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val licenseExtension = extensions.create("licenses", LicensesExtension::class.java)

            val licenseReportExtension =
                (licenseExtension as ExtensionAware).extensions.create(
                    "reports",
                    LicenseReportsExtension::class.java,
                )

            pluginManager.withPlugin("org.gradle.java") {
                // Special case: KMP with JVM withJava():
                // withKotlinMultiPlatformPlugin did already run, so the jvm target is already set, ignore
                // another setup.
                if (!project.pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
                    configureJavaProject(project, licenseReportExtension)
                }
            }

            pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
                configureMultiplatformProject(project, licenseReportExtension)
            }

            ANDROID_IDS.forEach { id ->
                pluginManager.withPlugin(id) {
                    configureAndroidProject(project, licenseReportExtension)
                }
            }
        }
    }

    companion object {
        private const val TASK_DESC = "Collect licenses from project"
        private const val TASK_GROUP = "Reporting"

        private val ANDROID_IDS =
            listOf(
                "com.android.application",
                "com.android.library",
                "com.android.feature",
                "com.android.dynamic-feature",
            )

        @JvmStatic
        private fun configureJavaProject(
            project: Project,
            licenseReportExtension: LicenseReportsExtension,
        ) {
            project.tasks.register(
                "licenseReport",
                LicensesTask::class.java,
            ) { task ->
                task.addBasicConfiguration(licenseReportExtension)
                val configuration = project.configurations.named(RUNTIME_CLASSPATH_CONFIGURATION_NAME)
                task.configurationToCheck(configuration)
            }
        }

        @JvmStatic
        private fun configureAndroidProject(
            project: Project,
            reportExtension: LicenseReportsExtension,
        ) {
            // check for AGP 7.0+ 'androidComponent' extension
            if (findClass("com.android.build.api.variant.AndroidComponentsExtension") != null) {
                configureAgp7Project(project, reportExtension)
            } else {
                throw GradleException("Minimum Android Gradle Plugin Version is 7.0+")
            }
        }

        @JvmStatic
        private fun configureAgp7Project(
            project: Project,
            reportExtension: LicenseReportsExtension,
        ) {
            project.logger.info("Using AGP 7.0+ AndroidComponentsExtension")
            val androidComponentsExtension =
                project.extensions.getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
            androidComponentsExtension
                .onVariants { variant ->

                    project.tasks.register(
                        "license${variant.name.uppercaseFirstChar()}Report",
                        LicensesTask::class.java,
                    ) { task ->
                        task.addBasicConfiguration(reportExtension)
                        task.configurationToCheck(variant.runtimeConfiguration)
                    }
                }
        }

        @JvmStatic
        private fun configureMultiplatformProject(
            project: Project,
            reportExtension: LicenseReportsExtension,
        ) {
            val kotlinVersion = ComparableVersion(project.getKotlinPluginVersion())

            if (kotlinVersion < ComparableVersion(MIN_KOTLIN_VERSION)) {
                throw GradleException("Using Multiplatform Gradle Plugin v$kotlinVersion not supported")
            }

            val kotlinMultiplatformExtension = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

            val targetRuntimeConfiguration = mutableListOf<Provider<Configuration>>()

            val rootTask = project.tasks.register("licenseMultiplatformReport")

            kotlinMultiplatformExtension.targets.configureEach { target ->
                val targetName = target.name
                if (target.platformType == KotlinPlatformType.common) {
                    // All common dependencies end up in platform targets.
                    return@configureEach
                }

                val task =
                    project.tasks.register(
                        "licenseMultiplatform${targetName.uppercaseFirstChar()}Report",
                        LicensesTask::class.java,
                    ) { task ->
                        task.addBasicConfiguration(reportExtension)
                        val compilation = target.compilations.getByName(KotlinCompilation.MAIN_COMPILATION_NAME)
                        // Fallback to compile dependencies when runtime isn't supported, e.g. Kotlin/Native.
                        val runtimeConfigurationName =
                            compilation.runtimeDependencyConfigurationName
                                ?: compilation.compileDependencyConfigurationName

                        val runtimeConfiguration = project.configurations.named(runtimeConfigurationName)
                        targetRuntimeConfiguration.add(runtimeConfiguration)
                        task.configurationToCheck(runtimeConfiguration)
                    }

                rootTask.configure { it.dependsOn(task) }
            }
        }

        @JvmStatic
        private fun LicensesTask.addBasicConfiguration(reportExtension: LicenseReportsExtension) {
            description = TASK_DESC
            group = TASK_GROUP

            reports.configureEach { report ->
                when (report.name) {
                    ReportType.HTML.name -> {
                        report as HtmlReport
                        val reporter =
                            reportExtension.html
                        report.configureReport(reporter)
                        report.css.set(reporter.css)
                        report.useDarkMode.set(reporter.useDarkMode)
                    }

                    ReportType.CSV.name -> {
                        val reporter =
                            reportExtension.csv
                        report.configureReport(reporter)
                    }

                    ReportType.JSON.name -> {
                        val reporter =
                            reportExtension.json
                        report.configureReport(reporter)
                    }

                    ReportType.MARKDOWN.name -> {
                        val reporter =
                            reportExtension.markdown
                        report.configureReport(reporter)
                    }

                    ReportType.TEXT.name -> {
                        val reporter =
                            reportExtension.plainText
                        report.configureReport(reporter)
                    }

                    ReportType.XML.name -> {
                        val reporter =
                            reportExtension.xml
                        report.configureReport(reporter)
                    }

                    ReportType.CUSTOM.name -> {
                        report as CustomReport
                        val reporter =
                            reportExtension.custom

                        report.generator.set(reporter.generator)

                        report.configureReport(reporter)
                    }

                    else -> {
                        throw GradleException("Unknown report $name")
                    }
                }
            }
        }

        @JvmStatic
        private fun SingleFileReport.configureReport(reporter: Reporter) {
            required.set(reporter.enabled)
            if (reporter.outputFile.isPresent) {
                outputLocation.set(reporter.outputFile)
            }
        }
    }
}

fun findClass(fqName: String) =
    try {
        Class.forName(fqName)
    } catch (_: ClassNotFoundException) {
        null
    }
