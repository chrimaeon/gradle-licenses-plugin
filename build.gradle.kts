/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import com.cmgapps.gradle.logResults
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import kotlinx.kover.api.VerificationValueType.COVERED_LINES_PERCENTAGE
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version Deps.Plugins.versionsVersion
    kotlin("jvm") version Deps.kotlinVersion
    id("com.gradle.plugin-publish") version Deps.Plugins.pluginPublishVersion
    id("org.jetbrains.dokka") version Deps.Plugins.dokkaVersion
    kotlin("plugin.serialization") version Deps.kotlinVersion
    id("org.jetbrains.changelog") version Deps.Plugins.changelogPluginVersion
    id("org.jetbrains.kotlinx.kover") version "0.4.4"
}

repositories {
    mavenCentral()
    google()
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
        testSourceDirs = testSourceDirs + functionalTestSourceSet.allJava.srcDirs
        testResourceDirs = testResourceDirs + functionalTestSourceSet.resources.srcDirs
    }
}

java {
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

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.dokkaJavadoc)
}

publishing {
    publications {
        register<MavenPublication>("pluginMaven") {
            // component registered by gradle-plugin plugin
            artifact(sourcesJar.get())
            artifact(javadocJar.get())

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
                    load(credFile.inputStream())
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
}

kover {
    coverageEngine.set(kotlinx.kover.api.CoverageEngine.JACOCO)
}

tasks {
    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath
    }

    register<JavaExec>("ktlintFormat") {
        group = "Verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args = listOf("src/**/*.kt", "--format", "--reporter=plain", "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml")
    }

    val ktlint by registering(JavaExec::class) {
        group = "Verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = ktlint
        args = listOf("src/**/*.kt", "--reporter=plain", "--reporter=checkstyle,output=$buildDir/reports/ktlint.xml")
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
        afterTest(KotlinClosure2(logger::logResults))
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
            jvmTarget = "1.8"
        }
    }

    dokkaJavadoc {
        outputDirectory.set(buildDir.resolve("javadoc"))
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "7.3.3"
    }

    val updateReadme by registering {
        val readmeFile = rootDir.resolve("README.md")
        val version: String by project

        inputs.property("libVersion", version)
        outputs.file(readmeFile)

        doLast {
            val content = readmeFile.readText()
            val oldVersion = """id "com.cmgapps.licenses" version "(.*)"""".toRegex().find(content)?.let {
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

    koverVerify {
        rule {
            name = "Minimal Line coverage"
            bound {
                minValue = 80
                valueType = COVERED_LINES_PERCENTAGE
            }
        }
    }
}

dependencies {
    compileOnly(Deps.androidGradlePlugin)
    compileOnly(Deps.kotlinMultiplatformPlugin)

    val kotlinReflect = kotlin("reflect", Deps.kotlinVersion)
    // Necessary to bump a transitive dependency.
    compileOnly(kotlinReflect)

    implementation(kotlin("stdlib-jdk8", Deps.kotlinVersion))
    implementation(Deps.mavenModel)
    implementation(Deps.mavenArtifact)
    implementation(Deps.kotlinSerialization)
    implementation(Deps.apacheCommonsCsv)

    ktlint(Deps.ktlint)

    testImplementation(Deps.jUnit) {
        exclude(group = "org.hamcrest")
    }
    testImplementation(Deps.androidGradlePlugin)
    testImplementation(Deps.hamcrest)
    testImplementation(kotlinReflect)
    testImplementation(Deps.mockitoKotlin)

    "functionalTestImplementation"(Deps.jUnit) {
        exclude(group = "org.hamcrest")
    }
    "functionalTestImplementation"(Deps.androidGradlePlugin)
    "functionalTestImplementation"(Deps.kotlinMultiplatformPlugin)
    "functionalTestImplementation"(Deps.hamcrest)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlinReflect)
}
