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
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date
import java.util.Properties

plugins {
    idea
    `java-gradle-plugin`
    `maven-publish`
    signing
    jacoco
    id("com.github.ben-manes.versions") version Deps.Plugins.versionsVersion
    kotlin("jvm") version Deps.kotlinVersion
    id("com.gradle.plugin-publish") version Deps.Plugins.pluginPublishVersion
    id("org.jetbrains.dokka") version Deps.Plugins.dokkaVersion
    kotlin("plugin.serialization") version Deps.kotlinVersion
    id("org.jetbrains.changelog") version Deps.Plugins.changelogPluginVersion
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

    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

val ktlint: Configuration by configurations.creating

configurations {
    named("functionalTestImplementation") {
        extendsFrom(testImplementation.get())
    }

    register("jacocoRuntime")
}

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
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                val credFile = file("./credentials.properties")
                if (credFile.exists()) {
                    load(credFile.inputStream())
                }
            }
            credentials {
                username = credentials.getProperty("sonaUsername")
                password = credentials.getProperty("sonaPassword")
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

tasks {
    val setupJacocoRuntime by registering(WriteProperties::class) {
        outputFile =
            functionalTestSourceSet.output.resourcesDir!!.resolve("testkit/testkit-gradle.properties")
        property(
            "org.gradle.jvmargs",
            "-javaagent:${configurations["jacocoRuntime"].asPath}=destfile=$buildDir/jacoco/functionalTest.exec"
        )
    }

    val functionalTest by registering(Test::class) {
        group = "verification"
        testClassesDirs = functionalTestSourceSet.output.classesDirs
        classpath = functionalTestSourceSet.runtimeClasspath
        dependsOn(setupJacocoRuntime)
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

    jacoco {
        toolVersion = Deps.jacocoAgentVersion
    }

    val jacocoExecData = fileTree("$buildDir/jacoco").include("*.exec")

    jacocoTestReport {
        executionData(jacocoExecData)
        dependsOn(test, functionalTest)
    }

    jacocoTestCoverageVerification {
        executionData(jacocoExecData)
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    minimum = "0.8".toBigDecimal()
                }
            }
        }
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
        gradleVersion = "7.2"
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
}

dependencies {
    compileOnly(Deps.androidGradlePlugin)

    val kotlinReflect = kotlin("reflect", Deps.kotlinVersion)
    // Necessary to bump a transitive dependency.
    compileOnly(kotlinReflect)

    implementation(kotlin("stdlib-jdk8", Deps.kotlinVersion))
    implementation(Deps.mavenModel)
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

    "functionalTestImplementation"(Deps.androidGradlePlugin)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlinReflect)

    "jacocoRuntime"("org.jacoco:org.jacoco.agent:${jacoco.toolVersion}:runtime")
}
