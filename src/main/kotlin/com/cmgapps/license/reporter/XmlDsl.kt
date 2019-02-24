/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.reporter

interface Element {
    fun render(builder: StringBuilder, intent: String, format: Boolean)
}

class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        if (format) {
            builder.append(intent)
        }
        builder.append(text)
        if (format) {
            builder.append('\n')
        }
    }
}

@DslMarker
annotation class HtmlTagMarker

@HtmlTagMarker
abstract class Tag(private val name: String) : Element {
    val children = arrayListOf<Element>()
    val attributes = hashMapOf<String, String>()

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun render(builder: StringBuilder, intent: String, format: Boolean) {
        if (format) {
            builder.append(intent)
        }
        builder.append("<$name").append(renderAttributes())

        if (children.isEmpty()) {
            builder.append("/>")
            if (format) {
                builder.append('\n')
            }
            return
        }

        builder.append(">")

        if (format) {
            builder.append('\n')
        }

        for (c in children) {
            c.render(builder, "$intent  ", format)
        }

        if (format) {
            builder.append(intent)
        }
        builder.append("</$name>")
        if (format) {
            builder.append('\n')
        }
    }

    private fun renderAttributes(): String {
        val builder = StringBuilder()
        for ((attr, value) in attributes) {
            builder.append(" $attr=\"$value\"")
        }
        return builder.toString()
    }

    fun toString(format: Boolean = true): String = StringBuilder().apply {
        render(this, "", format)
    }.toString()

    override fun toString(): String = toString(true)
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}
