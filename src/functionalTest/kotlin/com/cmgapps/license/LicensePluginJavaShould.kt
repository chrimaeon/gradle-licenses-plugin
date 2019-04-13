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

import com.cmgapps.license.util.TestUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern

class LicensePluginJavaShould {

    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String

    @BeforeEach
    fun setUp() {
        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "${testProjectDir}/build/reports/licenses/licenseReport"
        mavenRepoUrl = javaClass.getResource("/maven").toURI().toString()
    }

    @ParameterizedTest
    @ValueSource(strings = ["3.5", "4.0", "4.1", "4.5", "5.0", "5.1", "5.2"])
    fun `apply Licenses plugin to various Gradle versions`(version: String) {
        buildFile.writeText("""
            |plugins {
            |   id("java")
            |   id("com.cmgapps.licenses")
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withGradleVersion(version)
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .build()

        assertThat("Gradle version $version", result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat("Gradle version $version", result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
    }

    @Test
    fun `generate report with no dependencies`() {
        buildFile.writeText("""
            |plugins {
            |   id("java-library")
            |   id("com.cmgapps.licenses")
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withGradleVersion("5.2")
            .build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
    }

    @Test
    fun `generate report with no open source dependencies`() {
        buildFile.writeText("""
            |plugins {
            |   id("java-library")
            |   id("com.cmgapps.licenses")
            |}
            |repositories {
            |  maven {
            |    url '$mavenRepoUrl'
            |  }
            |}
            |dependencies {
            |  compile 'com.google.firebase:firebase-core:10.0.1'
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withGradleVersion("5.2")
            .build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
        assertThat(File("$reportFolder/licenses.html").readText().trim(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "</body>" +
            "</html>")
        )
    }

    @Test
    fun `java library with parent pom dependency`() {
        buildFile.writeText("""
            |plugins {
            |   id("java-library")
            |   id("com.cmgapps.licenses")
            |}
            |repositories {
            |  maven {
            |    url '$mavenRepoUrl'
            |  }
            |}
            |dependencies {
            |  compile 'com.squareup.retrofit2:retrofit:2.3.0'
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withGradleVersion("5.2")
            .build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
        assertThat(File("$reportFolder/licenses.html").readText().trim(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "<ul>" +
            "<li>Retrofit</li>" +
            "</ul>" +
            "<pre>" +
            TestUtils.getFileContent("apache-2.0.txt") +
            "</pre>" +
            "</body>" +
            "</html>")
        )
    }

    @Test
    fun `generate Report with custom license`() {
        buildFile.writeText("""
            |plugins {
            |   id("java-library")
            |   id("com.cmgapps.licenses")
            |}
            |repositories {
            |  maven {
            |    url '$mavenRepoUrl'
            |  }
            |}
            |dependencies {
            |  compile 'group:name:1.0.0'
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withGradleVersion("5.2")
            .build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
        assertThat(File("$reportFolder/licenses.html").readText().trim(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "<ul>" +
            "<li>Fake dependency name</li>" +
            "</ul>" +
            "<div class=\"license\">" +
            "<p>Some license</p>" +
            "<a href=\"http://website.tld/\">http://website.tld/</a>" +
            "</div>" +
            "</body>" +
            "</html>")
        )
    }

    @Test
    fun `generate Report with lib with no name`() {
        buildFile.writeText("""
            |plugins {
            |   id("java-library")
            |   id("com.cmgapps.licenses")
            |}
            |repositories {
            |  maven {
            |    url '$mavenRepoUrl'
            |  }
            |}
            |dependencies {
            |  compile 'group:noname:1.0.0'
            |}
        """.trimMargin())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withGradleVersion("5.2")
            .build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
        assertThat(result.output, matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL)))
        assertThat(File("$reportFolder/licenses.html").readText().trim(), `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "<ul>" +
            "<li>group:noname</li>" +
            "</ul>" +
            "<div class=\"license\">" +
            "<p>Some license</p>" +
            "<a href=\"http://website.tld/\">http://website.tld/</a>" +
            "</div>" +
            "</body>" +
            "</html>")
        )
    }
}

