/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import java.util.stream.Stream

const val AGP_7_x = "com.android.tools.build:gradle:7.2.1"
const val AGP_4_x = "com.android.tools.build:gradle:4.0.1"

class LicensePluginAndroidShould {

    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String
    private lateinit var pluginClasspath: String
    private lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setUp() {
        val pluginClasspathResource = javaClass.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")
            ?: throw IllegalStateException(
                "Did not find plugin classpath resource, run `:pluginUnderTestMetadata` task.",
            )
        pluginClasspath = Properties().run {
            load(pluginClasspathResource)
            getProperty("implementation-classpath").split(':').joinToString(", ") {
                "'$it'"
            }
        }

        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "$testProjectDir/build/reports/licenses"
        mavenRepoUrl =
            javaClass.getResource("/maven")?.toURI()?.toString() ?: error("""resource folder "/maven" not found!""")

        buildFile + """
            buildscript {
              repositories {
                mavenCentral()
                google()
              }
              dependencies {
                classpath "$AGP_7_x"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.application'
            apply plugin: 'com.cmgapps.licenses'

        """.trimIndent()

        gradleRunner = GradleRunner.create().withProjectDir(testProjectDir.toFile())
    }

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - taskName = {0}, AGP = {1}")
    @MethodSource("buildTypesAndAgpVersions")
    fun `generate licenses buildType report`(taskName: String, agbVersion: String) {
        buildFile.write(
            """
            buildscript {
              repositories {
                mavenCentral()
                google()
              }
              dependencies {
                classpath "$agbVersion"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.application'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
              defaultConfig {
                applicationId 'com.example'
              }
            }
            """.trimIndent(),
        )

        val result = gradleRunner.withArguments(":$taskName").build()

        assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - taskName = {0}")
    @MethodSource("productFlavorsAndCsv")
    fun `generate licenses variant report`(taskName: String, licensesAsCsvExpected: String) {
        buildFile + """
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
              
                flavorDimensions "version", "store"
                productFlavors {
                  demo {
                    dimension "version"
                  }
                  full {
                    dimension "version"
                  }
                  google {
                    dimension "store"
                  }
                  amazon {
                    dimension "store"
                  }
                }
              }
              
              licenses {
                reports {
                  html.enabled = false
                  csv.enabled = true
                }
              }
              dependencies {
                implementation 'group:name:1.0.0'
                demoImplementation 'group:noname:1.0.0'
                fullImplementation 'group:multilicenses:1.0.0'
                googleImplementation 'group:foo:1.0.0'
                amazonImplementation 'group:bar:1.0.0'
                releaseImplementation 'com.squareup.retrofit2:retrofit:2.3.0'
                debugImplementation 'group:zet:1.0.0'
              }
        """.trimIndent()

        gradleRunner
            .withArguments(":$taskName")
            .build()

        assertThat(File("$reportFolder/$taskName/licenses.csv").readText(), `is`(licensesAsCsvExpected))
    }

    @Test
    fun `generate Report for selected configuration`() {
        buildFile + """
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
                reports {
                    text.enabled = true
                }
            }
            
            dependencies {
                implementation 'group:name:1.0.0'
                debugImplementation 'group:noname:1.0.0'
                releaseImplementation 'com.squareup.retrofit2:retrofit:2.3.0'
            }
        """.trimIndent()

        gradleRunner.withArguments(":licenseDebugReport").build()

        assertThat(
            File("$reportFolder/licenseDebugReport/licenses.txt").readText().trim(),
            `is`(
                "Licenses\n" + "├─ Fake dependency name:1.0.0\n" + "│  ├─ License: Some license\n" + "│  └─ URL: http://website.tld/\n" + "└─ group:noname:1.0.0\n" + "   ├─ License: Some license\n" + "   └─ URL: http://website.tld/",
            ),
        )
    }

    @Test
    fun `should work for LibraryPlugin`() {
        buildFile.write(
            """
            buildscript {
              repositories {
                mavenCentral()
                google()
              }
              dependencies {
                classpath "$AGP_7_x"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.library'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
            }
            """.trimIndent(),
        )

        val taskName = ":licenseDebugReport"
        val result = gradleRunner.withArguments(taskName).build()

        assertThat(result.task(taskName)?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `should work for DynamicFeaturePlugin`() {
        buildFile.write(
            """
            buildscript {
              repositories {
                mavenCentral()
                google()
              }
              dependencies {
                classpath "$AGP_7_x"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'com.android.dynamic-feature'
            apply plugin: 'com.cmgapps.licenses'

            android {
              compileSdkVersion 28
            }
            """.trimIndent(),
        )

        val taskName = ":licenseDebugReport"
        val result = gradleRunner.withArguments(taskName).build()

        assertThat(result.task(taskName)?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    companion object {
        @JvmStatic
        fun buildTypesAndAgpVersions(): Stream<Arguments> =
            listOf("licenseDebugReport", "licenseReleaseReport").cartesianProduct(listOf(AGP_4_x, AGP_7_x))

        @JvmStatic
        fun productFlavorsAndCsv(): Stream<Arguments> = Stream.of(
            arguments(
                "licenseDemoGoogleDebugReport",
                LICENSE_DEMO_GOOGLE_DEBUG_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseDemoAmazonDebugReport",
                LICENSE_DEMO_AMAZON_DEBUG_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseFullGoogleDebugReport",
                LICENSE_FULL_GOOGLE_DEBUG_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseFullAmazonDebugReport",
                LICENSE_FULL_AMAZON_DEBUG_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseDemoGoogleReleaseReport",
                LICENSE_DEMO_GOOGLE_RELEASE_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseDemoAmazonReleaseReport",
                LICENSE_DEMO_AMAZON_RELEASE_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseFullGoogleReleaseReport",
                LICENSE_FULL_GOOGLE_RELEASE_CSV.replace("\n", "\r\n"),
            ),
            arguments(
                "licenseFullAmazonReleaseReport",
                LICENSE_FULL_AMAZON_RELEASE_CSV.replace("\n", "\r\n"),
            ),
        )
    }
}

internal fun <S, T> List<S>.cartesianProduct(other: List<T>): Stream<Arguments> = this.flatMap { s1 ->
    other.map { s2 ->
        arguments(s1, s2)
    }
}.stream()

const val LICENSE_DEMO_GOOGLE_DEBUG_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Foo,1.0.0,group:foo:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Zet,1.0.0,group:zet:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
,1.0.0,group:noname:1.0.0,Fake dependency description,,Some license,http://website.tld/
"""
const val LICENSE_DEMO_AMAZON_DEBUG_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Bar,1.0.0,group:bar:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Zet,1.0.0,group:zet:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
,1.0.0,group:noname:1.0.0,Fake dependency description,,Some license,http://website.tld/
"""
const val LICENSE_FULL_GOOGLE_DEBUG_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Foo,1.0.0,group:foo:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,BSD-2-Clause,BSD-2-Clause,https://opensource.org/licenses/BSD-2-Clause
Zet,1.0.0,group:zet:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
"""
const val LICENSE_FULL_AMAZON_DEBUG_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Bar,1.0.0,group:bar:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,BSD-2-Clause,BSD-2-Clause,https://opensource.org/licenses/BSD-2-Clause
Zet,1.0.0,group:zet:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
"""
const val LICENSE_DEMO_GOOGLE_RELEASE_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Foo,1.0.0,group:foo:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Retrofit,2.3.0,com.squareup.retrofit2:retrofit:2.3.0,,Apache-2.0,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
,1.0.0,group:noname:1.0.0,Fake dependency description,,Some license,http://website.tld/
"""
const val LICENSE_DEMO_AMAZON_RELEASE_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Bar,1.0.0,group:bar:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Retrofit,2.3.0,com.squareup.retrofit2:retrofit:2.3.0,,Apache-2.0,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
,1.0.0,group:noname:1.0.0,Fake dependency description,,Some license,http://website.tld/
"""
const val LICENSE_FULL_GOOGLE_RELEASE_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Foo,1.0.0,group:foo:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,BSD-2-Clause,BSD-2-Clause,https://opensource.org/licenses/BSD-2-Clause
Retrofit,2.3.0,com.squareup.retrofit2:retrofit:2.3.0,,Apache-2.0,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
"""
const val LICENSE_FULL_AMAZON_RELEASE_CSV =
    """Name,Version,MavenCoordinates,Description,SPDX-License-Identifier,License Name,License Url
Bar,1.0.0,group:bar:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Fake dependency name,1.0.0,group:name:1.0.0,Fake dependency description,,Some license,http://website.tld/
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,Apache-2.0,Apache-2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
Multi License,1.0.0,group:multilicenses:1.0.0,Fake dependency description,BSD-2-Clause,BSD-2-Clause,https://opensource.org/licenses/BSD-2-Clause
Retrofit,2.3.0,com.squareup.retrofit2:retrofit:2.3.0,,Apache-2.0,Apache 2.0,http://www.apache.org/licenses/LICENSE-2.0.txt
"""
