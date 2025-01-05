/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.getFileContent
import com.cmgapps.license.util.plus
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

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - Gradle Version = {0}")
    @ValueSource(
        strings = [
            "7.2", "7.3", "7.4", "7.5", "7.6", "8.0", "8.1", "8.2", "8.3", "8.4",
            "8.5", "8.6", "8.7", "8.8", "8.9", "8.10", "8.11", "8.12",
        ],
    )
    fun `apply Licenses plugin to various Gradle versions`(version: String) {
        val result =
            gradleRunner
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
        buildFile +
            // language=gradle
            """
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
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"color-scheme\" content=\"dark light\">" +
                    "<style>" +
                    "body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}" +
                    "@media(prefers-color-scheme: dark){body{background-color: #303030}pre,.license {background-color: #242424}}" +
                    "</style>" +
                    "<title>Open source licenses</title>" +
                    "</head>" +
                    "<body>" +
                    "<h3>Notice for packages:</h3>" +
                    "</body>" +
                    "</html>",
            ),
        )
    }

    @Test
    fun `java library with parent pom dependency`() {
        buildFile +
            // language=gradle
            """
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
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"color-scheme\" content=\"dark light\">" +
                    "<style>" +
                    "body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}" +
                    "@media(prefers-color-scheme: dark){body{background-color: #303030}pre,.license {background-color: #242424}}" +
                    "</style>" +
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate Report with custom license`() {
        buildFile +
            // language=gradle
            """
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
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"color-scheme\" content=\"dark light\">" +
                    "<style>" +
                    "body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}" +
                    "@media(prefers-color-scheme: dark){body{background-color: #303030}pre,.license {background-color: #242424}}" +
                    "</style>" +
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate Report with lib with no name`() {
        buildFile +
            // language=gradle
            """
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
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<meta name=\"color-scheme\" content=\"dark light\">" +
                    "<style>" +
                    "body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}" +
                    "@media(prefers-color-scheme: dark){body{background-color: #303030}pre,.license {background-color: #242424}}" +
                    "</style>" +
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate Html report with no dark theme`() {
        buildFile +
            // language=gradle
            """
            licenses {
                reports {
                    html.enabled.set(true)
                    html.useDarkMode.set(false)
                }
            }
            
            dependencies {
              implementation 'group:noname:1.0.0'
            }
            """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
                "<!DOCTYPE html>" +
                    "<html lang=\"en\">" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<style>body{font-family:sans-serif;background-color:#eee}" +
                    "pre,.license{background-color:#ddd;padding:1em}" +
                    "pre{white-space:pre-wrap}" +
                    "</style>" +
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate TXT Report`() {
        buildFile +
            // language=gradle
            """
            licenses {
                reports {
                    plainText.enabled.set(true)
                }
            }

            dependencies {
              implementation 'group:noname:1.0.0'
            }
            """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(
                Pattern.compile(
                    ".*licenses.txt report saved to .*$reportFolder/licenses.txt.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.txt").readText().trim(),
            `is`(
                "Licenses\n" +
                    "└─ group:noname:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/",
            ),
        )
    }

    @Test
    fun `generate Report with different html styles`() {
        buildFile +
            //language=gradle
            """
            licenses {
                reports {
                    html.enabled.set(true)
                    html.stylesheet("body{}")
                    html.useDarkMode.set(false)
                }
            }

            dependencies {
              implementation 'group:name:1.0.0'
            }
            """.trimIndent()

        val result = gradleRunner.build()

        assertThat(
            result.output,
            matchesPattern(
                Pattern.compile(
                    ".*licenses.html report saved to .*$reportFolder/licenses.html.*",
                    Pattern.DOTALL,
                ),
            ),
        )
        assertThat(
            File("$reportFolder/licenses.html").readText().trim(),
            `is`(
                // language=html
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
                    "</html>",
            ),
        )
    }

    @Test
    fun `generate custom report`() {
        buildFile +
            // language=gradle
            """
            licenses {
                reports {
                    custom.enabled.set(true)
                    com.cmgapps.license.reporter.CustomReportGenerator builder = { list -> list.collect { it.name }.join(', ') }
                    custom.generator.set(builder)
                }
            }

            dependencies {
              implementation 'group:name:1.0.0'
            }
            """.trimIndent()

        val result = gradleRunner.withDebug(true).build()

        assertThat(
            result.output,
            matchesPattern(Pattern.compile(".*licenses report saved to .*$reportFolder/licenses.*", Pattern.DOTALL)),
        )
        assertThat(File("$reportFolder/licenses").readText().trim(), `is`("Fake dependency name"))
    }

    @Test
    fun `handle DSL`() {
        buildFile +
            // language=gradle
            """
            licenses {
                reports {
                    csv {
                        enabled.set(true)
                    }
                    custom {
                        enabled.set(true)
                        com.cmgapps.license.reporter.CustomReportGenerator builder = { "" }
                        generator.set(builder)
                    }
                    html {
                        enabled.set(true)
                        stylesheet("body {background: #FAFAFA}")
                    }
                    json {
                        enabled.set(true)
                    }
                    markdown {
                        enabled.set(true)
                    }
                    plainText {
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
