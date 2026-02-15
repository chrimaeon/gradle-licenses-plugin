/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("org.jetbrains.kotlin.multiplatform") version embeddedKotlinVersion
    id("com.cmgapps.licenses")
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("com.google.firebase:firebase-core:10.0.1")
            }
        }

        jvmMain {
            dependencies {
                implementation("com.squareup.retrofit2:retrofit:2.3.0")
            }
        }

        jsMain {
            dependencies {
                implementation("group:name:1.0.0")
            }
        }
    }
}

licenses {
    reports {
        html.enabled.set(false)
        plainText.enabled.set(true)
    }
}
