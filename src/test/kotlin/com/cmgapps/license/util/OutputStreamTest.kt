/*
 * Copyright (c) 2024. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.util

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.ByteArrayOutputStream

@Target(AnnotationTarget.FIELD)
annotation class TestStream

class OutputStreamExtension :
    BeforeEachCallback,
    AfterEachCallback {
    private lateinit var outputStream: ByteArrayOutputStream

    override fun beforeEach(context: ExtensionContext) {
        outputStream = ByteArrayOutputStream()
        context.requiredTestClass.fields
            .firstOrNull { field ->
                field.isAnnotationPresent(TestStream::class.java) && field.type == ByteArrayOutputStream::class.java
            }?.set(context.requiredTestInstance, outputStream) ?: throw IllegalStateException(
            "Test class should have a property annotated with @${TestStream::class.simpleName} and type ${ByteArrayOutputStream::class.qualifiedName}",
        )
    }

    override fun afterEach(context: ExtensionContext) {
        outputStream.close()
    }
}

fun ByteArrayOutputStream.asString() = String(this.toByteArray())
