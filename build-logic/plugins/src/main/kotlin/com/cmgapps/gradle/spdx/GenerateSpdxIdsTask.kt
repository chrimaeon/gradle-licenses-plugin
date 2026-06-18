/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle.spdx

import com.cmgapps.gradle.spdx.model.SpdxLicenseJson
import com.cmgapps.gradle.spdx.model.SpdxLicenses
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import kotlinx.serialization.ExperimentalSerializationApi
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

private val SpdxId = ClassName("com.cmgapps.gradle.spdx", "SpdxId")
private val SpdxIdCompanion: ClassName = SpdxId.nestedClass("Companion")

@CacheableTask
abstract class GenerateSpdxIdsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputJson: RegularFileProperty

    @get:OutputDirectory
    abstract val generatedSpdx: DirectoryProperty

    init {
        group = "generateSpdx"
        generatedSpdx.convention(project.layout.buildDirectory.dir("generated/spdx"))
    }

    @OptIn(ExperimentalSerializationApi::class)
    @TaskAction
    fun write() {
        val parsed = SpdxLicenses.parseJson(inputJson.asFile.get().readText(), defaultFallbackUrls)

        parsed.generate().writeTo(generatedSpdx.get().asFile)
    }
}

private fun SpdxLicenses.generate(): FileSpec {
    val fileSpec = FileSpec.builder("com.cmgapps.gradle.spdx", "SpdxId")
    val spdxId =
        TypeSpec
            .classBuilder("SpdxId")
            .apply {
                addModifiers(KModifier.PUBLIC)
                addAnnotation(ClassName("dev.drewhamilton.poko", "Poko"))
                primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("id", STRING)
                        .addParameter("name", STRING)
                        .addParameter("url", STRING)
                        .addParameter("detailsUrl", STRING)
                        .build(),
                )
                addProperty(PropertySpec.builder("id", STRING).initializer("id").build())
                addProperty(PropertySpec.builder("name", STRING).initializer("name").build())
                addProperty(PropertySpec.builder("url", STRING).initializer("url").build())
                addProperty(PropertySpec.builder("detailsUrl", STRING).initializer("detailsUrl").build())
                addSuperinterface(java.io.Serializable::class)
                addType(spdxIdCompanion())
            }.build()

    fileSpec.addType(
        spdxId,
    )

    return fileSpec.build()
}

private val SpdxLicenseJson.identifier: String
    get() =
        when (id) {
            // Special-case IDs which start with a digit:
            "0BSD" -> {
                "ZeroBSD"
            }

            "3D-Slicer-1.0" -> {
                "ThreeD_Slicer_10"
            }

            else -> {
                id.replace("-", "_").replace(".", "").replace("+", "Plus")
            }
        }

private fun SpdxLicenses.spdxIdCompanion(): TypeSpec =
    TypeSpec
        .companionObjectBuilder()
        .apply {
            for ((_, license) in identifierToLicense) {
                addProperty(
                    PropertySpec
                        .builder(license.identifier, SpdxId, KModifier.INTERNAL)
                        .apply {
                            addAnnotation(JvmField::class)
                            addKdoc(license.name)
                            initializer(
                                "%T(%S, %S, %S, %S)",
                                SpdxId,
                                license.id,
                                license.name,
                                license.spdxUrl,
                                license.detailsUrl,
                            )
                        }.build(),
                )
            }

            val findByIdentifier = findByIdentifier()
            addFunction(findByIdentifier)

            val findByUrl = findByUrl()
            addFunction(findByUrl)
            addFunction(getSpdxIds(findByUrl = findByUrl, findByIdentifier = findByIdentifier))
        }.build()

private fun SpdxLicenses.findByIdentifier(): FunSpec =
    FunSpec
        .builder("findByIdentifier")
        .apply {
            addAnnotation(JvmStatic::class)
            addModifiers(KModifier.INTERNAL)
            addParameter("id", STRING)
            returns(SpdxId.copy(nullable = true))

            beginControlFlow("return when (id)")
            for ((_, license) in identifierToLicense) {
                addCode("%S -> %M\n", license.id, SpdxIdCompanion.member(license.identifier))
            }
            addCode("else -> null\n")
            endControlFlow()
        }.build()

private fun SpdxLicenses.findByUrl(): FunSpec =
    FunSpec
        .builder("findByUrl")
        .apply {
            addParameter("url", STRING)
            addModifiers(KModifier.INTERNAL)
            returns(LIST.parameterizedBy(SpdxId))

            beginControlFlow("return when (url)")
            for ((urls, licenses) in simplified) {
                for (url in urls) {
                    addCode("%S,\n", url)
                }
                addCode(" -> listOf(")
                for (license in licenses) {
                    addCode("\n%M,", SpdxIdCompanion.member(license.identifier))
                }
                addCode("\n)\n")
            }
            addCode("else -> emptyList()\n")
            endControlFlow()
        }.build()

private fun getSpdxIds(
    findByUrl: FunSpec,
    findByIdentifier: FunSpec,
): FunSpec =
    FunSpec
        .builder("getSpdxIds")
        .apply {
            val urmParam = ParameterSpec.builder("url", STRING.copy(nullable = true)).build()
            addParameter(urmParam)
            val nameParam = ParameterSpec.builder("name", STRING.copy(nullable = true)).build()
            addParameter(nameParam)
            addModifiers(KModifier.INTERNAL)
            returns(LIST.parameterizedBy(SpdxId))

            beginControlFlow("return when")
            addStatement("%N != null -> %N(%N)", urmParam, findByUrl, urmParam)
            beginControlFlow("%N != null ->", nameParam)
            addComment("Only fallback to name-based matching if the URL is null.")
            addCode(
                """
                |  val license = %N(%N)
                |  if (license != null) {
                |    listOf(license)
                |  } else {
                |    emptyList()
                |  }
                |
                """.trimMargin(),
                findByIdentifier,
                nameParam,
            )
            endControlFlow()
            addStatement("else -> emptyList()")
            endControlFlow()
        }.build()
