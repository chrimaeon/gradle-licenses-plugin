/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern

class LicensePluginAndroidShould {

    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String
    private lateinit var pluginClasspath: String

    @BeforeEach
    fun setUp() {
        val pluginClasspathResource = javaClass.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")
                ?: throw IllegalStateException(
                        "Did not find plugin classpath resource, run `:pluginUnderTestMetadata` task.")
        pluginClasspath = Properties().run {
            load(pluginClasspathResource)
            getProperty("implementation-classpath")
                    .split(':')
                    .map {
                        "'$it'"
                    }
                    .joinToString(", ")
        }

        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "${testProjectDir}/build/reports/licenses"
        mavenRepoUrl = javaClass.getResource("/maven").toURI().toString()
    }

    @Test
    fun `generate licenses debug report`() {

        buildFile.writeText("""
            |buildscript {
            |  repositories {
            |    jcenter()
            |    google()
            |  }
            |  dependencies {
            |    classpath "com.android.tools.build:gradle:3.5.0"
            |    classpath files($pluginClasspath)
            |  }
            |}
            |
            |apply plugin: 'com.android.application'
            |apply plugin: 'com.cmgapps.licenses'
            |android {
            |  compileSdkVersion 28
            |  defaultConfig {
            |    applicationId 'com.example'
            |  }
            |}
            |""".trimMargin())

        for (taskName in listOf("licenseDebugReport", "licenseReleaseReport")) {

            val result = GradleRunner.create()
                    .withProjectDir(testProjectDir.toFile())
                    .withArguments(":$taskName")
                    .build()

            assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
            assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/$taskName/licenses.html.*", Pattern.DOTALL)))
        }
    }

    @Test
    fun `generate licenses variant report`() {
        buildFile.writeText("""
            |buildscript {
            |  repositories {
            |    jcenter()
            |    google()
            |  }
            |  dependencies {
            |    classpath "com.android.tools.build:gradle:3.5.0"
            |    classpath files($pluginClasspath)
            |  }
            |}
            |
            |apply plugin: 'com.android.application'
            |apply plugin: 'com.cmgapps.licenses'
            |android {
            |  compileSdkVersion 28
            |  defaultConfig {
            |    applicationId 'com.example'
            |  }
            |
            |  flavorDimensions "version"
            |  productFlavors {
            |    demo {
            |      dimension "version"
            |    }
            |    full {
            |      dimension "version"
            |    }
            |  }
            |}
            |""".trimMargin())

        for (taskName in listOf("licenseDemoDebugReport",
                "licenseFullDebugReport",
                "licenseDemoReleaseReport",
                "licenseFullReleaseReport")) {


            val result = GradleRunner.create()
                    .withProjectDir(testProjectDir.toFile())
                    .withArguments(":$taskName")
                    .build()

            assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
            assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/$taskName/licenses.html.*", Pattern.DOTALL)))
        }
    }

}