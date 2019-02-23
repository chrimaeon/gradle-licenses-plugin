/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.model

data class Library(val name: String, val version: String?, val description: String?, val licenses: List<License>)
