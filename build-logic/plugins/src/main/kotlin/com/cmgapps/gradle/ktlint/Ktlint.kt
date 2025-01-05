/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.named

@Suppress("UnstableApiUsage")
fun Project.configureKtlint() {
    val ktlintConfiguration = configurations.create("ktlint")

    tasks {
        val inputFiles =
            fileTree("src") {
                include("**/*.kt")
            }
        val outputDir = layout.buildDirectory.dir("reports/ktlint")

        register("ktlintFormat", JavaExec::class.java) {
            inputs.files(inputFiles)
            inputs.dir(outputDir)

            group = "Formatting"
            description = "Fix Kotlin code style deviations."
            mainClass.set("com.pinterest.ktlint.Main")
            classpath = ktlintConfiguration
            jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
            args = listOf("-F", "src/**/*.kt")
        }

        val ktlintTask =
            register("ktlint", JavaExec::class.java) {
                inputs.files(inputFiles)
                outputs.dir(outputDir)

                group = "Verification"
                description = "Check Kotlin code style."
                mainClass.set("com.pinterest.ktlint.Main")
                classpath = ktlintConfiguration
                args =
                    listOf(
                        "src/**/*.kt",
                        "--reporter=plain",
                        "--reporter=html,output=${outputDir.get().asFile.absolutePath}/ktlint.html",
                        "--reporter=checkstyle,output=${outputDir.get().asFile.absolutePath}/ktlint.xml}",
                    )
            }

        named("check") {
            dependsOn(ktlintTask)
        }
    }

    val libs = project.extensions.getByType<VersionCatalogsExtension>().named("libs")
    dependencies {
        ktlintConfiguration(
            libs.findLibrary("ktlint-cli").orElseThrow {
                NoSuchElementException("ktlint-cli not found in catalog")
            },
        ) {
            attributes {
                attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
            }
        }
    }
}
