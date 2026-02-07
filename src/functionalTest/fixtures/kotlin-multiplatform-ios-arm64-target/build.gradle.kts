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
    iosArm64()

    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation("com.google.firebase:firebase-core:10.0.1")
            }
        }

        iosArm64Main {
            dependencies {
                implementation("group:name:1.0.0")
            }
        }

        jvmMain {
            dependencies {
                implementation("group:foo:1.0.0")
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
