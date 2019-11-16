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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    id("com.github.ben-manes.versions") version "0.25.0"
    kotlin("jvm") version Deps.kotlinVersion
    kotlin("kapt") version Deps.kotlinVersion
    id("com.jfrog.bintray") version "1.8.4"
    id("com.gradle.plugin-publish") version "0.10.1"
    id("com.cmgapps.licenses") version "1.3.0"
}

repositories {
    jcenter()
    google()
}

sourceSets {
    create("functionalTest") {
        java {
            srcDir("src/functionalTest/kotlin")
        }
        resources {
            srcDir("src/functionalTest/resources")
        }

        compileClasspath += sourceSets.main.get().output + configurations.testRuntime.get()
        runtimeClasspath += output + compileClasspath
    }
}

val ktlint by configurations.creating

configurations {
    named("functionalTestImplementation") {
        extendsFrom(testImplementation.get())
    }

    named("functionalTestRuntime") {
        extendsFrom(testRuntime.get())
    }
}

idea {
    module {
        testSourceDirs = testSourceDirs + sourceSets["functionalTest"].allJava.srcDirs
        testResourceDirs = testResourceDirs + sourceSets["functionalTest"].resources.srcDirs
    }
}

val group: String by project
val versionName: String by project
val projectUrl: String by project
val pomArtifactId: String by project
val pomName: String by project
val pomDescription: String by project
val scmUrl: String by project

project.group = group
version = versionName

pluginBundle {
    website = projectUrl
    vcsUrl = scmUrl
    tags = listOf("license-management", "android", "java", "java-library", "licenses")
}

gradlePlugin {
    plugins {
        create("licensesPlugin") {
            id = "com.cmgapps.licenses"
            implementationClass = "com.cmgapps.license.LicensesPlugin"
            displayName = pomName
            description = pomDescription
        }
    }

    testSourceSets(sourceSets["functionalTest"])
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

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
                    val connectionUrl: String by project
                    connection.set(connectionUrl)
                    val developerConnectionUrl: String by project
                    developerConnection.set(developerConnectionUrl)
                    url.set(scmUrl)
                }
            }
        }
    }
}

bintray {
    val credentialProps = Properties()
    val propsFile = file("${project.rootDir}/credentials.properties")

    if (propsFile.exists()) {
        credentialProps.load(propsFile.inputStream())
        user = credentialProps.getProperty("user")
        key = credentialProps.getProperty("key")
    }

    setPublications("pluginMaven")

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "maven"
        name = "${project.group}:$pomArtifactId"
        userOrg = user
        setLicenses("Apache-2.0")
        vcsUrl = projectUrl
        val issuesTrackerUrl: String by project
        issueTrackerUrl = issuesTrackerUrl
        githubRepo = projectUrl
        version(closureOf<BintrayExtension.VersionConfig> {
            name = versionName
            vcsTag = versionName
            released = Date().toString()
        })
    })
}

tasks {
    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs
        classpath = sourceSets["functionalTest"].runtimeClasspath
    }

    val ktlint by registering(JavaExec::class) {
        group = "Verification"
        description = "Check Kotlin code style."
        main = "com.pinterest.ktlint.Main"
        classpath = ktlint
        args = listOf("src/**/*.kt", "--reporter=plain", "--reporter=checkstyle,output=${buildDir}/reports/ktlint.xml")

    }

    check {
        dependsOn(functionalTest)
        dependsOn(ktlint)
    }

    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to pomName,
                    "Implementation-Version" to versionName,
                    "Built-By" to System.getProperty("user.name"),
                    "Built-Date" to Date(),
                    "Built-JDK" to System.getProperty("java.version"),
                    "Built-Gradle" to gradle.gradleVersion,
                    "Built-Kotlin" to Deps.kotlinVersion
                )
            )
        }
    }

    named<DependencyUpdatesTask>("dependencyUpdates") {
        revision = "release"

        rejectVersionIf {
            listOf("alpha", "beta", "rc", "cr", "m", "preview")
                .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                .any { it.matches(candidate.version) }
        }
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
    }
}

dependencies {
    compileOnly(Deps.androidGradlePlugin)
    implementation(kotlin("stdlib-jdk8", Deps.kotlinVersion))
    implementation(Deps.mavenModel)
    implementation(Deps.moshi)
    kapt(Deps.moshiCodegen)

    ktlint(Deps.ktlint)

    testImplementation(Deps.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(Deps.androidGradlePlugin)
    testImplementation(Deps.hamcrest)

    "functionalTestImplementation"(Deps.androidGradlePlugin)
    "functionalTestImplementation"(gradleTestKit())
}
