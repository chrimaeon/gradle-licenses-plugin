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

package com.cmgapps.license.helper

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import org.apache.maven.artifact.versioning.ComparableVersion

object LibrariesHelper {

    val libraries = listOf(
        Library(
            "Test lib 1",
            ComparableVersion("1.0"),
            "proper description",
            listOf(
                License(
                    "Apache 2.0",
                    "http://www.apache.org/licenses/LICENSE-2.0.txt"
                ),
                License(
                    "MIT License",
                    "http://opensource.org/licenses/MIT"
                )
            )
        )
    )
}
