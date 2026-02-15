/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import kotlin.io.path.div

rootProject.name = "build-logic"

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files(rootDir.parentFile.toPath() / "gradle" / "libs.versions.toml"))
        }
    }
}

include(
    ":plugins",
    ":convention",
)
