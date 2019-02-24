package com.cmgapps.license.reporter

interface Element {
    fun render(builder: StringBuilder, intent: String)
}

class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder, intent: String) {
        builder.append("$intent$text\n")
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

    override fun render(builder: StringBuilder, intent: String) {
        builder.append("$intent<$name").append(renderAttributes())

        if (children.isEmpty()) {
            builder.append("/>\n")
            return
        }

        builder.append(">\n")

        for (c in children) {
            c.render(builder, "$intent  ")
        }
        builder.append("$intent</$name>\n")
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
            render(this, "")
        }.toString()
    }
}

abstract class TagWithText(name: String) : Tag(name) {
    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }
}
