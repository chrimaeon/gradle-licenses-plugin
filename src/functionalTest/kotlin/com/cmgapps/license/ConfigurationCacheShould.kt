/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

class ConfigurationCacheShould {
    @Disabled(
        "every run has to recalculate graph because:\n" +
            "Calculating task graph as configuration cache cannot be reused because an input to task ':gradle-licenses-plugin:jar' has changed.",
    )
    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0}")
    @MethodSource("gradleVersions")
    fun `apply Licenses plugin to various Gradle versions`(version: String) {
        var result =
            createBuildRunner(
                File(fixturesDir, "apply-license"),
                args = arrayOf("--configuration-cache", "clean", ":licenseReport"),
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
        assertThat(
            "Gradle version $version",
            result.output,
            containsString("Configuration cache entry stored."),
        )

        result =
            createBuildRunner(
                File(fixturesDir, "apply-license"),
                args = arrayOf("--configuration-cache", ":licenseReport"),
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
        assertThat(
            "Gradle version $version",
            result.output,
            containsString("Configuration cache entry reused."),
        )
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
                    add("9.4.0")
                }
            }.stream().map { arguments(it) }
    }
}

private const val LATEST_VERSION = "latest"
