/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.repository.internal

import com.cmgapps.gradle.spdx.SpdxId
import com.cmgapps.license.repository.SpdxIdRepository
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.file.ProjectLayout
import org.gradle.api.logging.Logging
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal class SpdxIdRepositoryImpl(
    projectLayout: ProjectLayout,
) : SpdxIdRepository {
    private val licenseTextFileDir = projectLayout.buildDirectory.dir("tmp/licenses")
    private val logger = Logging.getLogger(SpdxIdRepositoryImpl::class.java)

    override fun getSpdxIds(
        url: String?,
        name: String?,
    ): List<SpdxId> = SpdxId.getSpdxIds(url = url, name = name)

    override fun SpdxId.licenseText(): String {
        val licenseTextFile = licenseTextFileDir.get().file(this.id.replace(' ', '_') + ".txt").asFile
        if (licenseTextFile.exists()) {
            logger.info("Reading license text for {} from cache", this.id)
            return licenseTextFile.readText()
        }

        val text = fetchLicenseText()
        licenseTextFile.apply {
            parentFile.mkdirs()
            logger.info("Writing license text for {} to {}", this@licenseText.id, this.path)
            createNewFile()
            writeText(text)
        }
        return text
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun SpdxId.fetchLicenseText(): String {
        logger.info("Fetching license text for {} from {}", this.id, this.detailsUrl)
        val request = HttpRequest.newBuilder(URI(this.detailsUrl)).GET().build()
        val httpResponse = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofInputStream())
        return Json
            .decodeFromStream(
                JsonObject.serializer(),
                httpResponse.body(),
            )["licenseText"]
            ?.jsonPrimitive
            ?.content ?: ""
    }
}
