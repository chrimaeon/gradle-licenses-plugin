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

package com.cmgapps.license.util

import org.gradle.testkit.runner.GradleRunner
import java.io.File

fun Any.getFileContent(fileName: String) = javaClass.getResource("/licenses/$fileName")?.readText()
    ?: error("""resource file "/licenses/$fileName" not found! """)

operator fun File.plus(text: String) = appendText(text)
infix fun File.write(text: String) = writeText(text)

fun GradleRunner.withJaCoCo(): GradleRunner = also {
    javaClass.classLoader.getResourceAsStream("testkit/testkit-gradle.properties")?.use { input ->
        File(projectDir, "gradle.properties").outputStream().use { input.copyTo(it) }
    } ?: throw IllegalStateException("Resource not found: testkit/testkit-gradle.properties")
}
