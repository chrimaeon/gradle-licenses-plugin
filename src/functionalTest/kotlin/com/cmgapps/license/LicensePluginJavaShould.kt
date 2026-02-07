/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.cmgapps.license.util.hasSameContentAs
import com.cmgapps.license.util.plus
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LicensePluginJavaShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String
    private lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setUp() {
        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "$testProjectDir/build/reports/licenses/licenseReport"
        mavenRepoUrl = javaClass.getResource("/maven")!!.toURI().toString()
        buildFile +
            // language=gradle
            """
            plugins {
               id("java")
               id("com.cmgapps.licenses")
            }

            repositories {
                maven {
                    url '$mavenRepoUrl'
                }
            }

            """.trimIndent()

        gradleRunner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(":licenseReport", "--info", "--stacktrace")
                .withPluginClasspath()
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0}")
    @ValueSource(
        strings = [MINIMUM_GRADLE_VERSION, "9.1.0", "9.2.0", "9.3.0", LATEST_VERSION],
    )
    fun `apply Licenses plugin to various Gradle versions`(version: String) {
        val result =
            createBuildRunner(
                File(fixturesDir, "apply-license"),
            ).apply {
                if (version != LATEST_VERSION) {
                    withGradleVersion(version)
                }
            }.build()

        assertThat(
            "Gradle version $version",
            result.task(":licenseReport")?.outcome,
            `is`(TaskOutcome.SUCCESS),
        )
    }

    @Test
    fun `generate report with no dependencies`() {
        val result = createBuildRunner(File(fixturesDir, "apply-license-no-dependency")).build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} {0}")
    @ValueSource(
        strings = [
            "java-library-no-open-source-dependency",
            "java-library-parent-pom-dependency",
            "java-library-custom-license",
            "java-library-no-name-license",
            "java-library-html-no-dark-mode",
            "java-library-text-report",
            "java-library-custom-css-style",
            "java-library-custom-report",
            "java-library-handle-complete-dsl",
        ],
    )
    fun `generate report for`(fixture: String) {
        val fixtureDir = File(fixturesDir, fixture)
        createBuildRunner(fixtureDir).build()

        assertExpectedFiles(fixtureDir)
    }

    private fun assertExpectedFiles(fixtureDir: File) {
        val expectedDir = File(fixtureDir, "expected")
        assertThat(expectedDir, anExistingDirectory())

        val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
        assertThat("$expectedDir is emtpy", expectedFiles, not(empty()))
        for (expectedFile in expectedFiles) {
            val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
            assertThat(actualFile, anExistingFile())
            assertThat(actualFile, hasSameContentAs(expectedFile))
        }
    }
}

private const val LATEST_VERSION = "latest"
