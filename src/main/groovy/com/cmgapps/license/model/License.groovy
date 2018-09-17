package com.cmgapps.license.model

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = 'url', includeFields = true, useCanEqual = false)
final class License {
    String name
    String url
}
