/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.dynamic-feature") version "9.2.0"
    id("com.cmgapps.licenses")
}

android {
    namespace = "com.cmgapps.licenses.example"
    compileSdk = 33
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
}
