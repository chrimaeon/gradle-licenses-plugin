/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class LicensesPluginShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var reportFolder: String
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        reportFolder = "$testProjectDir/build/reports/licenses/licensesReport"
        project =
            ProjectBuilder
                .builder()
                .withProjectDir(testProjectDir.toFile())
                .build()
        val mavenRepoUrl = javaClass.getResource("/maven")!!.toURI().toString()
        project.repositories.add(
            project.repositories.maven {
                it.setUrl(mavenRepoUrl)
            },
        )
    }

    @Test
    fun `handle java module`() {
        project.plugins.apply("java")
        project.dependencies.add("implementation", "group:name:1.0.0")

        LicensesPlugin().apply(project)

        val outputfile = File(reportFolder, "licenses.csv")

        project.extensions.configure(LicensesExtension::class.java) {
            it.reports {
                it.html.enabled = false
                it.csv {
                    it.enabled = true
                    it.destination = outputfile
                }
            }
        }

        project.tasks
            .withType(LicensesTask::class.java)
            .getByName("licenseReport")
            .licensesReport()

        assertThat(
            outputfile.readText(),
            `is`(
                "Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url\r\n" +
                    "Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/\r\n",
            ),
        )
    }
}
