/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.application") version "8.13.2"
    id("com.cmgapps.licenses")
}

android {
    namespace = "com.cmgapps.licenses.example"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.cmgapps.licenses.example"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
}
