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

object Deps {
    const val kotlinVersion = "1.4.32"
    const val jacocoAgentVersion = "0.8.7"

    object Plugins {
        const val versionsVersion = "0.38.0"
        const val pluginPublishVersion = "0.14.0"
        const val dokkaVersion = "1.4.32"
    }

    const val androidGradlePlugin = "com.android.tools.build:gradle:4.2.1"
    const val mavenModel = "org.apache.maven:maven-model:3.8.1"
    const val jUnit = "org.junit.jupiter:junit-jupiter:5.7.2"
    const val hamcrest = "org.hamcrest:hamcrest:2.2"
    const val ktlint = "com.pinterest:ktlint:0.41.0"
    const val kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1"
}
