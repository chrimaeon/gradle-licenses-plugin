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
            outputFile =
                project.layout.buildDirectory
                    .file("csv-report/customdir.csv")
                    .get()
                    .asFile
        }

        custom {
            enabled = true
            outputFile =
                project.layout.buildDirectory
                    .file("reports/licenses.txt")
                    .get()
                    .asFile
            generator.set { libraries ->
                libraries.map { (_, library) -> "${library.name}" }.joinToString("\n")
            }
        }
    }
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
}
