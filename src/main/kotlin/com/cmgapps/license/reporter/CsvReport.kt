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
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.StringWriter

internal class CsvReport(libraries: List<Library>) : Report(libraries) {

    override fun generate(): String = StringWriter().use { writer ->
        CSVPrinter(writer, CSVFormat.RFC4180.builder().setHeader(*HEADER).build()).use { printer ->
            libraries.forEach { library ->
                val license = library.licenses.firstOrNull()
                printer.printRecord(
                    library.name,
                    library.version,
                    library.description,
                    license?.name,
                    license?.url
                )
            }
            printer.flush()
        }
        writer.toString()
    }

    companion object {
        @JvmStatic
        private val HEADER = arrayOf("Name", "Version", "Description", "License Name", "License Url")
    }
}
