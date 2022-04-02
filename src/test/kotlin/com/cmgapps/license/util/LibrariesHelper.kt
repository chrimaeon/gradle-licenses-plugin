/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.helper

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates
import org.apache.maven.artifact.versioning.ComparableVersion

internal val testLibraries = listOf(
    Library(
        MavenCoordinates("test.group", "test.artifact", ComparableVersion("1.0")),
        "Test lib 1",
        "proper description",
        listOf(
            License(
                LicenseId.APACHE,
                name = "Apache 2.0",
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt",
            ),
            License(
                LicenseId.MIT,
                name = "MIT License",
                url = "https://opensource.org/licenses/MIT",
            ),
        ),
    ),
    Library(
        MavenCoordinates("group.test2", "artifact", ComparableVersion("2.3.4")),
        "Test lib 2",
        "descriptions of lib 2",
        listOf(
            License(
                LicenseId.APACHE,
                // have different name to check mapping by id
                name = "The Apache Software License, Version 2.0",
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt",
            ),
        ),
    )
)
