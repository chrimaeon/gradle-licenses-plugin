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

import com.cmgapps.license.util.plus
import com.cmgapps.license.util.write
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties

const val AGP = "com.android.tools.build:gradle:3.5.3"

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
                "Did not find plugin classpath resource, run `:pluginUnderTestMetadata` task."
            )
        pluginClasspath = Properties().run {
            load(pluginClasspathResource)
            getProperty("implementation-classpath")
                .split(':')
                .joinToString(", ") {
                    "'$it'"
                }
        }

        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "$testProjectDir/build/reports/licenses"
        mavenRepoUrl = javaClass.getResource("/maven").toURI().toString()

        buildFile + """
            buildscript {
              repositories {
                jcenter()
                google()
              }
              dependencies {
                classpath "$AGP"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.application'
            apply plugin: 'com.cmgapps.licenses'

        """.trimIndent()
    }

    @Test
    fun `generate licenses buildType report`() {

        buildFile + """
            android {
              compileSdkVersion 28
              defaultConfig {
                applicationId 'com.example'
              }
            }
            """.trimIndent()

        for (taskName in listOf("licenseDebugReport", "licenseReleaseReport")) {

            val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(":$taskName")
                .build()

            assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
        }
    }

    @Test
    fun `generate licenses variant report`() {
        buildFile + """
            android {
              compileSdkVersion 28
              defaultConfig {
                applicationId 'com.example'
              }
            
              flavorDimensions "version"
              productFlavors {
                demo {
                  dimension "version"
                }
                full {
                  dimension "version"
                }
              }
            }
            """.trimIndent()

        for (taskName in listOf(
            "licenseDemoDebugReport",
            "licenseFullDebugReport",
            "licenseDemoReleaseReport",
            "licenseFullReleaseReport"
        )) {

            val result = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(":$taskName")
                .build()

            assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
        }
    }

    @Test
    fun `generate Report for selected configuration`() {
        buildFile + """
            import com.cmgapps.license.OutputType
            repositories {
                maven {
                url '$mavenRepoUrl'
              }
            }
            android {
                compileSdkVersion 28
                defaultConfig {
                    applicationId 'com.example'
                }
            }
            
            licenses {
                outputType OutputType.TEXT
            }
            
            dependencies {
                implementation 'group:name:1.0.0'
                debugImplementation 'group:noname:1.0.0'
                releaseImplementation 'com.squareup.retrofit2:retrofit:2.3.0'
            }
        """.trimIndent()

        GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseDebugReport")
            .withPluginClasspath()
            .build()

        assertThat(
            File("$reportFolder/licenseDebugReport/licenses.txt").readText().trim(),
            `is`(
                "Licenses\n" +
                    "├─ Fake dependency name:1.0.0\n" +
                    "│  ├─ License: Some license\n" +
                    "│  └─ URL: http://website.tld/\n" +
                    "└─ group:noname:1.0.0\n" +
                    "   ├─ License: Some license\n" +
                    "   └─ URL: http://website.tld/"
            )
        )
    }

    @Test
    fun `should work for LibraryPlugin`() {
        buildFile.write(
            """
            buildscript {
              repositories {
                jcenter()
                google()
              }
              dependencies {
                classpath "$AGP"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.library'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
            }
        """.trimIndent()
        )

        val taskName = ":licenseDebugReport"
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(taskName)
            .withPluginClasspath()
            .build()

        assertThat(result.task(taskName)?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `should work for DynamicFeaturePlugin`() {
        buildFile.write(
            """
            buildscript {
              repositories {
                jcenter()
                google()
              }
              dependencies {
                classpath "$AGP"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.dynamic-feature'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
            }
        """.trimIndent()
        )

        val taskName = ":licenseDebugReport"
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(taskName)
            .withPluginClasspath()
            .build()

        assertThat(result.task(taskName)?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `should work for FeaturePlugin`() {
        buildFile.write(
            """
            buildscript {
              repositories {
                jcenter()
                google()
              }
              dependencies {
                classpath "$AGP"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.feature'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
            }
        """.trimIndent()
        )

        val taskName = ":licenseDebugFeatureReport"
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(taskName)
            .withPluginClasspath()
            .build()

        assertThat(result.task(taskName)?.outcome, `is`(TaskOutcome.SUCCESS))
    }
}
