/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("com.android.application")
    id("com.cmgapps.licenses") version "1.0.0"
}

repositories {
    google()
    mavenCentral()
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

licenses {
    reports {
        html.enabled = true
        json.enabled = true
        xml.enabled = true
        csv {
            enabled = true
            destination = buildDir.resolve("csv-report").resolve("customdir.csv")
        }

        custom {
            enabled = true
            destination = buildDir.resolve("reports").resolve("licenses.txt")
            generate { list -> list.map { it.name }.joinToString() }
        }
    }
}

dependencies {
    implementation("org.apache.maven:maven-model:3.6.3")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}
