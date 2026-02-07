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
        html.enabled.set(false)
        plainText.enabled.set(true)
    }
}

dependencies {
    implementation("group:name:1.0.0")
}
