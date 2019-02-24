/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.model.Library
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types


class JsonReport(private val libraries: List<Library>) : Report {

    private val moshi = Moshi.Builder().build()
    override fun generate(): String {
        val type = Types.newParameterizedType(List::class.java, Library::class.java)
        return moshi.adapter<List<Library>>(type).indent("  ").toJson(libraries)
    }
}