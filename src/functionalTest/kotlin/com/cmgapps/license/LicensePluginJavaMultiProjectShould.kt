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
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class LicensePluginJavaMultiProjectShould {
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
