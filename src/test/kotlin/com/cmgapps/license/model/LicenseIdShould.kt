/*
 * Copyright (c) 2022. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.model

import com.cmgapps.license.helper.filename
import com.cmgapps.license.helper.text
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.hamcrest.Matchers.nullValue
import org.hamcrest.Matchers.oneOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedInvocationConstants
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.util.stream.Stream

class LicenseIdShould {
    @ParameterizedTest
    @EnumSource(
        LicenseId::class,
        names = ["UNKNOWN"],
        mode = EnumSource.Mode.EXCLUDE,
    )
    fun `map known ids to spdx license identifier`(id: LicenseId) {
        assertThat(
            id.spdxLicenseIdentifier,
            `is`(
                oneOf(
                    "Apache-2.0",
                    "BSD-2-Clause",
                    "BSD-3-Clause",
                    "CDDL-1.0",
                    "EPL-2.0",
                    "GPL-2.0-only",
                    "GPL-3.0-only",
                    "LGPL-2.1-only",
                    "LGPL-3.0-only",
                    "MIT",
                    "MPL-2.0",
                    "EPL-1.0",
                    "ISC",
                ),
            ),
        )
    }

    @Test
    fun `map unknown id to null spdx license identifier`() {
        assertThat(LicenseId.UNKNOWN.spdxLicenseIdentifier, nullValue())
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Apache-2.0",
            "BSD-2-Clause",
            "BSD-3-Clause",
            "CDDL-1.0",
            "EPL-2.0",
            "GPL-2.0-only",
            "GPL-3.0-only",
            "LGPL-2.1-only",
            "LGPL-3.0-only",
            "MIT",
            "MPL-2.0",
            "EPL-1.0",
            "ISC",
        ],
    )
    fun `map spdx license identifiers to known ids`(value: String) {
        assertThat(LicenseId.fromSpdxLicenseIdentifier(value), not(nullValue()))
    }

    @Test
    fun `map null spdx license identifier to UNKNOWN`() {
        assertThat(LicenseId.fromSpdxLicenseIdentifier(null), `is`(LicenseId.UNKNOWN))
    }

    @ParameterizedTest
    @EnumSource(
        LicenseId::class,
        names = ["UNKNOWN"],
        mode = EnumSource.Mode.EXCLUDE,
    )
    fun `map known ids to resource file`(id: LicenseId) {
        assertThat(
            id.filename,
            `is`(
                oneOf(
                    "apache-2.0.txt",
                    "bsd-2-clause.txt",
                    "bsd-3-clause.txt",
                    "cddl.txt",
                    "epl-2.0.txt",
                    "gpl-2.0.txt",
                    "gpl-3.0.txt",
                    "lgpl-2.1.txt",
                    "lgpl-3.0.txt",
                    "mit.txt",
                    "mpl-2.0.txt",
                    "epl-1.0.txt",
                    "isc.txt",
                ),
            ),
        )
    }

    @Test
    fun `UNKNOWN should throw exception when requesting resource file name`() {
        assertThrows<IllegalArgumentException> { LicenseId.UNKNOWN.filename }
    }

    @ParameterizedTest
    @EnumSource(
        LicenseId::class,
        names = ["UNKNOWN"],
        mode = EnumSource.Mode.EXCLUDE,
    )
    fun `get license text for known ids`(id: LicenseId) {
        assertThat(id.text, not(emptyString()))
    }

    @Test
    fun `get empty text for UNKNOW license id`() {
        assertThat(LicenseId.UNKNOWN.text, `is`(emptyString()))
    }

    @ParameterizedTest(name = "${ParameterizedInvocationConstants.DISPLAY_NAME_PLACEHOLDER} - LicenseId = {0}")
    @MethodSource("provideLicenseIdMapping")
    fun `map licenses to name and url`(
        id: LicenseId,
        size: Int,
    ) {
        assertThat(LicenseId.map.filterValues { it == id }.size, `is`(size))
    }

    companion object {
        @JvmStatic
        private fun provideLicenseIdMapping(): Stream<Arguments> =
            Stream.of(
                Arguments.of(LicenseId.APACHE, 12),
                Arguments.of(LicenseId.BSD_2, 5),
                Arguments.of(LicenseId.BSD_3, 4),
                Arguments.of(LicenseId.EPL_2, 6),
                Arguments.of(LicenseId.GPL_2, 7),
                Arguments.of(LicenseId.GPL_3, 7),
                Arguments.of(LicenseId.LGPL_2_1, 9),
                Arguments.of(LicenseId.LGPL_3, 7),
                Arguments.of(LicenseId.MIT, 4),
                Arguments.of(LicenseId.MPL_2, 6),
                Arguments.of(LicenseId.CDDL, 4),
                Arguments.of(LicenseId.EPL_1, 9),
                Arguments.of(LicenseId.ISC, 3),
            )
    }
}
