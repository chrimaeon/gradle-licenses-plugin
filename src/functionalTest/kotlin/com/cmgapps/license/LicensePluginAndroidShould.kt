/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.*
import java.util.regex.Pattern

class LicensePluginAndroidShould {

    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String
    private lateinit var pluginClasspath: String

    @Before
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

        buildFile = testProjectDir.newFile("build.gradle")
        reportFolder = "${testProjectDir.root.path}/build/reports/licenses"
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
            |    classpath "com.android.tools.build:gradle:3.3.0"
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
                    .withProjectDir(testProjectDir.root)
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
            |    classpath "com.android.tools.build:gradle:3.3.0"
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
                    .withProjectDir(testProjectDir.root)
                    .withArguments(":$taskName")
                    .build()

            assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
            assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/$taskName/licenses.html.*", Pattern.DOTALL)))
        }
    }

}