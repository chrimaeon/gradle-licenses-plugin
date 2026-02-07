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

import com.cmgapps.license.util.assertExpectedFiles
import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.cmgapps.license.util.hasSameContentAs
import com.cmgapps.license.util.plus
import org.gradle.testkit.runner.GradleRunner
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.not
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class LicensePluginJavaMultiProjectShould {
    @TempDir
    lateinit var testProjectDir: Path

    private lateinit var module1File: File
    private lateinit var module2File: File
    private lateinit var module3File: File
    private lateinit var mavenRepoUrl: String
    private lateinit var gradleRunner: GradleRunner

    @BeforeEach
    fun setUp() {
        Files
            .createFile(Paths.get(testProjectDir.toString(), "settings.gradle"))
            .toFile() + "include ':module1', ':module2', ':modules:submodule'"
        module1File =
            Paths.get(testProjectDir.toString(), "module1").toFile().run {
                mkdirs()
                Files.createFile(Paths.get(this.absolutePath, "build.gradle")).toFile()
            }

        module2File =
            Paths.get(testProjectDir.toString(), "module2").toFile().run {
                mkdirs()
                Files.createFile(Paths.get(this.absolutePath, "build.gradle")).toFile()
            }

        module3File =
            Paths.get(testProjectDir.toString(), "modules").resolve("submodule").toFile().run {
                mkdirs()
                Files.createFile(Paths.get(this.absolutePath, "build.gradle")).toFile()
            }

        mavenRepoUrl =
            javaClass.getResource("/maven")?.toURI()?.toString() ?: error("""resource folder "/maven" not found!""")
        gradleRunner =
            GradleRunner
                .create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(":module1:licenseReport")
                .withPluginClasspath()
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} with {0}")
    @ValueSource(
        strings = [
            "multi-project-collect-additional",
            "multi-project-merge-additional",
            "multi-project-only-single-instance",
        ],
    )
    fun `collect dependencies from additional module`(fixture: String) {
        val fixtureDir = File(fixturesDir, fixture)
        createBuildRunner(fixtureDir, "clean", ":module1:licenseReport").build()

        assertExpectedFiles(fixtureDir)
    }
}
