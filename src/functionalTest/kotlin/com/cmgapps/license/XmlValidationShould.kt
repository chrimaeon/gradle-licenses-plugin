/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.xmlunit.builder.Input
import org.xmlunit.matchers.ValidationMatcher.valid
import java.io.File
import kotlin.io.path.Path

class XmlValidationShould {
    @Test
    fun validateSchema() {
        val fixtureDir = File(fixturesDir, "xml-validator")
        createBuildRunner(fixtureDir).build()

        assertThat(
            Input.fromFile(
                File(fixtureDir, "build/reports/licenses/licenseReport/licenses.xml"),
            ),
            valid(
                Input.fromURI(
                    Path(
                        fixturesDir.parentFile.parent,
                        "main",
                        "xsd",
                        "licenses.xsd",
                    ).toUri(),
                ),
            ),
        )
    }
}
