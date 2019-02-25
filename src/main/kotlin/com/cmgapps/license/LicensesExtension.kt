/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license


open class LicensesExtension {
    var outputType: OutputType? = null
}

enum class OutputType {
    HTML,
    XML,
    JSON
}