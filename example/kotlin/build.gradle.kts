/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    }
}

tasks.register<Copy>("copyLicense") {
    from(tasks.named("licenseReport"))
    into(rootProject.projectDir)
}

dependencies {
    implementation("org.apache.maven:maven-model:3.6.3")
}
