/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license.reporter

class XmlReport(libraries: List<com.cmgapps.license.model.Library>) : Report(libraries) {

    override fun generate(): String {
        return libraries {
            for (library in libraries) {
                library {
                    name {
                        +library.name
                    }
                    version {
                        +(library.version ?: "")
                    }

                    description {
                        +(library.description ?: "")
                    }

                    licenses {
                        for (license in library.licenses) {
                            license {
                                name {
                                    +license.name
                                }
                                url {
                                    +license.url
                                }
                            }
                        }
                    }
                }
            }
        }.toString()
    }
}

class Libraries : Tag("libraries") {

    fun library(init: Library.() -> Unit) = initTag(Library(), init)

    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>")
        if (format) {
            builder.append('\n')
        }
        super.render(builder, intent, format)
    }
}

class Library : Tag("library") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)
    fun version(init: Version.() -> Unit) = initTag(Version(), init)
    fun description(init: Description.() -> Unit) = initTag(Description(), init)
    fun licenses(init: Licenses.() -> Unit) = initTag(Licenses(), init)
}

class Name : TagWithText("name")
class Version : TagWithText("version")
class Description : TagWithText("description")
class Licenses : Tag("licenses") {
    fun license(init: License.() -> Unit) = initTag(License(), init)
}

class License : Tag("license") {
    fun name(init: Name.() -> Unit) = initTag(Name(), init)
    fun url(init: Url.() -> Unit) = initTag(Url(), init)
}

class Url : TagWithText("url")

fun libraries(init: Libraries.() -> Unit): Libraries {
    val libraries = Libraries()
    libraries.init()
    return libraries
}
