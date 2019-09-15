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
abstract class Tag(protected val name: String) : Element {
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
