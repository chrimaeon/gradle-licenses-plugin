/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'url', includeFields = true, useCanEqual = false)
final class License {
    String name
    String url
}
