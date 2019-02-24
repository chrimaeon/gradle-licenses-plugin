package com.cmgapps.license


open class LicensesExtension {
    var outputType: OutputType = OutputType.HTML

}

enum class OutputType {
    HTML,
    XML
}