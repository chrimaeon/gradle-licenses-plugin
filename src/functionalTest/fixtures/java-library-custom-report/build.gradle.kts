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
        html.enabled = false
        custom {
            enabled = true
            outputFile = buildDir.resolve("reports").resolve("custom.licenses")
            generator.set { list -> list.joinToString(separator = "\n") }
        }
    }
}

dependencies {
    implementation("group:name:1.0.0")
    implementation("group:bar:1.0.0")
}
