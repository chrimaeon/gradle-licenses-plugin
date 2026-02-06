/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    kotlin("jvm") version embeddedKotlinVersion
    `java-gradle-plugin`
}

group = "com.cmgapps.gradle.convention"

gradlePlugin {
    plugins {
        register("testLoggerConvention") {
            id = "testlogger"
            implementationClass = "com.cmgapps.gradle.TestLoggerPlugin"
        }
    }
}
