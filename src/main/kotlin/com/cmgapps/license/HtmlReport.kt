/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import com.cmgapps.license.helper.LicensesHelper
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License

class HtmlReport(private val libraries: List<Library>) {

    companion object {
        private const val BODY_CSS = "body{font-family:sans-serif;background-colo   r:#eee}"
        private const val PRE_CSS = "pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}"
        private const val CSS_STYLE = "$BODY_CSS$PRE_CSS"

        private const val OPEN_SOURCE_LIBRARIES = "Open source licenses"

        private const val NOTICE_LIBRARIES = "Notice for packages:"
    }

    fun generate(): String {

        val licenseListMap = mutableMapOf<License, MutableList<Library>>()

        libraries.forEach { library ->

            if (!library.licenses.isNullOrEmpty()) {
                val key = library.licenses[0]

                if (!licenseListMap.contains(key)) {
                    licenseListMap.put(key, mutableListOf())
                }

                licenseListMap.get(key)?.add(library)
            }
        }

        return html {
            head {
                meta(mapOf("charset" to "UTF-8"))
                style {
                    +CSS_STYLE
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

                    if (LicensesHelper.LICENSE_MAP.containsKey(entry.key.url)) {
                        pre {
                            +getLicenseText(LicensesHelper.LICENSE_MAP.get(entry.key.url))
                        }
                    } else if (LicensesHelper.LICENSE_MAP.containsKey(entry.key.name)) {
                        pre {
                            +getLicenseText(LicensesHelper.LICENSE_MAP.get(entry.key.url))
                        }
                    } else {
                        div("license") {
                            p {
                                +"${entry.key.name}<br/>"
                            }
                            a(entry.key.url) {
                                +entry.key.url
                            }
                        }
                    }

                }
            }
        }.toString()
    }

    private fun getLicenseText(fileName: String?): String =
            javaClass.getResource("/licenses/$fileName").readText()
}


interface Element {
    fun render(builder: StringBuilder)
}

class TextElement(val text: String) : Element {
    override fun render(builder: StringBuilder) {
        builder.append(text)
    }
}

@DslMarker
annotation class HtmlTagMarker

@HtmlTagMarker
abstract class Tag(val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder) {
        builder.append("<$name").append(renderAttributes())

        if (children.isEmpty()) {
            builder.append("/>")
            return
        }

        builder.append('>')

        for (c in children) {
            c.render(builder)
        }
        builder.append("</$name>")
    }

    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        return builder.toString()
    }

    override fun toString(): String {
        return StringBuilder().apply {
            render(this)
        }.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}

class HTML : TagWithText("html") {

    init {
        attributes["lang"] = "en"
    }

    fun head(init: Head.() -> Unit) = initTag(Head(), init)

    fun body(init: Body.() -> Unit) = initTag(Body(), init)

    override fun render(builder: StringBuilder) {
        builder.append("<!DOCTYPE html>")
        super.render(builder)
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
class Meta : Element {
    val attributes = hashMapOf<String, String>()

    override fun render(builder: StringBuilder) {
        builder.append("<meta")
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        builder.append('>')
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
