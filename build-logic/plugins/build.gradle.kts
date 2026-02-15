/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    id("java-gradle-plugin")
}

group = "com.cmgapps.gradle.plugins"

gradlePlugin {
    plugins {
        register("ktlintPlugin") {
            id = "ktlint"
            implementationClass = "com.cmgapps.gradle.ktlint.KtlintPlugin"
        }
    }
}
