/*
 * Copyright (c) 2021. Christian Grach <christian.grach@cmgapps.com>
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
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.matchesPattern
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Properties
import java.util.regex.Pattern

private const val KOTLIN_MULTIPLATFORM_PLUGIN = "org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22"

class LicensePluginMultiplatformShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var buildFile: File
    private lateinit var reportFolder: String
    private lateinit var mavenRepoUrl: String
    private lateinit var gradleRunner: GradleRunner
    private lateinit var pluginClasspath: String

    @BeforeEach
    fun setUp() {
        val pluginClasspathResource =
            javaClass.classLoader.getResourceAsStream("plugin-under-test-metadata.properties")
                ?: throw IllegalStateException(
                    "Did not find plugin classpath resource, run `:pluginUnderTestMetadata` task.",
                )
        pluginClasspath =
            Properties().run {
                load(pluginClasspathResource)
                getProperty("implementation-classpath")
                    .split(':')
                    .joinToString(", ") {
                        "'$it'"
                    }
            }

        buildFile = Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile()
        reportFolder = "$testProjectDir/build/reports/licenses"
        mavenRepoUrl =
            javaClass.getResource("/maven")?.toURI()?.toString() ?: error("""resource folder "/maven" not found!""")

        buildFile +
            """
            buildscript {
              repositories {
                mavenCentral()
                google()
              }
              dependencies {
                classpath "$KOTLIN_MULTIPLATFORM_PLUGIN"
                classpath files($pluginClasspath)
              }
            }
            apply plugin: 'org.jetbrains.kotlin.multiplatform'
            apply plugin: 'com.cmgapps.licenses'
            
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
    }

    @Test
    fun `apply plugin`() {
        val taskName = "licenseMultiplatformReport"

        buildFile +
            """
            kotlin {
                jvm()
            }
            """.trimIndent()
        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
    }

    @Test
    fun `generate overall licenses as TXT`() {
        val taskName = "licenseMultiplatformReport"

        buildFile +
            """
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                }
                
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation 'com.google.firebase:firebase-core:10.0.1'
                        }
                    }
                    
                    jvmMain {
                        dependencies {
                            implementation 'com.squareup.retrofit2:retrofit:2.3.0'
                        }
                    }
                    
                    jsMain {
                        dependencies {
                            implementation 'group:name:1.0.0'
                        }
                    }
                }
            }
            licenses {
                reports {
                    html.enabled = false
                    text.enabled = true
                }
            }
            
            """.trimIndent()
        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(
            result.output,
            matchesPattern(
                ".*Wrote TEXT report to .*$reportFolder/$taskName/licenses.txt.*".toPattern(Pattern.DOTALL),
            ),
        )
        assertThat(
            File("$reportFolder/$taskName/licenses.txt").readText().trim(),
            `is`(
                """
                    |Licenses
                    |├─ Fake dependency name:1.0.0
                    |│  ├─ License: Some license
                    |│  └─ URL: http://website.tld/
                    |├─ Retrofit:2.3.0
                    |│  ├─ License: Apache 2.0
                    |│  ├─ SPDX-License-Identifier: Apache-2.0
                    |│  └─ URL: http://www.apache.org/licenses/LICENSE-2.0.txt
                    |└─ com.google.firebase:firebase-core:10.0.1
                    |   └─ License: Undefined
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate JVM licenses as TXT`() {
        val taskName = "licenseMultiplatformJvmReport"

        buildFile +
            """
            
            kotlin {
                jvm()
                js(IR) {
                    browser()
                }
                
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation 'com.google.firebase:firebase-core:10.0.1'
                        }
                    }
                    
                    jvmMain {
                        dependencies {
                            implementation 'com.squareup.retrofit2:retrofit:2.3.0'
                        }
                    }
                    
                    jsMain {
                        dependencies {
                            implementation 'group:name:1.0.0'
                        }
                    }
                }
            }
            licenses {
                reports {
                    html.enabled = false
                    text.enabled = true
                }
            }
            
            """.trimIndent()
        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(
            result.output,
            matchesPattern(
                ".*Wrote TEXT report to .*$reportFolder/$taskName/licenses.txt.*".toPattern(Pattern.DOTALL),
            ),
        )
        assertThat(
            File("$reportFolder/$taskName/licenses.txt").readText().trim(),
            `is`(
                """
                    |Licenses
                    |├─ Retrofit:2.3.0
                    |│  ├─ License: Apache 2.0
                    |│  ├─ SPDX-License-Identifier: Apache-2.0
                    |│  └─ URL: http://www.apache.org/licenses/LICENSE-2.0.txt
                    |└─ com.google.firebase:firebase-core:10.0.1
                    |   └─ License: Undefined
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate native ios licenses`() {
        val taskName = "licenseMultiplatformIosArm64Report"

        buildFile +
            """
            kotlin {
                iosArm64()
                
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation 'com.google.firebase:firebase-core:10.0.1'
                        }
                    }
                    
                    iosArm64Main {
                        dependencies {
                            implementation 'group:name:1.0.0'
                        }
                    }
                }
            }
            licenses {
                reports {
                    html.enabled = false
                    text.enabled = true
                }
            }
            """.trimIndent()
        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(
            result.output,
            matchesPattern(
                ".*Wrote TEXT report to .*$reportFolder/$taskName/licenses.txt.*".toPattern(Pattern.DOTALL),
            ),
        )
        assertThat(
            File("$reportFolder/$taskName/licenses.txt").readText().trim(),
            `is`(
                """
                    |Licenses
                    |├─ Fake dependency name:1.0.0
                    |│  ├─ License: Some license
                    |│  └─ URL: http://website.tld/
                    |└─ com.google.firebase:firebase-core:10.0.1
                    |   └─ License: Undefined
                """.trimMargin(),
            ),
        )
    }

    @Test
    fun `generate custom target name`() {
        val taskName = "licenseMultiplatformCustomReport"

        buildFile +
            """
            kotlin {
                jvm("custom")
                
                sourceSets {
                    commonMain {
                        dependencies {
                            implementation 'com.google.firebase:firebase-core:10.0.1'
                        }
                    }
                    
                    customMain {
                        dependencies {
                            implementation 'group:name:1.0.0'
                        }
                    }
                }
            }
            licenses {
                reports {
                    html.enabled = false
                    text.enabled = true
                }
            }
            """.trimIndent()
        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(
            result.output,
            matchesPattern(
                ".*Wrote TEXT report to .*$reportFolder/$taskName/licenses.txt.*".toPattern(Pattern.DOTALL),
            ),
        )
        assertThat(
            File("$reportFolder/$taskName/licenses.txt").readText().trim(),
            `is`(
                """
                    |Licenses
                    |├─ Fake dependency name:1.0.0
                    |│  ├─ License: Some license
                    |│  └─ URL: http://website.tld/
                    |└─ com.google.firebase:firebase-core:10.0.1
                    |   └─ License: Undefined
                """.trimMargin(),
            ),
        )
    }

    @ParameterizedTest(name = "${ParameterizedTest.DISPLAY_NAME_PLACEHOLDER} - KGP Version = {0}")
    @ValueSource(strings = ["1.6.0", "1.7.0", "1.8.0", "1.9.0", "2.0.0"])
    fun `handle kotlin gradle plugin versions`(kgpVersion: String) {
        buildFile.outputStream().bufferedWriter().use {
            @Language("groovy")
            val content =
                """
                        buildscript {
                  repositories {
                    mavenCentral()
                    google()
                  }
                  dependencies {
                    classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kgpVersion"
                    classpath files($pluginClasspath)
                  }
                }
                apply plugin: 'org.jetbrains.kotlin.multiplatform'
                apply plugin: 'com.cmgapps.licenses'
                
                repositories {
                    maven {
                        url '$mavenRepoUrl'
                    }
                }
                kotlin {
                    jvm()
                }
                """.trimIndent()
            it.write(content)
        }

        val taskName = "licenseMultiplatformReport"

        val result =
            gradleRunner
                .withArguments(":$taskName")
                .build()

        assertThat(result.task(":$taskName")?.outcome, `is`(TaskOutcome.SUCCESS))
    }
}
