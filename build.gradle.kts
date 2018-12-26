import com.github.benmanes.gradle.versions.updates.DependencyUpdates
import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

plugins {
    groovy
    `java-gradle-plugin`
    `maven-publish`
    signing
    id("com.github.ben-manes.versions") version "0.20.0"
}

repositories {
    jcenter()
    google()
}

val GROUP: String by project
val VERSION_NAME: String by project

group = GROUP
version = VERSION_NAME

gradlePlugin {
    plugins {
        create("licensesPlugin") {
            id = "com.cmgapps.licenses"
            implementationClass = "com.cmgapps.license.LicensesPlugin"
        }
    }
}

tasks.getByName("dependencyUpdates", DependencyUpdatesTask::class) {
    revision = "release"
}

dependencies {
    compileOnly("com.android.tools.build:gradle:3.3.0-rc03")

    testCompile("junit:junit:4.12")
}

val DEVEO_USERNAME: String by project
val DEVEO_PASSWORD: String by project

val POM_ARTIFACT_ID: String by project
val POM_NAME: String by project
val POM_DESCRIPTION: String by project

task<Jar>("sourcesJar") {
    classifier = "sources"
    from(project.sourceSets.main.get().allSource)
}

task<Jar>("javadocJar") {
    classifier = "javadoc"
    from(tasks.javadoc)
}

publishing {
    publications {
        create<MavenPublication>("gradlePlugin") {
            from(components["java"])

            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            artifactId = POM_ARTIFACT_ID

            pom {
                name.set(POM_NAME)
                description.set(POM_DESCRIPTION)
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

