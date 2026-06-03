/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.util

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import com.cmgapps.license.model.LicenseId
import com.cmgapps.license.model.MavenCoordinates

internal val testLibraries =
    mapOf(
        MavenCoordinates("test.group", "test.artifact", "1.0") to
            Library(
                "Test lib 1",
                "proper description",
                setOf(
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
        MavenCoordinates("group.test2", "artifact", "2.3.4") to
            Library(
                "Test lib 2",
                "descriptions of lib 2",
                setOf(
                    License(
                        LicenseId.APACHE,
                        // have different name to check mapping by id
                        name = "The Apache Software License, Version 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt",
                    ),
                ),
            ),
    )
