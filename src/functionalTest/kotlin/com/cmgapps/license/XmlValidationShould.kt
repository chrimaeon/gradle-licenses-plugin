/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.plus
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.xmlunit.builder.Input
import org.xmlunit.matchers.ValidationMatcher.valid
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class XmlValidationShould {
    @TempDir
    lateinit var testProjectDir: Path

    @Test
    fun validateSchema() {
        val reportFolder = "$testProjectDir/build/reports/licenses/licenseReport"
        val mavenRepoUrl = javaClass.getResource("/maven")!!.toString()
        Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile() +
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
            
            licenses {
              reports {
                  html.enabled.set(false)
                  xml.enabled.set(true)
              }
            }
            
            dependencies {
              implementation 'group:multilicenses:1.0.0'
              implementation 'group:name:1.0.0'
            }
            """.trimIndent()

        GradleRunner
            .create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .build()

        assertThat(
            Input.fromFile("$reportFolder/licenses.xml"),
            valid(Input.fromURL(URL("https://www.cmgapps.com/xsd/licenses.xsd"))),
        )
    }
}
