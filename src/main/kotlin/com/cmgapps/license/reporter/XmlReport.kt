/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.reporter

internal class XmlReport(libraries: List<com.cmgapps.license.model.Library>) : Report(libraries) {

    override fun generate(): String {
        return libraries {
            for (library in libraries) {
                library(
                    id = library.mavenCoordinates.toString(),
                    version = library.mavenCoordinates.version.toString()
                ) {
                    name {
                        +(library.name ?: library.mavenCoordinates.toString())
                    }

                    description {
                        +(library.description ?: "")
                    }

                    licenses {
                        for (license in library.licenses) {
                            license(spdxLicenseIdentifier = license.id.spdxLicenseIdentifier, url = license.url) {
                                name {
                                    +license.name
                                }
                            }
                        }
                    }
                }
            }
        }.toString()
    }
}

internal class Libraries : Tag("libraries") {

    fun library(id: String, version: String, init: Library.() -> Unit): Library {
        val tag = initTag(Library(), init)
        tag.attributes["id"] = id
        tag.attributes["version"] = version
        return tag
    }

    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
        if (format) {
            builder.append('\n')
        }
        super.render(builder, intent, format)
    }
}

internal class Library : Tag("library") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)
    fun description(init: Description.() -> Unit) = initTag(Description(), init)
    fun licenses(init: Licenses.() -> Unit) = initTag(Licenses(), init)
}

internal class Name : TagWithText("name")
internal class Version : TagWithText("version")
internal class Description : TagWithText("description")
internal class Licenses : Tag("licenses") {
    fun license(spdxLicenseIdentifier: String? = null, url: String, init: License.() -> Unit) {
        val tag = initTag(License(), init)
        tag.attributes["url"] = url
        if (spdxLicenseIdentifier != null) {
            tag.attributes["spdx-license-identifier"] = spdxLicenseIdentifier
        }
    }
}

internal class License : Tag("license") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)
}

internal fun libraries(init: Libraries.() -> Unit): Libraries {
    val libraries = Libraries()
    libraries.attributes["xmlns"] = "https://www.cmgapps.com"
    libraries.attributes["xmlns:xsi"] = "http://www.w3.org/2001/XMLSchema-instance"
    libraries.attributes["xsi:schemaLocation"] = "https://www.cmgapps.com https://www.cmgapps.com/xsd/licenses.xsd"

    libraries.init()
    return libraries
}
