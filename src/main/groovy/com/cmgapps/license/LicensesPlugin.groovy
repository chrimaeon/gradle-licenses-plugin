package com.cmgapps.license

import org.gradle.api.Plugin
import org.gradle.api.Project

final class LicensesPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        final def taskName = "licenseReport"
        final def path = "${project.buildDir}/reports/licenses/$taskName"

        LicensesTask task = project.tasks.create("$taskName".toString(), LicensesTask)
        task.description = 'Collect licenses from libraries'
        task.group = 'Reporting'
        task.htmlFile = project.file(path + '.html')
    }
}
