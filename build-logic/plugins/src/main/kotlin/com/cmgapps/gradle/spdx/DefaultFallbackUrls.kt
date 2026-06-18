/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.cmgapps.gradle.spdx

import com.cmgapps.gradle.spdx.model.FallbackBuilder

@Suppress("HttpUrlsUsage")
internal val defaultFallbackUrls: FallbackBuilder.() -> Unit = {
    putLicense("Apache-2.0") {
        add("http://www.apache.org/licenses/LICENSE-2.0.txt")
        add("https://www.apache.org/licenses/LICENSE-2.0.txt")
        add("http://www.apache.org/licenses/LICENSE-2.0.html")
        add("https://www.apache.org/licenses/LICENSE-2.0.html")
        add("http://www.opensource.org/licenses/apache2.0.php")
        add("https://www.opensource.org/licenses/apache2.0.php")
        add("http://www.apache.org/licenses/LICENSE-2.0")
        add("http://api.github.com/licenses/apache-2.0")
        add("https://api.github.com/licenses/apache-2.0")
    }
    putLicense("CC0-1.0") {
        add("http://creativecommons.org/publicdomain/zero/1.0/")
        add("https://creativecommons.org/publicdomain/zero/1.0/")
        add("http://api.github.com/licenses/cc0-1.0")
        add("https://api.github.com/licenses/cc0-1.0")
    }
    putLicense("LGPL-2.1-only") {
        add("http://www.opensource.org/licenses/LGPL-2.1")
        add("https://www.opensource.org/licenses/LGPL-2.1")
        add("http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html")
        add("http://api.github.com/licenses/lgpl-2.1")
        add("https://api.github.com/licenses/lgpl-2.1")
    }
    putLicense("MIT") {
        add("http://opensource.org/licenses/mit-license")
        add("https://opensource.org/licenses/mit-license")
        add("https://opensource.org/license/mit")
        add("http://www.opensource.org/licenses/mit-license.php")
        add("https://www.opensource.org/licenses/mit-license.php")
        add("http://api.github.com/licenses/mit")
        add("https://api.github.com/licenses/mit")
    }
    putLicense("BSD-2-Clause") {
        add("http://www.opensource.org/licenses/bsd-license")
        add("https://www.opensource.org/licenses/bsd-license")
        add("http://www.opensource.org/licenses/bsd-license.php")
        add("https://www.opensource.org/licenses/bsd-license.php")
        add("http://api.github.com/licenses/bsd-2-clause")
        add("https://api.github.com/licenses/bsd-2-clause")
    }
    putLicense("BSD-3-Clause") {
        add("http://opensource.org/licenses/BSD-3-Clause")
        add("http://api.github.com/licenses/bsd-3-clause")
        add("https://api.github.com/licenses/bsd-3-clause")
    }
    putLicense("GPL-2.0-with-classpath-exception") {
        add("http://www.gnu.org/software/classpath/license.html")
    }
    putLicense("GPL-2.0", "GPL-2.0-or-later") {
        add("http://choosealicense.com/licenses/gpl-2.0")
        add("https://choosealicense.com/licenses/gpl-2.0")
        add("http://opensource.org/license/gpl-2-0")
        add("https://opensource.org/license/gpl-2-0")
        add("http://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
        add("https://www.gnu.org/licenses/old-licenses/gpl-2.0.html")
        add("http://api.github.com/licenses/gpl-2.0")
        add("https://api.github.com/licenses/gpl-2.0")
    }
    putLicense("EPL-1.0") {
        add("http://www.eclipse.org/org/documents/epl-v10.php")
        add("https://www.eclipse.org/org/documents/epl-v10.php")
        add("http://api.github.com/licenses/epl-1.0")
        add("https://api.github.com/licenses/epl-1.0")
    }
    putLicense("EPL-2.0") {
        add("http://www.eclipse.org/legal/epl-2.0/")
        add("https://www.eclipse.org/legal/epl-2.0/")
        add("http://api.github.com/licenses/epl-2.0")
        add("https://api.github.com/licenses/epl-2.0")
    }
    putLicense("ISC") { add("https://opensource.org/licenses/isc-license.txt") }
}
