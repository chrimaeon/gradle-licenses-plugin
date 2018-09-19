/*
 * Copyright (c) 2018. <christian.grach@cmgapps.com>
 */

package com.cmgapps.license

import com.cmgapps.license.helper.LicensesHelper
import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import groovy.xml.MarkupBuilder

class HtmlReport {

    private final static def BODY_CSS = "body{font-family:sans-serif;background-color:#eee}"
    private final static
    def PRE_CSS = "pre{background-color:#ddd;padding:1em;white-space:pre-wrap;}"
    private final static def CSS_STYLE = BODY_CSS + " " + PRE_CSS

    private final static def OPEN_SOURCE_LIBRARIES = "Open source licenses"

    private final static def NOTICE_LIBRARIES = "Notice for packages:"

    private List<Library> mLibraries

    HtmlReport(def libraries) {
        mLibraries = libraries
    }

    def generate() {

        final Map<License, List<Library>> licenseListMap = [:]

        mLibraries.each { library ->

            if (library.licenses && library.licenses.size > 0) {
                def key = library.licenses[0]

                if (!licenseListMap.containsKey(key)) {
                    licenseListMap.put(key, [])
                }

                licenseListMap.get(key).add(library)
            }
        }

        final def stringWriter = new StringWriter()
        new MarkupBuilder(new IndentPrinter(stringWriter, '', false, false))
                .html(lang: 'en') {
            head {
                meta('charset': "UTF-8")
                style(CSS_STYLE)
                title(OPEN_SOURCE_LIBRARIES)
            }

            body {
                h3(NOTICE_LIBRARIES)
                licenseListMap.entrySet().each { entry ->
                    ul {
                        entry.value.sort {
                            left, right -> left.name <=> right.name
                        }.each { library ->
                            li(library.name)
                        }
                    }

                    if (LicensesHelper.LICENSE_MAP.containsKey(entry.key.url)) {
                        pre(getLicenseText(LicensesHelper.LICENSE_MAP."${entry.key.url}"))
                    } else if (LicensesHelper.LICENSE_MAP.containsKey(entry.key.name)) {
                        pre(getLicenseText(LicensesHelper.LICENSE_MAP."${entry.key.name}"))
                    } else {
                        pre {
                            mkp.yield("${entry.key.name}\n")
                            a('href': entry.key.url, entry.key.url)
                        }
                    }
                }
            }
        }

        return stringWriter.toString()
    }

    private String getLicenseText(def fileName) {
        getClass().getResource("/licenses/$fileName").text
    }

}
