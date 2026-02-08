/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaRegistry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.io.path.Path

class JsonSchemaValidationShould {
    @Test
    fun validateSchema() {
        val fixtureDir = File(fixturesDir, "json-schema-validator")
        createBuildRunner(fixtureDir).build()

        val schema =
            SchemaRegistry
                // setting null uses the "$schema" from JSON and throws if absent
                .withDefaultDialectId(null, null)
                .getSchema(
                    Path(
                        fixturesDir.parentFile.parent,
                        "main",
                        "schema",
                        "licenses-schema.json",
                    ).toFile().inputStream(),
                )

        val result =
            schema.validate(
                File(fixtureDir, "build/reports/licenses/licenseReport/licenses.json").readText(),
                InputFormat.JSON,
            ) { executionContext ->
                executionContext.executionConfig { executionConfigBuilder ->
                    executionConfigBuilder.formatAssertionsEnabled(true)
                }
            }

        assertThat(result, empty())
    }
}
