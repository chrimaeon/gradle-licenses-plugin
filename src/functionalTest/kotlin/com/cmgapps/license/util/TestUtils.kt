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
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.not
import org.hamcrest.TypeSafeDiagnosingMatcher
import org.hamcrest.io.FileMatchers.anExistingDirectory
import org.hamcrest.io.FileMatchers.anExistingFile
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import java.io.File
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.stream.Stream

operator fun File.plus(text: String) = appendText(text)

val fixturesDir = File("src/functionalTest/fixtures")

fun <S, T> List<S>.cartesianProduct(other: List<T>): Stream<Arguments> =
    this
        .flatMap { s1 ->
            other.map { s2 ->
                arguments(s1, s2)
            }
        }.stream()

fun createBuildRunner(
    fixtureDir: File,
    vararg tasks: String = arrayOf("clean", "licenseReport"),
): GradleRunner =
    GradleRunner
        .create()
        .withDebug(true)
        .withArguments(
            *tasks,
            "--info",
            "--stacktrace",
            "--continue",
        ).withProjectDir(fixtureDir)
        .forwardOutput()

fun assertExpectedFiles(
    fixtureDir: File,
    taskName: String = "",
) {
    val expectedDir = File(fixtureDir, "expected/$taskName")
    assertThat(expectedDir, anExistingDirectory())

    val expectedFiles = expectedDir.walk().filter { it.isFile }.toList()
    assertThat("$expectedDir is emtpy", expectedFiles, not(empty()))
    for (expectedFile in expectedFiles) {
        val actualFile = File(fixtureDir, expectedFile.relativeTo(expectedDir).toString())
        assertThat(actualFile, anExistingFile())
        assertThat(actualFile, hasSameContentAs(expectedFile))
    }
}

private fun hasSameContentAs(
    expected: File,
    charset: Charset = StandardCharsets.UTF_8,
    normalizeLineEndings: Boolean = true,
): Matcher<File> =
    object : TypeSafeDiagnosingMatcher<File>(File::class.java) {
        override fun describeTo(description: Description) {
            description
                .appendText("a file with same content as ")
                .appendValue(expected.path)
                .appendText(" (charset=")
                .appendValue(charset.name())
                .appendText(", normalizeLineEndings=")
                .appendValue(normalizeLineEndings)
                .appendText(")")
        }

        override fun matchesSafely(
            actual: File,
            mismatchDescription: Description,
        ): Boolean {
            if (!expected.exists()) {
                mismatchDescription.appendText("expected file does not exist: ").appendValue(expected.path)
                return false
            }
            if (!actual.exists()) {
                mismatchDescription.appendText("actual file does not exist: ").appendValue(actual.path)
                return false
            }
            if (!expected.isFile) {
                mismatchDescription.appendText("expected is not a file: ").appendValue(expected.path)
                return false
            }
            if (!actual.isFile) {
                mismatchDescription.appendText("actual is not a file: ").appendValue(actual.path)
                return false
            }

            val expectedText = readText(expected, charset, normalizeLineEndings)
            val actualText = readText(actual, charset, normalizeLineEndings)

            if (expectedText == actualText) return true

            val diffIndex = firstDiffIndex(expectedText, actualText)
            mismatchDescription
                .appendText("content differed")
                .appendText(", first difference at index ")
                .appendValue(diffIndex)
                .appendText(", expected length=")
                .appendValue(expectedText.length)
                .appendText(", actual length=")
                .appendValue(actualText.length)

            return false
        }

        private fun readText(
            file: File,
            charset: Charset,
            normalizeLineEndings: Boolean,
        ): String {
            val text = file.readText(charset)
            return if (normalizeLineEndings) text.replace("\r\n", "\n") else text
        }

        private fun firstDiffIndex(
            a: String,
            b: String,
        ): Int {
            val min = minOf(a.length, b.length)
            for (i in 0 until min) {
                if (a[i] != b[i]) return i
            }
            return min
        }
    }
