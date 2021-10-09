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

package com.cmgapps.gradle

import org.gradle.api.logging.Logger
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType

const val CSI = "\u001B["
const val ANSI_RED = "31"
const val ANSI_GREEN = "32"
const val ANSI_YELLOW = "33"
const val ANSI_BOLD = "1"

fun Logger.logResults(desc: TestDescriptor, result: TestResult) {
    val message = "{} > {} {}" + if (result.exception != null) "\n>\t{}\n" else "\n"

    @OptIn(ExperimentalStdlibApi::class)
    val params = buildList<String> {
        add(desc.className?.substringAfterLast('.') ?: "")
        add(desc.displayName)
        add(getFormattedResult(result))
        result.exception?.let {
            add(it.message?.replace("\n", "\n>\t") ?: "")
        }
    }.toTypedArray()

    if (result.resultType == ResultType.FAILURE) {
        this.error(message, *params)
    } else {
        this.lifecycle(message, *params)
    }
}

private fun getFormattedResult(result: TestResult): String {
    return buildString {
        val isAnsiColorTerm = System.getenv("TERM")?.toLowerCase()?.contains("color") ?: false
        val (color, text) = when (result.resultType) {
            ResultType.SUCCESS -> ANSI_GREEN to "PASSED"
            ResultType.FAILURE -> ANSI_RED to "FAILED"
            ResultType.SKIPPED -> ANSI_YELLOW to "SKIPPED"
            null -> ANSI_YELLOW to "NO RESULT"
        }
        if (isAnsiColorTerm) {
            append(CSI)
            append(color)
            append(";${ANSI_BOLD}m")
        }
        append(text)

        if (isAnsiColorTerm) {
            append(CSI)
            append("0m")
        }
    }
}
