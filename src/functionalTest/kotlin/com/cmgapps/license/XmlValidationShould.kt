/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.plus
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import javax.xml.XMLConstants
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory

class XmlValidationShould {

    @TempDir
    lateinit var testProjectDir: Path

    @Test
    fun validateSchema() {
        val reportFolder = "$testProjectDir/build/reports/licenses/licenseReport"
        val mavenRepoUrl = javaClass.getResource("/maven")!!.toString()
        Files.createFile(Paths.get(testProjectDir.toString(), "build.gradle")).toFile() + """
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
                  html.enabled = false
                  xml.enabled = true
              }
            }
            
            dependencies {
              implementation 'group:multilicenses:1.0.0'
              implementation 'group:name:1.0.0'
            }
        """.trimIndent()

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir.toFile())
            .withArguments(":licenseReport")
            .withPluginClasspath()
            .build()

        assertThat(
            result.output,
            Matchers.matchesPattern(
                Pattern.compile(
                    ".*Wrote XML report to .*$reportFolder/licenses.xml.*",
                    Pattern.DOTALL
                )
            )
        )

        val schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
            .newSchema(URL("https://www.cmgapps.com/xsd/licenses.xsd"))

        val validator = schema.newValidator()
        assertDoesNotThrow {
            validator.validate(StreamSource(File("$reportFolder/licenses.xml")))
        }
    }
}
