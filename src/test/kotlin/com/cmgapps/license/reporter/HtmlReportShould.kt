/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.reporter

import com.cmgapps.license.LicensesTask
import com.cmgapps.license.helper.LibrariesHelper
import com.cmgapps.license.util.TestUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class HtmlReportShould {

    @Test
    fun `generate HTML report`() {
        val result = HtmlReport(LibrariesHelper.libraries,
            LicensesTask.DEFAULT_BODY_CSS,
            LicensesTask.DEFAULT_PRE_CSS).generate()

        assertThat(result, `is`("<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "<meta charset=\"UTF-8\">" +
            "<style>body{font-family:sans-serif;background-color:#eee}pre,.license{background-color:#ddd;padding:1em}pre{white-space:pre-wrap}</style>" +
            "<title>Open source licenses</title>" +
            "</head>" +
            "<body>" +
            "<h3>Notice for packages:</h3>" +
            "<ul>" +
            "<li>Test lib 1</li>" +
            "</ul>" +
            "<pre>" +
            TestUtils.getFileContent("apache-2.0.txt") +
            "</pre>" +
            "</body>" +
            "</html>"))
    }
}