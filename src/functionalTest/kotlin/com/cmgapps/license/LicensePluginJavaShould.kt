/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.assertExpectedFiles
import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.cmgapps.license.util.hasSameContentAs
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.stream.Stream

class LicensePluginJavaShould {
    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0}")
    @MethodSource("gradleVersions")
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

    companion object {
        @JvmStatic
        fun gradleVersions(): Stream<Arguments> =
            buildList {
                add(MINIMUM_GRADLE_VERSION)
                add(LATEST_VERSION)
                if (System.getenv("CI") == null) {
                    add("9.1.0")
                    add("9.2.0")
                    add("9.3.0")
                }
            }.stream().map { arguments(it) }
    }
}

private const val LATEST_VERSION = "latest"
