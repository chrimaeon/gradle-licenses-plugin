/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("HttpUrlsUsage")

package com.cmgapps.license

import com.cmgapps.license.util.cartesianProduct
import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.cmgapps.license.util.hasSameContentAs
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.util.stream.Stream

class LicensePluginAndroidShould {
    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} {1} and {0}")
    @MethodSource("taskNamesAndFixtures")
    fun `generate licenses report for `(
        taskName: String,
        fixture: String,
    ) {
        val fixtureDir = File(fixturesDir, fixture)

        createBuildRunner(fixtureDir, "clean", taskName).build()

        assertExpectedFiles(fixtureDir, taskName)
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} {0}")
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
    fun `generate licenses into csv for for flavor`(taskName: String) {
        val fixture = File(fixturesDir, "android-gradle-plugin-flavors-into-csv")
        createBuildRunner(fixture, taskName).build()

        assertExpectedFiles(fixture, taskName)
    }

    private fun assertExpectedFiles(
        fixtureDir: File,
        taskName: String,
    ) {
        val expectedDir = File(fixtureDir, "expected/$taskName")
        assertThat(expectedDir, anExistingDirectory())

        val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
        assertThat(expectedFiles, not(empty()))
        for (expectedFile in expectedFiles) {
            val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
            assertThat(actualFile, anExistingFile())
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
