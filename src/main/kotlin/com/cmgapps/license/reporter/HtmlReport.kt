/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
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

import com.cmgapps.license.helper.LicensesHelper
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License

class HtmlReport(
    private val libraries: List<Library>,
    private val bodyCss: String,
    private val preCss: String
) : Report {

    companion object {
        private const val OPEN_SOURCE_LIBRARIES = "Open source licenses"

        private const val NOTICE_LIBRARIES = "Notice for packages:"
    }

    override fun generate(): String {

        val licenseListMap = mutableMapOf<License, MutableList<Library>>()

        libraries.forEach { library ->

            if (!library.licenses.isNullOrEmpty()) {
                val key = library.licenses[0]

                if (!licenseListMap.contains(key)) {
                    licenseListMap[key] = mutableListOf()
                }

                licenseListMap[key]?.add(library)
            }
        }

        return html {
            head {
                meta(mapOf("charset" to "UTF-8"))
                style {
                    +"$bodyCss$preCss"
                }
                title {
                    +OPEN_SOURCE_LIBRARIES
                }
            }

            body {
                h3 {
                    +NOTICE_LIBRARIES
                }

                licenseListMap.entries.forEach { entry ->
                    ul {
                        with(entry.value) {
                            sortBy { it.name }
                            forEach { library ->
                                li {
                                    +library.name
                                }
                            }
                        }
                    }

                    when {
                        LicensesHelper.LICENSE_MAP.containsKey(entry.key.url) -> pre {
                            +(getLicenseText(LicensesHelper.LICENSE_MAP[entry.key.url]) ?: "")
                        }
                        LicensesHelper.LICENSE_MAP.containsKey(entry.key.name) -> pre {
                            +(getLicenseText(LicensesHelper.LICENSE_MAP[entry.key.name]) ?: "")
                        }
                        else -> div("license") {
                            p {
                                +entry.key.name
                            }
                            a(entry.key.url) {
                                +entry.key.url
                            }
                        }
                    }
                }
            }
        }.toString(false)
    }

    private fun getLicenseText(fileName: String?): String? =
        javaClass.getResource("/licenses/$fileName")?.readText()
}

class HTML : TagWithText("html") {

    init {
        attributes["lang"] = "en"
    }

    fun head(init: Head.() -> Unit) = initTag(Head(), init)

    fun body(init: Body.() -> Unit) = initTag(Body(), init)

    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        builder.append("<!DOCTYPE html>")
        if (format) {
            builder.append('\n')
        }
        super.render(builder, intent, format)
    }
}

class Head : TagWithText("head") {
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun meta(attrs: Map<String, String>) {
        val meta = initTag(Meta()) {}
        meta.attributes.putAll(attrs)
    }

    fun style(init: Style.() -> Unit) = initTag(Style(), init)
}

class Title : TagWithText("title")
class Meta : Tag("meta") {

    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        if (format) {
            builder.append(intent)
        }

        builder.append("<$name")
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        builder.append(">")
        if (format) {
            builder.append('\n')
        }
    }
}

class Style : TagWithText("style")

abstract class BodyTag(name: String) : TagWithText(name) {
    fun pre(init: Pre.() -> Unit) = initTag(Pre(), init)
    fun h3(init: H3.() -> Unit) = initTag(H3(), init)
    fun ul(init: Ul.() -> Unit) = initTag(Ul(), init)
    fun li(init: Li.() -> Unit) = initTag(Li(), init)
    fun a(href: String, init: A.() -> Unit) {
        val a = initTag(A(), init)
        a.href = href
    }

    fun div(`class`: String, init: Div.() -> Unit) {
        val div = initTag(Div(), init)
        div.`class` = `class`
    }

    fun p(init: P.() -> Unit) = initTag(P(), init)
}

class Body : BodyTag("body")
class Pre : BodyTag("pre")
class H3 : BodyTag("h3")
class Ul : BodyTag("ul")
class Li : BodyTag("li")
class A : BodyTag("a") {
    var href: String
        get() = attributes["href"]!!
        set(value) {
            attributes["href"] = value
        }
}

class Div : BodyTag("div") {
    var `class`: String
        get() = attributes["class"]!!
        set(value) {
            attributes["class"] = value
        }
}

class P : BodyTag("p")

fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}
