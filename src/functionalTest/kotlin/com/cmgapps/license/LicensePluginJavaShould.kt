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

import com.cmgapps.license.util.getFileContent
import com.cmgapps.license.util.plus
import com.cmgapps.license.util.withJaCoCo
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
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
    private lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setUp() {
        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "$testProjectDir/build/reports/licenses/licenseReport"
        mavenRepoUrl = javaClass.getResource("/maven")!!.toURI().toString()
        buildFile + """
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

        gradleRunner = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .withJaCoCo()
    }

    @DisabledIfEnvironmentVariable(named = "CIRCLECI", matches = "true")
    @ParameterizedTest
    @ValueSource(strings = ["7.0.1"])
    fun `apply Licenses plugin to various Gradle versions`(version: String) {
        val result = gradleRunner
            .withGradleVersion(version)
            .build()

        assertThat("Gradle version $version", result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `generate report with no dependencies`() {
        val result = gradleRunner.build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `generate report with no open source dependencies`() {
        buildFile + """
            licenses {
                reports {
                    html.enabled.set(true)
                }
            }
            dependencies {
              implementation 'com.google.firebase:firebase-core:10.0.1'
            }
        """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "</body>" +
                    "</html>"
            )
        )
    }

    @Test
    fun `java library with parent pom dependency`() {
        buildFile + """
            licenses {
                reports {
                    html.enabled.set(true)
                }
            }
            
            dependencies {
              implementation 'com.squareup.retrofit2:retrofit:2.3.0'
            }
        """.trimIndent()
        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                "<!DOCTYPE html>" +
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
                    getFileContent("apache-2.0.txt") +
                    "</pre>" +
                    "</body>" +
                    "</html>"
            )
        )
    }

    @Test
    fun `generate Report with custom license`() {
        buildFile + """
            licenses {
                reports {
                    html.enabled.set(true)
                }
            }
            
            dependencies {
              implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                "<!DOCTYPE html>" +
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
                    "</html>"
            )
        )
    }

    @Test
    fun `generate Report with lib with no name`() {
        buildFile + """
            licenses {
                reports {
                    html.enabled.set(true)
                }
            }
            
            dependencies {
              implementation 'group:noname:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                "<!DOCTYPE html>" +
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
                    "</html>"
            )
        )
    }

    @Test
    fun `generate TXT Report`() {
        buildFile + """
            licenses {
                reports {
                    text.enabled.set(true)
                }
            }

            dependencies {
              implementation 'group:noname:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote TEXT report to .*$reportFolder/licenses.txt.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.txt").readText().trim(),
            `is`(
                "Licenses\n" +
                    "└─ group:noname:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/"
            )
        )
    }

    @Test
    fun `generate Report with different html styles`() {
        buildFile + """
            licenses {
                reports {
                    html.enabled.set(true)
                    html.stylesheet.set(project.resources.text.fromString("body{}"))
                }
            }

            dependencies {
              implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote HTML report to .*$reportFolder/licenses.html.*", Pattern.DOTALL))
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{}</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "<ul><li>Fake dependency name</li></ul>" +
                    "<div class=\"license\">" +
                    "<p>Some license</p>" +
                    "<a href=\"http://website.tld/\">http://website.tld/</a>" +
                    "</div>" +
                    "</body>" +
                    "</html>"
            )
        )
    }

    @Test
    fun `generate custom report`() {
        buildFile + """
            licenses {
                reports {
                    custom.enabled.set(true)
                    custom.generate { list -> list.collect { it.name }.join(', ') }
                }
            }

            dependencies {
              implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.withDebug(true).build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*Wrote CUSTOM report to .*$reportFolder/licenses.*", Pattern.DOTALL))
        )
        assertThat(File("$reportFolder/licenses").readText().trim(), `is`("Fake dependency name"))
    }

    @Test
    fun `handle DSL`() {
        buildFile + """
            licenses {
                reports {
                    csv {
                        enabled.set(true)
                    }
                    custom {
                        enabled.set(true)
                        generate {}
                    }
                    html {
                        enabled.set(true)
                        stylesheet.set(resources.text.fromString("body {background: #FAFAFA}"))
                    }
                    json {
                        enabled.set(true)
                    }
                    markdown {
                        enabled.set(true)
                    }
                    text {
                        enabled.set(true)
                    }
                    xml {
                        enabled.set(true)
                    }
                }
            }

            dependencies {
              implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        val result = gradleRunner.withDebug(true).build()

        assertThat(result.task(":licenseReport")?.outcome, `is`(TaskOutcome.SUCCESS))

    }
}
