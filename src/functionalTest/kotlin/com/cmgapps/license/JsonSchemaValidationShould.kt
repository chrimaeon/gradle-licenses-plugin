/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license

import com.cmgapps.license.util.createBuildRunner
import com.cmgapps.license.util.fixturesDir
import com.networknt.schema.Error
import com.networknt.schema.InputFormat
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.utils.JsonNodes
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.TypeSafeDiagnosingMatcher
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
                .builder()
                .nodeReader { nodeReader ->
                    nodeReader.locationAware()
                }.build()
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

        assertThat(result, hasNoJsonSchemaValidationErrors())
    }
}

private fun hasNoJsonSchemaValidationErrors(): Matcher<List<Error>> =
    object : TypeSafeDiagnosingMatcher<List<Error>>() {
        override fun matchesSafely(
            item: List<Error>,
            mismatchDescription: Description,
        ): Boolean {
            if (item.isEmpty()) {
                return true
            }

            item.forEachIndexed { index, error ->
                val instanceLocation =
                    JsonNodes.tokenStreamLocationOf(error.instanceNode)

                mismatchDescription
                    .appendText(error.message)
                    .appendText(" on line ")
                    .appendValue(instanceLocation.lineNr)
                    .appendText(" column ")
                    .appendValue(instanceLocation.columnNr)
                if (index != item.lastIndex) {
                    mismatchDescription.appendText(" and \n")
                }
            }

            return false
        }

        override fun describeTo(description: Description) {
            description.appendText("no JSON Schema validation errors")
        }
    }
