/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.util

object TestUtils {
    fun getFileContent(fileName: String) = javaClass.getResource("/licenses/$fileName").readText()
}