/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("java")
    id("com.cmgapps.licenses")
}

licenses {
    additionalProjects(":module2")
    reports {
        html.enabled.set(true)
    }
}

dependencies {
    implementation("group:name:1.0.0")
}
