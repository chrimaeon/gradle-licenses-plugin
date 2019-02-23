/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import com.android.build.gradle.*
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DomainObjectSet
import org.gradle.api.Plugin
import org.gradle.api.Project

class LicensesPlugin : Plugin<Project> {

    companion object {
        private const val TASK_DESC = "Collect licenses from libraries"
        private const val TASK_GROUP = "Reporting"
        private const val APP_PLUGIN_ID = "com.android.application"
        private const val LIBRARY_PLUGIN_ID = "com.android.library"
        private const val FEATURE_PLUGIN_ID = "com.android.feature"
        private val ANDROID_IDS = listOf(APP_PLUGIN_ID, LIBRARY_PLUGIN_ID, FEATURE_PLUGIN_ID)

        private fun configureJavaProject(project: Project) {
            val taskName = "licenseReport"
            val path = "${project.buildDir}/reports/licenses/$taskName/"

            val task = project.tasks.create(taskName, LicensesTask::class.java)
            task.htmlFile = project.file(path + "licenses.html")
            task.description = TASK_DESC
            task.group = TASK_GROUP
            task.outputs.upToDateWhen { false }
        }

        private fun configureAndroidProject(project: Project) {
            getAndroidVariants(project)?.all { variant ->
                variant as BaseVariant
                val taskName = "license${variant.name.capitalize()}Report"
                val path = "${project.buildDir}/reports/licenses/$taskName/"

                val task = project.tasks.create(taskName, LicensesTask::class.java)
                task.htmlFile = project.file(path + "licenses.html")
                task.description = TASK_DESC
                task.group = TASK_GROUP
                task.variant = variant.name
                task.buildType = variant.buildType.name
                task.productFlavors = variant.productFlavors

                task.outputs.upToDateWhen { false }
            }
        }

        private fun getAndroidVariants(project: Project): DomainObjectSet<*>? {
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
    }

    override fun apply(project: Project) {

        project.plugins.withId("java") { configureJavaProject(project) }

        ANDROID_IDS.forEach { id ->
            project.plugins.withId(id) { configureAndroidProject(project) }
        }
    }
}
