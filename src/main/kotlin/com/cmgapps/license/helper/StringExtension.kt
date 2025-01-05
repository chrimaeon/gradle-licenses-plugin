/*
 * Copyright (c) 2025. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.license.helper

import java.util.Locale

fun String.uppercaseFirstChar(): String = replaceFirstChar { it.uppercase(Locale.US) }
