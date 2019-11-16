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

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library

internal class TextReport(libraries: List<Library>) : Report(libraries) {
    override fun generate(): String {
        return StringBuilder().apply {
            append("Licenses\n")
            val libLength = libraries.size
            libraries.forEachIndexed { libIndex, library ->
                if (libIndex < libLength - 1) {
                    append("$ITEM_PREFIX ")
                } else {
                    append("$LAST_ITEM_PREFIX ")
                }
                append(library.name)
                append(':')
                append(library.version)
                val licensesLength = library.licenses.size
                library.licenses.forEachIndexed { index, license ->

                    if (libIndex < libLength - 1) {
                        append(LINE_PREFIX)
                    } else {
                        append(LAST_LINE_PREFIX)
                    }

                    append("$ITEM_PREFIX License: ")
                    append(license.name)

                    if (libIndex < libLength - 1) {
                        append(LINE_PREFIX)
                    } else {
                        append(LAST_LINE_PREFIX)
                    }

                    if (index < licensesLength - 1) {
                        append(ITEM_PREFIX)
                    } else {
                        append(LAST_ITEM_PREFIX)
                    }
                    append(" URL: ")
                    append(license.url)
                }
                if (libIndex < libLength - 1) {
                    append('\n')
                }
            }
        }.toString()
    }

    private companion object {
        private const val ITEM_PREFIX = "├─"
        private const val LAST_ITEM_PREFIX = "└─"
        private const val LINE_PREFIX = "\n│  "
        private const val LAST_LINE_PREFIX = "\n   "
    }
}
