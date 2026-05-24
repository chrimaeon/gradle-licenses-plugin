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
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.reporting.SingleFileReport
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import java.io.File

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
                val configs = collectJavaConfigurations(project, licenseExtension.additionalProjects)
                val (resolved, all) = collectAllPomFiles(project, configs)
                task.resolvedPomFiles.from(resolved)
                task.pomFiles.from(all)
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

                        val baseConfigs = collectJavaConfigurations(project, extension.additionalProjects)
                        val androidConfigs =
                            collectAndroidConfigurations(
                                project,
                                extension.additionalProjects,
                                task.buildType,
                                task.productFlavors,
                                task.variant,
                            )
                        val (resolved, all) = collectAllPomFiles(project, baseConfigs + androidConfigs)
                        task.resolvedPomFiles.from(resolved)
                        task.pomFiles.from(all)
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

            kotlinMultiplatformExtension.targets.configureEach { target ->
                val targetName = target.name
                if (target.platformType == KotlinPlatformType.common) {
                    return@configureEach
                }

                project.tasks.register(
                    "licenseMultiplatform${targetName.uppercaseFirstChar()}Report",
                    KotlinMultiplatformTask::class.java,
                ) { task ->
                    task.addBasicConfiguration(extension, reportExtension)
                    task.targetNames = listOf("common", targetName)

                    val baseConfigs = collectJavaConfigurations(project, extension.additionalProjects)
                    val kmpConfigs =
                        collectKmpConfigurations(project, extension.additionalProjects, task.targetNames)
                    val (resolved, all) = collectAllPomFiles(project, baseConfigs + kmpConfigs)
                    task.resolvedPomFiles.from(resolved)
                    task.pomFiles.from(all)
                }
            }

            val targetNames = mutableListOf("common")
            kotlinMultiplatformExtension.targets.configureEach { target ->
                if (target.platformType == KotlinPlatformType.common) {
                    return@configureEach
                }

                targetNames.add(target.name)
            }

            project.tasks.register(
                "licenseMultiplatformReport",
                KotlinMultiplatformTask::class.java,
            ) { task ->
                task.addBasicConfiguration(extension, reportExtension)
                task.targetNames = targetNames

                val baseConfigs = collectJavaConfigurations(project, extension.additionalProjects)
                val kmpConfigs = collectKmpConfigurations(project, extension.additionalProjects, task.targetNames)
                val (resolved, all) = collectAllPomFiles(project, baseConfigs + kmpConfigs)
                task.resolvedPomFiles.from(resolved)
                task.pomFiles.from(all)
            }
        }

        @JvmStatic
        private fun collectJavaConfigurations(
            project: Project,
            additionalProjectPaths: Set<String>,
        ): Set<Configuration> {
            val allProjects = resolveProjects(project, additionalProjectPaths)
            return buildSet {
                allProjects.forEach { proj ->
                    proj.configurations.findByName("compile")?.let { add(it) }
                    proj.configurations.findByName("api")?.let { add(it) }
                    proj.configurations.findByName("implementation")?.let { add(it) }
                }
            }
        }

        @JvmStatic
        private fun collectAndroidConfigurations(
            project: Project,
            additionalProjectPaths: Set<String>,
            buildType: String,
            productFlavors: List<String>,
            variant: String,
        ): Set<Configuration> {
            val allProjects = resolveProjects(project, additionalProjectPaths)
            return buildSet {
                allProjects.forEach { proj ->
                    addAll(addAndroidConfiguration(proj, buildType))
                    productFlavors.forEach { flavor ->
                        if (variant.uppercaseFirstChar().contains(flavor.uppercaseFirstChar())) {
                            addAll(addAndroidConfiguration(proj, flavor))
                        }
                    }
                }
            }
        }

        @JvmStatic
        private fun addAndroidConfiguration(
            project: Project,
            type: String,
        ): Set<Configuration> =
            buildSet {
                project.configurations.find { it.name == "${type}Compile" }?.let { add(it) }
                project.configurations.find { it.name == "${type}Api" }?.let { add(it) }
                project.configurations.find { it.name == "${type}Implementation" }?.let { add(it) }
            }

        @JvmStatic
        private fun collectKmpConfigurations(
            project: Project,
            additionalProjectPaths: Set<String>,
            targetNames: List<String>,
        ): Set<Configuration> {
            val allProjects = resolveProjects(project, additionalProjectPaths)
            return buildSet {
                allProjects.forEach { proj ->
                    targetNames.forEach { name ->
                        proj.configurations.find { it.name == "${name}MainApi" }?.let { add(it) }
                        proj.configurations.find { it.name == "${name}MainImplementation" }?.let { add(it) }
                    }
                }
            }
        }

        @JvmStatic
        private fun resolveProjects(
            project: Project,
            additionalProjectPaths: Set<String>,
        ): Set<Project> {
            val allProjects: Set<Project> = project.rootProject.allprojects
            return setOf(project) +
                additionalProjectPaths
                    .map { path ->
                        allProjects.find { it.path == path }
                            ?: throw IllegalArgumentException("additionalProject $path not found")
                    }.toSet()
        }

        /**
         * Resolves all POM files for the given configurations at configuration time,
         * including parent POM chains. Returns a FileCollection of all POM files.
         */
        @JvmStatic
        internal fun collectAllPomFiles(
            project: Project,
            configurations: Set<Configuration>,
        ): Pair<FileCollection, FileCollection> {
            val reader = MavenXpp3Reader()
            val allPomFiles = mutableMapOf<String, File>()
            val toResolve = mutableSetOf<Dependency>()
            val initialKeys = mutableSetOf<String>()
            val processed = mutableSetOf<String>()

            // Collect initial @pom coordinates from configurations
            configurations.forEach { config ->
                config.incoming.dependencies
                    .filterIsInstance<ExternalDependency>()
                    .forEach { dep ->
                        val key = "${dep.group}:${dep.name}:${dep.version}"
                        initialKeys.add(key)
                        toResolve.add(project.dependencies.create("$key@pom"))
                    }
            }

            // Iteratively resolve POMs and their parent chains
            while (toResolve.isNotEmpty()) {
                val detached =
                    project.configurations
                        .detachedConfiguration(*toResolve.toTypedArray())
                        .apply {
                            isCanBeResolved = true
                            isTransitive = false
                        }
                toResolve.clear()

                detached.resolvedConfiguration.lenientConfiguration.artifacts.forEach { artifact ->
                    val id = artifact.moduleVersion.id
                    val key = "${id.group}:${id.name}:${id.version}"
                    if (processed.add(key)) {
                        allPomFiles[key] = artifact.file
                        try {
                            val model = artifact.file.inputStream().use { reader.read(it) }
                            model.parent?.let { parent ->
                                val parentKey = "${parent.groupId}:${parent.artifactId}:${parent.version}"
                                if (!processed.contains(parentKey)) {
                                    toResolve.add(project.dependencies.create("$parentKey@pom"))
                                }
                            }
                        } catch (_: Exception) {
                            // skip unparseable POMs
                        }
                    }
                }
            }

            val resolvedRootFiles = allPomFiles.filterKeys { key -> initialKeys.contains(key) }.values
            return Pair(project.files(resolvedRootFiles), project.files(allPomFiles.values))
        }

        @JvmStatic
        private fun LicensesTask.addBasicConfiguration(
            extension: LicensesExtension,
            reportExtension: LicenseReportsExtension,
        ) {
            val name = this.name
            additionalProjects.set(extension.additionalProjects)
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

                    else -> {
                        throw GradleException("Unknown report $name")
                    }
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
    } catch (_: ClassNotFoundException) {
        null
    }
