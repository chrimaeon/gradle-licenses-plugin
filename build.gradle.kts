/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.GroupingEntityType
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.kotlinx.kover)
    id("testlogger")
    id("ktlint")
    alias(libs.plugins.buildconfig)
}

private val jvmTargetVersion = JvmTarget.JVM_17

kotlin {
    jvmToolchain(jvmTargetVersion.target.toInt())
    abiValidation {
        @OptIn(ExperimentalAbiValidation::class)
        enabled = true
    }
}

val pomProperties =
    Properties().apply {
        rootDir.resolve("pom.properties").inputStream().use(::load)
    }

val group: String by pomProperties
val versionName: String by pomProperties
val pomName: String by pomProperties
val projectUrl: String by pomProperties

project.group = group
project.version = versionName

val minimumGradleVersion = "9.0"
configurations.apiElements {
    attributes {
        attribute(
            GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
            objects.named(GradlePluginApiVersion::class.java, minimumGradleVersion),
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTargetVersion.target
    targetCompatibility = jvmTargetVersion.target
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2
        jvmTarget = jvmTargetVersion
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

testing {
    suites {
        val test: JvmTestSuite by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val functionalTestSuite =
            register<JvmTestSuite>("functionalTest") {
                dependencies {
                    implementation(project())
                    implementation(libs.jUnit) {
                        exclude(group = "org.hamcrest")
                    }
                    implementation(libs.hamcrest)
                    implementation(gradleTestKit())
                    implementation(libs.xmlunit.core)
                    implementation(libs.xmlunit.matchers)
                    implementation(libs.networknt.jsonschemavalidator)
                    implementation(libs.java.diff.utils)
                }

                targets.configureEach {
                    testTask.configure {
                        jvmArgs("-Xmx2g", "-Xms512m")
                        shouldRunAfter(test)
                    }
                }
            }

        tasks.check {
            dependsOn(functionalTestSuite)
        }
    }
}

gradlePlugin {
    val scmUrl: String by pomProperties
    val pomDescription: String by pomProperties

    website = projectUrl
    vcsUrl = scmUrl

    plugins {
        create("licensesPlugin") {
            id = "com.cmgapps.licenses"
            implementationClass = "com.cmgapps.license.LicensesPlugin"
            displayName = pomName
            description = pomDescription
            tags = listOf("license-management", "android", "java", "java-library", "licenses")
        }
    }

    testSourceSet(sourceSets["functionalTest"])
}

changelog {
    version = versionName
    header = versionName
    versionPrefix = ""
    repositoryUrl = projectUrl
}

kover {
    useJacoco = true
    jacocoVersion = libs.versions.jacoco

    currentProject {
        sources {
            excludedSourceSets.addAll(sourceSets["functionalTest"].name)
        }
    }

    reports {
        total {
            log {
                onCheck = true
                header = "Total Test Line Coverage"
                groupBy = GroupingEntityType.APPLICATION
                aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                coverageUnits = CoverageUnit.LINE
                format = "<value>% total line coverage"
            }
        }

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

buildConfig {
    sourceSets.named("functionalTest") {
        useKotlinOutput {
            packageName = "com.cmgapps.license"
            topLevelConstants = true
        }
        buildConfigField("MINIMUM_GRADLE_VERSION", minimumGradleVersion)
    }
}

tasks {
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

    withType<Test>().configureEach {
        useJUnitPlatform()
        jvmArgs("-Xmx2g", "-Xms512m")
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = libs.versions.gradle.get()
    }

    val updateReadme by registering {
        description = "Updates the version in the README.md"
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

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.multiplatform.plugin)

    implementation(libs.maven.model)
    implementation(libs.maven.model.builder)
    implementation(libs.maven.artifact)
    implementation(libs.kotlin.serialization)
    implementation(libs.apache.commons.csv)
    implementation(libs.apache.commons.text)

    testImplementation(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.kotlin)
}
