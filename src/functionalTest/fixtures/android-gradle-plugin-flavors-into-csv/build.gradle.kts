/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.application") version "9.2.0"
    id("com.cmgapps.licenses")
}

android {
    namespace = "com.cmgapps"
    compileSdk = 28
    defaultConfig {
        applicationId = "com.example"
    }

    flavorDimensions += listOf("version", "store")
    productFlavors {
        register("demo") {
            dimension = "version"
        }
        register("full") {
            dimension = "version"
        }
        register("google") {
            dimension = "store"
        }
        register("amazon") {
            dimension = "store"
        }
    }
}

licenses {
    reports {
        html.enabled.set(false)
        csv.enabled.set(true)
    }
}
dependencies {
    implementation("group:name:1.0.0")
    "demoImplementation"("group:noname:1.0.0")
    "fullImplementation"("group:multilicenses:1.0.0")
    "googleImplementation"("group:foo:1.0.0")
    "amazonImplementation"("group:bar:1.0.0")
    "releaseImplementation"("com.squareup.retrofit2:retrofit:2.3.0")
    "debugImplementation"("group:zet:1.0.0")
}
