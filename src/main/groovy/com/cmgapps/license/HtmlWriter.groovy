package com.cmgapps.license

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License
import groovy.xml.MarkupBuilder
import org.apache.commons.io.output.StringBuilderWriter

class HtmlReport {

    private final static def BODY_CSS = "body{font-family:sans-serif;background-color:#eee}"
    private final static
    def PRE_CSS = "pre{background-color:#ddd;padding:1em;white-space:pre-wrap;}"
    private final static def CSS_STYLE = BODY_CSS + " " + PRE_CSS

    private final static def OPEN_SOURCE_LIBRARIES = "Open source licenses"

    private final static def NO_LICENSE = "No license found"
    private final static def NO_URL = "N/A"

    private final static def NOTICE_LIBRARIES = "Notice for packages:"

    private List<Library> mLibraries

    HtmlReport(def libraries) {
        mLibraries = libraries
    }

    def generate() {
        final def stringWriter = new StringBuilderWriter()
        final def markup = new MarkupBuilder(stringWriter)

        final Map<License, List<Library>> licenseListMap = new HashMap<>()

        mLibraries.each { library ->
            def key = new License(name: NO_LICENSE, url: NO_URL)

            if (library.licenses && library.licenses.size > 0) {
                key = library.licenses[0]
            }

            if (!licenseListMap.containsKey(key)) {
                licenseListMap.put(key, [])
            }

            licenseListMap.get(key).add(library)
        }

        markup.html(lang: 'en') {
            head {
                meta('charset': "UTF-8")
                style(CSS_STYLE)
                title(OPEN_SOURCE_LIBRARIES)
            }

            body {
                h3(NOTICE_LIBRARIES)
                licenseListMap.entrySet().each { entry ->
                    ul {


                        final List<Library> sortedLibraries = entry.value.sort {
                            left, right -> left.name <=> right.name
                        }

                        sortedLibraries.each { library ->
                            li("${library.name} $library.version")
                        }
                    }

                    pre(entry.key.name)
                }
            }
        }

        return stringWriter.toString()
    }

}
