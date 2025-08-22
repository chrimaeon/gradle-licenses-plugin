/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.util.Date
import java.util.Properties

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.versions)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.kotlinx.kover)
    id("com.cmgapps.gradle.test-logger")
    id("com.cmgapps.gradle.ktlint")
}

kotlin {
    jvmToolchain(17)
}

val pomProperties =
    Properties().apply {
        rootDir.resolve("pom.properties").inputStream().use {
            load(it)
        }
    }

val group: String by pomProperties
val versionName: String by pomProperties
val pomName: String by pomProperties
val projectUrl: String by pomProperties

project.group = group
version = versionName

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("functionalTest") {
            dependencies {
                implementation(project())
                implementation(libs.jUnit) {
                    exclude(group = "org.hamcrest")
                }
                implementation(libs.kotlin.multiplatformPlugin)
                implementation(libs.hamcrest)
                implementation(gradleTestKit())
                implementation(libs.xmlunit.core)
                implementation(libs.xmlunit.matchers)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

@Suppress("UnstableApiUsage")
gradlePlugin {
    val scmUrl: String by pomProperties
    val pomDescription: String by pomProperties

    website.set(projectUrl)
    vcsUrl.set(scmUrl)

    plugins {
        create("licensesPlugin") {
            id = "com.cmgapps.licenses"
            implementationClass = "com.cmgapps.license.LicensesPlugin"
            displayName = pomName
            description = pomDescription
            tags.set(listOf("license-management", "android", "java", "java-library", "licenses"))
        }
    }

    testSourceSet(sourceSets["functionalTest"])
}

changelog {
    version = versionName
    header = provider { version.get() }
    versionPrefix = provider { "" }
    repositoryUrl = provider { projectUrl }
}

kover {
    useJacoco("0.8.13")
    currentProject {
        sources {
            excludedSourceSets.addAll(sourceSets["functionalTest"].name)
        }
    }

    reports {
        verify {
            rule("Minimal Line coverage") {
                bound {
                    minValue = 80
                    coverageUnits = CoverageUnit.LINE
                    aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

tasks {
    check {
        dependsOn(testing.suites.named("functionalTest"))
    }

    jar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to pomName,
                    "Implementation-Version" to versionName,
                    "Implementation-Vendor" to "CMG Mobile Apps",
                    "Created-By" to """${System.getProperty("java.version")} (${System.getProperty("java.vendor")})""",
                    "Build-By" to System.getProperty("user.name"),
                    "Build-Date" to Date(),
                    "Build-JDK" to System.getProperty("java.version"),
                    "Build-Gradle" to gradle.gradleVersion,
                    "Build-Kotlin" to libs.versions.kotlin,
                ),
            )
        }
    }

    named<DependencyUpdatesTask>("dependencyUpdates") {
        revision = "release"

        gradleReleaseChannel = GradleReleaseChannel.CURRENT.id

        rejectVersionIf {
            listOf("alpha", "beta", "rc", "cr", "m", "preview")
                .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                .any { it.matches(candidate.version) }
        }
    }

    withType<Test> {
        useJUnitPlatform()
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = libs.versions.gradle.get()
    }

    val updateReadme by registering {
        val readmeFile = rootDir.resolve("README.md")
        val version: String by project

        inputs.property("libVersion", version)
        outputs.file(readmeFile)

        doLast {
            val content = readmeFile.readText()
            val oldVersion =
                """id\("com.cmgapps.licenses"\) version "(.*)"""".toRegex(RegexOption.MULTILINE).find(content)?.let {
                    it.groupValues[1]
                } ?: error("Cannot find oldVersion")

            logger.info("Updating README.md version $oldVersion to $version")

            val newContent = content.replace(oldVersion, version)
            readmeFile.writeText(newContent)
        }
    }

    patchChangelog {
        dependsOn(updateReadme)
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.multiplatformPlugin)

    implementation(libs.maven.model)
    implementation(libs.maven.artifact)
    implementation(libs.kotlin.serialization)
    implementation(libs.apache.commonsCsv)

    testImplementation(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.android.gradlePlugin)
}
