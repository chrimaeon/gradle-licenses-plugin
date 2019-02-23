/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.model

data class License(val name: String, val url: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as License

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }
}

