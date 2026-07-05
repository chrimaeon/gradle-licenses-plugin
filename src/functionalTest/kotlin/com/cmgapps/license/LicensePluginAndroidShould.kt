/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.assertExpectedFiles
import com.cmgapps.license.util.cartesianProduct
import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
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
    @MethodSource("androidTasks")
    fun `generate licenses report for AGP 8 task `(taskName: String) {
        val fixtureDir = File(fixturesDir, "android-gradle-plugin-8")
        createBuildRunner(fixtureDir, "clean", taskName)
            .apply {
                withGradleVersion("9.5.0")
            }.build()
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

    companion object {
        @JvmStatic
        fun taskNamesAndFixtures(): Stream<Arguments> =
            androidTasks().cartesianProduct(
                listOf(
                    "android-gradle-plugin-9",
                    "android-gradle-plugin-library",
                    "android-gradle-plugin-dynamic-feature",
                ),
            )

        @JvmStatic
        fun androidTasks(): Stream<Arguments> = listOf("licenseDebugReport", "licenseReleaseReport").stream().map(::arguments)
    }
}
