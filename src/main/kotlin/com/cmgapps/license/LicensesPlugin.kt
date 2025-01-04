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
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.reporting.SingleFileReport
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

private const val MIN_KOTLIN_VERSION = "1.6.0"

@Suppress("unused")
class LicensesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val licenseExtension = extensions.create("licenses", LicensesExtension::class.java)

            val licenseReportExtension =
                (licenseExtension as ExtensionAware).extensions.create(
                    "reports",
                    LicenseReportsExtension::class.java,
                )

            plugins.withId("java") {
                configureJavaProject(project, licenseExtension, licenseReportExtension)
            }

            plugins.withId("org.jetbrains.kotlin.multiplatform") {
                configureMultiplatformProject(project, licenseExtension, licenseReportExtension)
            }

            ANDROID_IDS.forEach { id ->
                plugins.withId(id) {
                    configureAndroidProject(project, licenseExtension, licenseReportExtension)
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
            licenseExtension: LicensesExtension,
            licenseReportExtension: LicenseReportsExtension,
        ) {
            project.tasks.register(
                "licenseReport",
                LicensesTask::class.java,
            ) { task ->
                task.addBasicConfiguration(licenseExtension, licenseReportExtension)
            }
        }

        @JvmStatic
        private fun configureAndroidProject(
            project: Project,
            extension: LicensesExtension,
            reportExtension: LicenseReportsExtension,
        ) {
            // check for AGP 7.0+ 'androidComponent' extension
            if (findClass("com.android.build.api.variant.AndroidComponentsExtension") != null) {
                configureAgp7Project(project, extension, reportExtension)
            } else {
                throw GradleException("Minimum Android Gradle Plugin Version is 7.0+")
            }
        }

        @JvmStatic
        private fun configureAgp7Project(
            project: Project,
            extension: LicensesExtension,
            reportExtension: LicenseReportsExtension,
        ) {
            project.logger.info("Using AGP 7.0+ AndroidComponentsExtension")
            val androidComponentsExtension =
                project.extensions.getByType(com.android.build.api.variant.AndroidComponentsExtension::class.java)
            androidComponentsExtension
                .onVariants(androidComponentsExtension.selector().all()) { variant ->

                    project.tasks.register(
                        "license${variant.name.uppercaseFirstChar()}Report",
                        AndroidLicensesTask::class.java,
                    ) { task ->
                        task.addBasicConfiguration(extension, reportExtension)
                        task.variant = variant.name
                        task.buildType = variant.buildType!!
                        task.productFlavors = variant.productFlavors.map { it.second }
                    }
                }
        }

        @JvmStatic
        private fun configureMultiplatformProject(
            project: Project,
            extension: LicensesExtension,
            reportExtension: LicenseReportsExtension,
        ) {
            val kotlinVersion = ComparableVersion(project.getKotlinPluginVersion())

            if (kotlinVersion < ComparableVersion(MIN_KOTLIN_VERSION)) {
                throw GradleException("Using Multiplatform Gradle Plugin v$kotlinVersion not supported")
            }

            val kotlinMultiplatformExtension = project.extensions.getByName("kotlin") as KotlinMultiplatformExtension

            kotlinMultiplatformExtension.targets.all { target ->
                val targetName = target.name
                if (target.platformType == KotlinPlatformType.common) {
                    return@all
                }

                project.tasks.register(
                    "licenseMultiplatform${targetName.uppercaseFirstChar()}Report",
                    KotlinMultiplatformTask::class.java,
                ) { task ->
                    task.addBasicConfiguration(extension, reportExtension)
                    task.targetNames = listOf("common", targetName)
                }
            }

            val targetNames = mutableListOf("common")
            kotlinMultiplatformExtension.targets.all { target ->
                if (target.platformType == KotlinPlatformType.common) {
                    return@all
                }

                targetNames.add(target.name)
            }

            project.tasks.register(
                "licenseMultiplatformReport",
                KotlinMultiplatformTask::class.java,
            ) { task ->
                task.addBasicConfiguration(extension, reportExtension)
                task.targetNames = targetNames
            }
        }

        @JvmStatic
        private fun LicensesTask.addBasicConfiguration(
            extension: LicensesExtension,
            reportExtension: LicenseReportsExtension,
        ) {
            val name = this.name
            additionalProjects = extension.additionalProjects
            description = TASK_DESC
            group = TASK_GROUP
            reports.configureEach { report ->
                when (report.name) {
                    ReportType.HTML.name -> {
                        report as HtmlReport
                        val reporter =
                            reportExtension.html.apply {
                                this.outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.html"))
                            }
                        report.configureReport(reporter)
                        report.css.set(reporter.css)
                        report.useDarkMode.set(reporter.useDarkMode)
                    }

                    ReportType.CSV.name -> {
                        val reporter =
                            reportExtension.csv.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.csv"))
                            }
                        report.configureReport(reporter)
                    }

                    ReportType.JSON.name -> {
                        val reporter =
                            reportExtension.json.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.json"))
                            }
                        report.configureReport(reporter)
                    }

                    ReportType.MARKDOWN.name -> {
                        val reporter =
                            reportExtension.markdown.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.md"))
                            }
                        report.configureReport(reporter)
                    }

                    ReportType.TEXT.name -> {
                        val reporter =
                            reportExtension.plainText.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.txt"))
                            }
                        report.configureReport(reporter)
                    }

                    ReportType.XML.name -> {
                        val reporter =
                            reportExtension.xml.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses.xml"))
                            }
                        report.configureReport(reporter)
                    }

                    ReportType.CUSTOM.name -> {
                        report as CustomReport
                        val reporter =
                            reportExtension.custom.apply {
                                outputFile.convention(project.layout.buildDirectory.file("reports/licenses/$name/licenses"))
                            }

                        report.generator.set(reporter.generator)

                        report.configureReport(reporter)
                    }

                    else -> throw GradleException("Unknown report $name")
                }
            }
        }

        @JvmStatic
        private fun SingleFileReport.configureReport(reporter: Reporter) {
            required.set(reporter.enabled)
            this.outputLocation.set(reporter.outputFile)
        }
    }
}

fun findClass(fqName: String) =
    try {
        Class.forName(fqName)
    } catch (ex: ClassNotFoundException) {
        null
    }
