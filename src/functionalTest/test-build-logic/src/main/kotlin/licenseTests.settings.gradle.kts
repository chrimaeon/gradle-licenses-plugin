/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import kotlin.io.path.Path

pluginManagement {
    includeBuild(rootDir.parentFile.parentFile.parentFile.parentFile)
    repositories {
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        maven {
            url = Path(rootDir.parentFile.parent, "maven-repo").toUri()
        }
    }
}
