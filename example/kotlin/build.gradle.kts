/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    java
    id("com.cmgapps.licenses") version "1.0.0"
}

repositories {
    mavenCentral()
}

licenses {
    reports {
        csv {
            enabled = true
            destination = buildDir.resolve("csv-report").resolve("customdir.csv")
        }
        json.enabled = true

        custom {
            enabled = true
            destination = buildDir.resolve("reports").resolve("licenses.txt")
            generate { list -> list.map { it.name }.joinToString() }
        }
        xml.enabled = true
    }
}

tasks.register<Copy>("copyLicense") {
    from(tasks.named("licenseReport"))
    into(rootProject.projectDir)
}

dependencies {
    implementation("org.apache.maven:maven-model:3.6.3")
}
