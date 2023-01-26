/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.gradle.logResults
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import com.github.benmanes.gradle.versions.updates.gradle.GradleReleaseChannel
import kotlinx.kover.api.CounterType
import kotlinx.kover.api.DefaultJacocoEngine
import kotlinx.kover.api.KoverTaskExtension
import kotlinx.kover.api.VerificationValueType.COVERED_PERCENTAGE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    signing
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin.jvm)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlin.serialization)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.versions)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.gradle.pluginPublish)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.jetbrains.dokka)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.jetbrains.changelog)
    @Suppress("DSL_SCOPE_VIOLATION")
    alias(libs.plugins.kotlinx.kover)
}

val functionalTestSourceSet: SourceSet = sourceSets.create("functionalTest") {
    val sourceSetName = name
    java {
        srcDir("src/$sourceSetName/kotlin")
    }
    resources {
        srcDirs(sourceSets.main.get().resources.srcDirs)
    }
}

val ktlint: Configuration by configurations.creating

idea {
    module {
        testSources.from(functionalTestSourceSet.allJava.srcDirs)
        testResources.from(functionalTestSourceSet.resources.srcDirs)
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val pomProperties = Properties().apply {
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
                name.set(pomName)
                description.set(pomDescription)
                url.set(projectUrl)
                issueManagement {
                    val issuesTrackerUrl: String by pomProperties
                    system.set("github")
                    url.set(issuesTrackerUrl)
                }
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
        }
    }

    repositories {
        maven {
            name = "sonatype"
            val releaseUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
            url = if (versionName.endsWith("SNAPSHOT")) snapshotUrl else releaseUrl

            val credentials = Properties().apply {
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

signing {
    sign(publishing.publications["pluginMaven"])
}

changelog {
    version.set(versionName)
    header.set(provider { version.get() })
}

kover {
    engine.set(DefaultJacocoEngine)
    verify {
        rule {
            name = "Minimal Line coverage"
            bound {
                minValue = 80
                counter = CounterType.LINE
                valueType = COVERED_PERCENTAGE
            }
        }
    }
}

tasks {
    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath

        extensions.configure(KoverTaskExtension::class) {
            isDisabled.set(true)
        }
    }

    register<JavaExec>("ktlintFormat") {
        group = "Verification"
        description = "Format Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args = listOf(
            "src/**/*.kt",
            "--format",
        )
    }

    val ktlint by registering(JavaExec::class) {
        group = "Verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args = listOf(
            "src/**/*.kt",
            "--reporter=plain",
            "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml",
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
                    "Built-By" to System.getProperty("user.name"),
                    "Built-Date" to Date(),
                    "Built-JDK" to System.getProperty("java.version"),
                    "Built-Gradle" to gradle.gradleVersion,
                    "Built-Kotlin" to libs.versions.kotlin,
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

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
            jvmTarget = "1.8"
        }
    }

    dokkaJavadoc {
        outputDirectory.set(buildDir.resolve("javadoc"))
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
    // Necessary to bump a transitive dependency.
    compileOnly(libs.kotlin.reflect)

    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.maven.model)
    implementation(libs.maven.artifact)
    implementation(libs.kotlin.serialization)
    implementation(libs.apache.commonsCsv)

    ktlint(libs.ktlint)

    testImplementation(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(libs.hamcrest)
    testImplementation(libs.kotlin.reflect)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.android.gradlePlugin)

    "functionalTestImplementation"(libs.jUnit) {
        exclude(group = "org.hamcrest")
    }
    "functionalTestImplementation"(libs.kotlin.multiplatformPlugin)
    "functionalTestImplementation"(libs.hamcrest)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(libs.kotlin.reflect)
    "functionalTestImplementation"(libs.xmlunit.core)
    "functionalTestImplementation"(libs.xmlunit.matchers)
}
