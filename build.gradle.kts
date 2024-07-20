/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.gradle.logResults
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel
import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-gradle-plugin`
    signing
    alias(libs.plugins.nexus.publish)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.versions)
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.jetbrains.dokka)
    alias(libs.plugins.jetbrains.changelog)
    alias(libs.plugins.kotlinx.kover)
}

val functionalTestSourceSet: SourceSet =
    sourceSets.create("functionalTest") {
        val sourceSetName = name
        java {
            srcDir("src/$sourceSetName/kotlin")
        }
        resources {
            srcDirs(
                sourceSets.main
                    .get()
                    .resources.srcDirs,
            )
        }
    }

val ktlint: Configuration by configurations.creating

idea {
    module {
        testSources.from(functionalTestSourceSet.allJava.srcDirs)
        testResources.from(functionalTestSourceSet.resources.srcDirs)
    }
}

val javaLanguageVersion = JavaLanguageVersion.of("17")

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(javaLanguageVersion)
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(javaLanguageVersion)
    }
}

val pomProperties =
    Properties().apply {
        rootDir.resolve("pom.properties").inputStream().use {
            load(it)
        }
    }

val group: String by pomProperties
val versionName: String by pomProperties
val projectUrl: String by pomProperties
val pomName: String by pomProperties
val pomDescription: String by pomProperties
val scmUrl: String by pomProperties

project.group = group
version = versionName

@Suppress("UnstableApiUsage")
gradlePlugin {
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

    testSourceSets(functionalTestSourceSet)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            val pomArtifactId: String by pomProperties

            artifactId = pomArtifactId

            pom {
                basePomInfo()
                name.set(pomName)
                description.set(pomDescription)
                issueManagement {
                    val issuesTrackerUrl: String by pomProperties
                    system.set("github")
                    url.set(issuesTrackerUrl)
                }
            }
        }

        afterEvaluate {
            named<MavenPublication>("licensesPluginPluginMarkerMaven") {
                pom {
                    basePomInfo()
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (versionName.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

            val credentials =
                Properties().apply {
                    val credFile = projectDir.resolve("credentials.properties")
                    if (credFile.exists()) {
                        credFile.inputStream().use {
                            load(it)
                        }
                    }
                }
            credentials {
                username = credentials.getProperty("username")
                password = credentials.getProperty("password")
            }
        }
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}

changelog {
    version.set(versionName)
    header.set(provider { version.get() })
}

kover {
    useJacoco()
    currentProject {
        sources {
            excludedSourceSets.addAll(functionalTestSourceSet.name)
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

fun MavenPom.basePomInfo() {
    url.set(projectUrl)

    developers {
        developer {
            id.set("cgrach")
            name.set("Christian Grach")
        }
    }
    scm {
        val connectionUrl: String by pomProperties
        connection.set(connectionUrl)
        val developerConnectionUrl: String by pomProperties
        developerConnection.set(developerConnectionUrl)
        url.set(scmUrl)
    }
    licenses {
        license {
            name.set("Apache License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
        }
    }
}

tasks {
    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath
    }

    register<JavaExec>("ktlintFormat") {
        group = "Verification"
        description = "Format Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args =
            listOf(
                "src/**/*.kt",
                "--format",
            )
    }

    val checkstyleOutputFile = layout.buildDirectory.file("reports/ktlint.xml")

    val ktlint by registering(JavaExec::class) {
        group = "Verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args =
            listOf(
                "src/**/*.kt",
                "--reporter=plain",
                "--reporter=checkstyle,output=${checkstyleOutputFile.get()}",
            )
    }

    check {
        dependsOn(functionalTest, ktlint)
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
        afterTest(KotlinClosure2(logger::logResults))
    }

    dokkaJavadoc {
        outputDirectory.set(layout.buildDirectory.dir("javadoc"))
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

    ktlint(libs.ktlint.cli)

    testImplementation(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(libs.hamcrest)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.android.gradlePlugin)

    "functionalTestImplementation"(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    "functionalTestImplementation"(libs.kotlin.multiplatformPlugin)
    "functionalTestImplementation"(libs.hamcrest)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(libs.xmlunit.core)
    "functionalTestImplementation"(libs.xmlunit.matchers)
}
