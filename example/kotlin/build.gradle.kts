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
            outputFile = buildDir.resolve("csv-report").resolve("customdir.csv")
        }
        html.enabled = true
        json.enabled = true
        markdown.enabled = true
        plainText.enabled = true

        custom {
            enabled = true
            outputFile = buildDir.resolve("reports").resolve("licenses.custom")
            generator.set { list -> list.joinToString(separator = "\n") }
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
    implementation("ch.qos.logback:logback-classic:1.4.5")
}
