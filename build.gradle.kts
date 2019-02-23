import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

plugins {
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version "0.20.0"
    kotlin("jvm") version "1.3.21"
}

repositories {
    jcenter()
    google()
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
}

tasks.named<DependencyUpdatesTask>("dependencyUpdates") {
    revision = "release"

    resolutionStrategy {
        componentSelection {
            all {
                val rejected = listOf("alpha", "beta", "rc", "cr", "m", "preview")
                        .map { qualifier -> Regex("(?i).*[.-]$qualifier[.\\d-]*") }
                        .any { it.matches(candidate.version) }
                if (rejected) {
                    reject("Release candidate")
                }
            }
        }
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:3.3.1")
    implementation(kotlin("stdlib-jdk8", "1.3.21"))
    implementation("org.apache.maven:maven-model:3.6.0")


    testCompile("junit:junit:4.12")
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
        create<MavenPublication>("pluginMaven") {

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

