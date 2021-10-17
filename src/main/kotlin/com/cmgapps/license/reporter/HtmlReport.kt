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
import org.gradle.api.logging.Logger
import org.gradle.api.resources.TextResource

internal class HtmlReport(
    libraries: List<Library>,
    private val css: TextResource?,
    private val logger: Logger
) : Report(libraries) {

    companion object {
        private const val DEFAULT_PRE_CSS = "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}"
        private const val DEFAULT_BODY_CSS = "body{font-family:sans-serif;background-color:#eee}"
        private const val DEFAULT_CSS = "$DEFAULT_BODY_CSS$DEFAULT_PRE_CSS"
        private const val OPEN_SOURCE_LIBRARIES = "Open source licenses"

        private const val NOTICE_LIBRARIES = "Notice for packages:"
    }

    override fun generate(): String {

        val licenseListMap = mutableMapOf<License, MutableList<Library>>()

        libraries.forEach { library ->

            if (library.licenses.isNotEmpty()) {
                val key = library.licenses[0]

                licenseListMap[key] ?: mutableListOf<Library>().also {
                    licenseListMap[key] = it
                }.add(library)
            }
        }

        return html {
            head {
                meta(mapOf("charset" to "UTF-8"))
                style {
                    +(css?.asString() ?: DEFAULT_CSS)
                }
                title {
                    +OPEN_SOURCE_LIBRARIES
                }
            }

            body {
                h3 {
                    +NOTICE_LIBRARIES
                }

                licenseListMap.entries.forEach { (license, libraries) ->
                    ul {
                        libraries.asSequence().sortedBy { it.name }.forEach { library ->
                            li {
                                +library.name
                            }
                        }
                    }

                    val licenseUrl = license.url
                    val licenseName = license.name

                    when {
                        LicensesHelper.LICENSE_MAP.containsKey(licenseUrl) -> pre {
                            +(getLicenseText(LicensesHelper.LICENSE_MAP[licenseUrl]) ?: "")
                        }
                        LicensesHelper.LICENSE_MAP.containsKey(licenseName) -> pre {
                            +(getLicenseText(LicensesHelper.LICENSE_MAP[licenseName]) ?: "")
                        }
                        else -> {
                            logger.warn(
                                """
                                    |No mapping found for license: '$licenseName' with url '$licenseUrl'
                                    |used by ${libraries.joinToString { "'${it.name}'" }}
                                    |
                                    |If it is a valid Open Source License, please report to https://github.com/chrimaeon/gradle-licenses-plugin/issues 
                                    """.trimMargin()
                            )
                            div("license") {
                                p {
                                    +licenseName
                                }
                                a(licenseUrl) {
                                    +licenseUrl
                                }
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

internal class HTML : TagWithText("html") {

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

internal class Head : TagWithText("head") {
    fun title(init: Title.() -> Unit) = initTag(Title(), init)
    fun meta(attrs: Map<String, String>) {
        val meta = initTag(Meta()) {}
        meta.attributes.putAll(attrs)
    }

    fun style(init: Style.() -> Unit) = initTag(Style(), init)
}

internal class Title : TagWithText("title")
internal class Meta : Tag("meta") {

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

internal class Style : TagWithText("style")

internal abstract class BodyTag(name: String) : TagWithText(name) {
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

internal class Body : BodyTag("body")
internal class Pre : BodyTag("pre")
internal class H3 : BodyTag("h3")
internal class Ul : BodyTag("ul")
internal class Li : BodyTag("li")
internal class A : BodyTag("a") {
    var href: String
        get() = attributes["href"]!!
        set(value) {
            attributes["href"] = value
        }
}

internal class Div : BodyTag("div") {
    var `class`: String
        get() = attributes["class"]!!
        set(value) {
            attributes["class"] = value
        }
}

internal class P : BodyTag("p")

internal fun html(init: HTML.() -> Unit): HTML {
    val html = HTML()
    html.init()
    return html
}
