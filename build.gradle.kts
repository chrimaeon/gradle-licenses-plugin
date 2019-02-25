/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
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

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.jfrog.bintray.gradle.BintrayExtension
import java.util.*

plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version "0.20.0"
    kotlin("jvm") version Deps.kotlinVersion
    id("com.jfrog.bintray") version "1.8.4"
}

repositories {
    jcenter()
    google()
}

sourceSets {
    create("functionalTest") {
        java {
            srcDir(file("src/functionalTest/kotlin"))
        }
        resources {
            srcDir(file("src/functionalTest/resources"))
        }

        compileClasspath += sourceSets.main.get().output + configurations.testRuntime
        runtimeClasspath += output + compileClasspath
    }
}

configurations {
    named("functionalTestImplementation") {
        extendsFrom(testImplementation.get())
    }

    named("functionalTestRuntime") {
        extendsFrom(testRuntime.get())
    }
}

val group: String by project
val versionName: String by project

project.group = group
version = versionName

gradlePlugin {
    plugins {
        create("licensesPlugin") {
            id = "com.cmgapps.licenses"
            implementationClass = "com.cmgapps.license.LicensesPlugin"
        }
    }

    testSourceSets(sourceSets["functionalTest"])
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"

    resolutionStrategy {
        componentSelection {
            all {
                listOf("alpha", "beta", "rc", "cr", "m", "preview")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                        .any { it.matches(candidate.version) }.let {
                            if (it) {
                                reject("Release candidate")
                            }
                        }

            }
        }
    }
}

dependencies {
    compileOnly(Deps.androidGradlePlugin)
    implementation(kotlin("stdlib-jdk8", Deps.kotlinVersion))
    implementation(Deps.mavenModel)
    implementation(Deps.moshi)

    testImplementation(Deps.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(Deps.hamcrest)
    "functionalTestImplementation"(gradleTestKit())
}

val pomArtifactId: String by project
val pomName: String by project
val pomDescription: String by project

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    classifier = "javadoc"
    from(tasks.javadoc)
}

val connectionUrl: String by project
val developerConnectionUrl: String by project
val projectUrl: String by project

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {

            artifact(sourcesJar.get())
            artifact(javadocJar.get())

            artifactId = pomArtifactId

            pom {
                name.set(pomName)
                description.set(pomDescription)
                developers {
                    developer {
                        id.set("cgrach")
                        name.set("Christian Grach")
                    }
                }
                scm {
                    connection.set(connectionUrl)
                    developerConnection.set(developerConnectionUrl)
                    url.set(projectUrl)
                }
            }
        }
    }
}

bintray {
    val credentialProps = Properties()
    credentialProps.load(file("${project.rootDir}/credentials.properties").inputStream())
    user = credentialProps.getProperty("user")
    key = credentialProps.getProperty("key")
    setPublications("pluginMaven")

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "${project.group}:$pomArtifactId"
        userOrg = user
        setLicenses("Apache-2.0")
        vcsUrl = projectUrl
        version(closureOf<BintrayExtension.VersionConfig> {
            name = versionName
            vcsTag = versionName
            released = Date().toString()
        })
    })
}

val functionalTest by tasks.registering(Test::class) {
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
}

tasks.check { dependsOn(functionalTest) }



