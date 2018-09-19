package com.cmgapps.license

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.Plugin
import org.gradle.api.Project

final class LicensesPlugin implements Plugin<Project> {

    private static final def TASK_DESC = 'Collect licenses from libraries'
    private static final def TASK_GROUP = 'Reporting'

    private static final def ANDROID_IDS = [
            "com.android.application",
            "com.android.feature",
            "com.android.instantapp",
            "com.android.library",
            "com.android.test"]

    @Override
    void apply(Project project) {

        project.plugins.withId("java") { configureJavaProject(project) }

        ANDROID_IDS.each { id ->
            project.plugins.withId(id) { configureAndroidProject(project) }
        }
    }

    private static configureJavaProject(Project project) {
        final def taskName = "licenseReport"
        final def path = "${project.buildDir}/reports/licenses/${taskName}/"

        LicensesTask task = project.tasks.create("$taskName".toString(), LicensesTask)
        task.description = TASK_DESC
        task.group = TASK_GROUP
        task.htmlFile = project.file(path + 'licenses.html')

        task.outputs.upToDateWhen { false }
    }

    private static configureAndroidProject(Project project) {
        def variants = getAndroidVariants(project)
        variants.all { BaseVariant variant ->
            final def variantName = variant.name.capitalize()
            final def taskName = "license${variantName}Report"
            final def path = "${project.buildDir}/reports/licenses/${taskName}/"

            final LicensesTask task = project.tasks.create("$taskName".toString(), LicensesTask)
            task.description = TASK_DESC
            task.group = TASK_GROUP
            task.htmlFile = project.file(path + 'licenses.html')
            task.variant = variant.name
            task.buildType = variant.buildType.name
            task.productFlavors = variant.productFlavors

            task.outputs.upToDateWhen { false }
        }
    }

    private static getAndroidVariants(Project project) {
        (project.android.hasProperty("libraryVariants") ? project.android.libraryVariants : project.android.applicationVariants)
    }
}
