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
    reports {
        html.enabled.set(true)
        html.stylesheet("body{}")
        html.useDarkMode.set(false)
    }
}

dependencies {
    implementation("group:name:1.0.0")
}
