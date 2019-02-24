/*
 * Copyright (c)  2019. Christian Grach <christian.grach@cmgapps.com>
 */

package com.cmgapps.license.helper

import com.cmgapps.license.model.Library
import com.cmgapps.license.model.License

object LibrariesHelper {

    val libraries = listOf(Library("Test lib 1", "1.0", "proper description", listOf(
            License("Apache 2.0", "http://www.apache.org/licenses/LICENSE-2.0.txt"), License("MIT License", "http://opensource.org/licenses/MIT"))))

}