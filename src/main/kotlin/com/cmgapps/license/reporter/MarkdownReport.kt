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

internal class MarkdownReport(libraries: List<Library>) : Report(libraries) {
    override fun generate(): String {
        return StringBuilder().apply {
            append("# Open source licenses\n")
            append("### Notice for packages:\n")
            libraries.forEach { library ->
                append(library.name)
                append(' ')
                append('_')
                append(library.version)
                append("_:")
                library.licenses.forEach { license ->
                    append("\n* ")
                    append(license.name)
                    append(" (")
                    append(license.url)
                    append(")")
                }
                append("\n\n")
            }
        }.toString()
    }
}
