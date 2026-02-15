/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle.ktlint

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.attributes.Bundling
import org.gradle.api.tasks.JavaExec

@Suppress("unused")
class KtlintPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val ktlintConfiguration = configurations.create("ktlint")

            val inputFiles =
                fileTree("src") { tree ->
                    tree.include("**/*.kt")
                    tree.exclude("**/build/**")
                }
            val outputDir = layout.buildDirectory.dir("reports/ktlint")

            tasks.register("ktlintFormat", JavaExec::class.java) { task ->
                task.inputs.files(inputFiles)
                task.inputs.dir(outputDir)

                task.group = "Formatting"
                task.description = "Fix Kotlin code style deviations."
                task.mainClass.set("com.pinterest.ktlint.Main")
                task.classpath = ktlintConfiguration
                task.jvmArgs = listOf("--add-opens=java.base/java.lang=ALL-UNNAMED")
                task.args =
                    listOf(
                        "-F",
                        "src/**/*.kt",
                        "!src/**/build/**",
                    )
            }

            val ktlintTask =
                tasks.register("ktlint", JavaExec::class.java) { task ->
                    task.inputs.files(inputFiles)
                    task.outputs.dir(outputDir)

                    task.group = "Verification"
                    task.description = "Check Kotlin code style."
                    task.mainClass.set("com.pinterest.ktlint.Main")
                    task.classpath = ktlintConfiguration
                    task.args =
                        listOf(
                            "src/**/*.kt",
                            "!src/**/build/**",
                            "--reporter=plain",
                            "--reporter=html,output=${outputDir.get().asFile.absolutePath}/ktlint.html",
                        )
                }

            tasks.named("check") { task ->
                task.dependsOn(ktlintTask)
            }

            val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

            val closure =
                object : Closure<Unit>(this) {
                    fun doCall(dependency: ModuleDependency) {
                        dependency.attributes {
                            it.attribute(
                                Bundling.BUNDLING_ATTRIBUTE,
                                objects.named(Bundling::class.java, Bundling.EXTERNAL),
                            )
                        }
                    }
                }

            dependencies.add(
                ktlintConfiguration.name,
                libs
                    .findLibrary("ktlint-cli")
                    .orElseThrow { NoSuchElementException("ktlint-cli not found in version catalog") },
                closure,
            )
        }
    }
}
