import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/*
 * Copyright (c)  2018. Christian Grach <christian.grach@cmgapps.com>
 */

plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version "0.20.0"
    kotlin("jvm") version Deps.kotlinVersion
}

repositories {
    jcenter()
    google()
}

val group: String by project
val versionName: String by project

project.group = group
version = versionName

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
    "functionalTestImplementation"(Deps.jUnit) {
        exclude(group = "org.hamcrest")
    }
    "functionalTestImplementation"(Deps.hamcrest)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("stdlib-jdk8", Deps.kotlinVersion))
}

val DEVEO_USERNAME: String by project
val DEVEO_PASSWORD: String by project

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
                    connection.set("scm:git:git://bitbucket.org/chrimaeon/gradle-licenses-plugin.git")
                    developerConnection.set("scm:git:ssh://bitbucket.org/chrimaeon/gradle-licenses-plugin.git")
                    url.set("http://bitbucket.org/chrimaeon/gradle-licenses-plugin/")
                }
            }
        }
    }

    repositories {
        maven {
            name = "HelixTeamHub"
            url = uri("https://helixteamhub.cloud/cmgapps/projects/cmgapp-libs/repositories/maven/libraries")
            credentials {
                username = DEVEO_USERNAME
                password = DEVEO_PASSWORD
            }
        }
    }
}

val functionalTest by tasks.registering(Test::class) {
    group = "verification"
    testClassesDirs = sourceSets["functionalTest"].output.classesDirs
    classpath = sourceSets["functionalTest"].runtimeClasspath
}

tasks.check { dependsOn(functionalTest) }



