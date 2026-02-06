/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("HttpUrlsUsage")

package com.cmgapps.license

import com.cmgapps.license.util.cartesianProduct
import com.cmgapps.license.util.fixturesDir
import com.cmgapps.license.util.hasSameContentAs
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.stream.Stream

class LicensePluginAndroidShould {
    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} {1} and :{0}")
    @MethodSource("taskNamesAndFixtures")
    fun `generate licenses report for `(
        taskName: String,
        fixture: String,
    ) {
        val fixtureDir = File(fixturesDir, fixture)

        val result = createBuildRunner(fixtureDir).withArguments(":$taskName").build()

        assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertExpectedFiles(fixtureDir)
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - taskName = {0}")
    @ValueSource(
        strings = [
            "licenseDemoGoogleDebugReport",
            "licenseDemoAmazonDebugReport",
            "licenseFullGoogleDebugReport",
            "licenseFullAmazonDebugReport",
            "licenseDemoGoogleReleaseReport",
            "licenseDemoAmazonReleaseReport",
            "licenseFullGoogleReleaseReport",
            "licenseFullAmazonReleaseReport",
        ],
    )
    fun `generate licenses for flavors into csv`(taskName: String) {
        val fixture = File(fixturesDir, "android-gradle-plugin-flavors-into-csv")
        val result =
            createBuildRunner(fixture)
                .withArguments(":$taskName", "--stacktrace")
                .build()

        assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertExpectedFiles(fixture)
    }

    private fun createBuildRunner(fixtureDir: File): GradleRunner =
        GradleRunner
            .create()
            .withDebug(true)
            .withProjectDir(fixtureDir)

    private fun assertExpectedFiles(fixtureDir: File) {
        val expectedDir = File(fixtureDir, "expected")
        if (!expectedDir.exists()) {
            throw AssertionError("Missing expected/ directory")
        }

        val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
        assertThat(expectedFiles, not(empty()))
        for (expectedFile in expectedFiles) {
            val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
            if (!actualFile.exists()) {
                throw AssertionError("Expected $actualFile but does not exist")
            }
            assertThat(actualFile, hasSameContentAs(expectedFile))
        }
    }

    companion object {
        @JvmStatic
        fun taskNamesAndFixtures(): Stream<Arguments> =
            listOf("licenseDebugReport", "licenseReleaseReport").cartesianProduct(
                listOf(
                    "android-gradle-plugin-8",
                    "android-gradle-plugin-9",
                    "android-gradle-plugin-library",
                    "android-gradle-plugin-dynamic-feature",
                ),
            )
    }
}
