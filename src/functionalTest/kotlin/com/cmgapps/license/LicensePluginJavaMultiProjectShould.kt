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
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LicensePluginJavaMultiProjectShould {

    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var module1File: File
    private lateinit var module2File: File
    private lateinit var mavenRepoUrl: String
    private lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setUp() {
        Files.createFile(Paths.get(testProjectDir.toString(), "settings.gradle"))
            .toFile() + "include ':module1', ':module2'"
        module1File = Paths.get(testProjectDir.toString(), "module1").toFile().run {
            mkdirs()
            Files.createFile(Paths.get(this.absolutePath, "build.gradle")).toFile()
        }

        module2File = Paths.get(testProjectDir.toString(), "module2").toFile().run {
            mkdirs()
            Files.createFile(Paths.get(this.absolutePath, "build.gradle")).toFile()
        }

        mavenRepoUrl =
            javaClass.getResource("/maven")?.toURI()?.toString() ?: error("""resource folder "\maven" not found!""")
        gradleRunner = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":module1:licenseReport")
            .withPluginClasspath()
            .withJaCoCo()
    }

    @Test
    fun `collect dependencies from additional module`() {
        module1File + """
            plugins {
                id("java")
                id("com.cmgapps.licenses")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            licenses {
                additionalProjects ':module2'
                reports {
                    html.enabled = true
                }
            }
        """.trimIndent()

        module2File + """
            plugins {
                id("java-library")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            dependencies {
                implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        gradleRunner.build()

        assertThat(
            File("$testProjectDir/module1/build/reports/licenses/licenseReport/licenses.html")
                .readText().trim(),
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
                    "<ul><li>Fake dependency name</li></ul><div class=\"license\"><p>Some license</p><a href=\"http://website.tld/\">http://website.tld/</a></div>" +
                    "</body>" +
                    "</html>"
            )
        )
    }

    @Test
    fun `merge dependencies of modules`() {
        module1File + """
            plugins {
                id("java-library")
                id("com.cmgapps.licenses")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            licenses {
                additionalProjects ':module2'
                reports {
                    html.enabled = true
                }
            }
            dependencies {
                implementation 'com.squareup.retrofit2:retrofit:2.3.0'
            }
        """.trimIndent()

        module2File + """
            plugins {
                id("java-library")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            dependencies {
                implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        gradleRunner.build()

        assertThat(
            File("$testProjectDir/module1/build/reports/licenses/licenseReport/licenses.html")
                .readText().trim(),
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
                    "<ul><li>Retrofit</li></ul><pre>" +
                    getFileContent("apache-2.0.txt") +
                    "</pre>" +
                    "<ul><li>Fake dependency name</li></ul><div class=\"license\"><p>Some license</p><a href=\"http://website.tld/\">http://website.tld/</a></div>" +
                    "</body>" +
                    "</html>"
            )
        )
    }

    @Test
    fun `not add already added dependencies`() {
        module1File + """
            plugins {
                id("java")
                id("com.cmgapps.licenses")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            licenses {
                additionalProjects ':module2'
                reports {
                    html.enabled = true
                }
            }
            dependencies {
                implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        module2File + """
            plugins {
                id("java-library")
            }
            repositories {  
                maven { url '$mavenRepoUrl' }
            }
            dependencies {
                implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        gradleRunner.build()

        assertThat(
            File("$testProjectDir/module1/build/reports/licenses/licenseReport/licenses.html")
                .readText().trim(),
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
                    "<ul><li>Fake dependency name</li></ul><div class=\"license\"><p>Some license</p><a href=\"http://website.tld/\">http://website.tld/</a></div>" +
                    "</body>" +
                    "</html>"
            )
        )
    }
}
