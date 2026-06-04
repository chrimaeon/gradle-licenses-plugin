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
            outputFile = project.layout.buildDirectory.file("csv-report/customdir.csv")
        }

        custom {
            enabled = true
            outputFile = project.layout.buildDirectory.file("reports/licenses.txt")
            generator.set { libraries ->
                libraries.map { (coordinates, library) -> "$coordinates -> $library" }.joinToString("\n")
            }
        }
    }
}

dependencies {
    implementation("org.apache.maven:maven-model:3.6.3")
    implementation("io.github.java-diff-utils:java-diff-utils:4.12")
    implementation("ch.qos.logback:logback-classic:1.4.5")
}
