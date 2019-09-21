/*
 * Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmgapps.license.helper

/**
 * Map License name and URL to license text file.
 *
 * Based on "popular and widely-used or with strong communities" found here: https://opensource.org/licenses/category.
 * License text from: https://github.com/github/choosealicense.com/blob/gh-pages/_licenses.
 */
internal object LicensesHelper {

    private const val APACHE_2_FILE_NAME = "apache-2.0.txt"
    private const val BSD_2_FILE_NAME = "bsd-2-clause.txt"
    private const val BSD_3_FILE_NAME = "bsd-3-clause.txt"
    private const val EPL_2_FILE_NAME = "epl-2.0.txt"
    private const val GPL_2_FILE_NAME = "gpl-2.0.txt"
    private const val GPL_3_FILE_NAME = "gpl-3.0.txt"
    private const val LGPL_2_1_FILE_NAME = "lgpl-2.1.txt"
    private const val LGPL_3_FILE_NAME = "lgpl-3.0.txt"
    private const val MIT_FILE_NAME = "mit.txt"
    private const val MPL_2_FILE_NAME = "mpl-2.0.txt"
    private const val CDDL_FILE_NAME = "cddl.txt"

    @JvmField
    val LICENSE_MAP = mapOf(
        // Apache License 2.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/apache-2.0.txt
        "Apache-2.0" to APACHE_2_FILE_NAME,
        "Apache 2.0" to APACHE_2_FILE_NAME,
        "Apache v2" to APACHE_2_FILE_NAME,
        "Apache License 2.0" to APACHE_2_FILE_NAME,
        "The Apache Software License" to APACHE_2_FILE_NAME,
        "The Apache Software License, Version 2.0" to APACHE_2_FILE_NAME,
        "http://www.apache.org/licenses/LICENSE-2.0.txt" to APACHE_2_FILE_NAME,
        "https://www.apache.org/licenses/LICENSE-2.0.txt" to APACHE_2_FILE_NAME,
        "http://opensource.org/licenses/Apache-2.0" to APACHE_2_FILE_NAME,
        "https://opensource.org/licenses/Apache-2.0" to APACHE_2_FILE_NAME,

        // BSD 2-Clause "Simplified" License
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-2-clause.txt
        "BSD-2-Clause" to BSD_2_FILE_NAME,
        "BSD 2-Clause \"Simplified\" License" to BSD_2_FILE_NAME,
        "http://opensource.org/licenses/BSD-2-Clause" to BSD_2_FILE_NAME,
        "https://opensource.org/licenses/BSD-2-Clause" to BSD_2_FILE_NAME,

        // BSD 3-Clause "New" or "Revised" License
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/bsd-3-clause.txt
        "BSD-3-Clause" to BSD_3_FILE_NAME,
        "BSD 3-Clause \"New\" or \"Revised\" License" to BSD_3_FILE_NAME,
        "http://opensource.org/licenses/BSD-3-Clause" to BSD_3_FILE_NAME,
        "https://opensource.org/licenses/BSD-3-Clause" to BSD_3_FILE_NAME,

        // Eclipse Public License 2.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/epl-2.0.txt
        "EPL-2.0" to EPL_2_FILE_NAME,
        "Eclipse Public License 2.0" to EPL_2_FILE_NAME,
        "http://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to EPL_2_FILE_NAME,
        "https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt" to EPL_2_FILE_NAME,
        "http://opensource.org/licenses/EPL-2.0" to EPL_2_FILE_NAME,
        "https://opensource.org/licenses/EPL-2.0" to EPL_2_FILE_NAME,

        // GNU General Public License v2.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-2.0.txt
        "GPL-2.0" to GPL_2_FILE_NAME,
        "GNU General Public License v2.0" to GPL_2_FILE_NAME,
        "http://www.gnu.org/licenses/gpl-2.0.txt" to GPL_2_FILE_NAME,
        "https://www.gnu.org/licenses/gpl-2.0.txt" to GPL_2_FILE_NAME,
        "http://opensource.org/licenses/GPL-2.0" to GPL_2_FILE_NAME,
        "https://opensource.org/licenses/GPL-2.0" to GPL_2_FILE_NAME,

        // GNU General Public License v3.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/gpl-3.0.txt
        "GPL-3.0" to GPL_3_FILE_NAME,
        "GNU General Public License v3.0" to GPL_3_FILE_NAME,
        "https//www.gnu.org/licenses/gpl-3.0.txt" to GPL_3_FILE_NAME,
        "https://www.gnu.org/licenses/gpl-3.0.txt" to GPL_3_FILE_NAME,
        "http://opensource.org/licenses/GPL-3.0" to GPL_3_FILE_NAME,
        "https://opensource.org/licenses/GPL-3.0" to GPL_3_FILE_NAME,

        // GNU Lesser General Public License v2.1
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-2.1.txt
        "LGPL-2.1" to LGPL_2_1_FILE_NAME,
        "GNU Lesser General Public License v2.1" to LGPL_2_1_FILE_NAME,
        "http://www.gnu.org/licenses/lgpl-2.1.txt" to LGPL_2_1_FILE_NAME,
        "https://www.gnu.org/licenses/lgpl-2.1.txt" to LGPL_2_1_FILE_NAME,
        "http://opensource.org/licenses/LGPL-2.1" to LGPL_2_1_FILE_NAME,
        "https://opensource.org/licenses/LGPL-2.1" to LGPL_2_1_FILE_NAME,

        // GNU Lesser General Public License v3.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/lgpl-3.0.txt
        "LGPL-3.0" to LGPL_3_FILE_NAME,
        "GNU Lesser General Public License v3.0" to LGPL_3_FILE_NAME,
        "http://www.gnu.org/licenses/lgpl-3.0.txt" to LGPL_3_FILE_NAME,
        "https://www.gnu.org/licenses/lgpl-3.0.txt" to LGPL_3_FILE_NAME,
        "http://opensource.org/licenses/LGPL-3.0" to LGPL_3_FILE_NAME,
        "https://opensource.org/licenses/LGPL-3.0" to LGPL_3_FILE_NAME,

        // MIT License
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mit.txt
        "MIT" to MIT_FILE_NAME,
        "MIT License" to MIT_FILE_NAME,
        "http://opensource.org/licenses/MIT" to MIT_FILE_NAME,
        "https://opensource.org/licenses/MIT" to MIT_FILE_NAME,

        // Mozilla Public License 2.0
        // https://github.com/github/choosealicense.com/blob/gh-pages/_licenses/mpl-2.0.txt
        "MPL-2.0" to MPL_2_FILE_NAME,
        "Mozilla Public License 2.0" to MPL_2_FILE_NAME,
        "http://www.mozilla.org/media/MPL/2.0/index.txt" to MPL_2_FILE_NAME,
        "https://www.mozilla.org/media/MPL/2.0/index.txt" to MPL_2_FILE_NAME,
        "http://opensource.org/licenses/MPL-2.0" to MPL_2_FILE_NAME,
        "https://opensource.org/licenses/MPL-2.0" to MPL_2_FILE_NAME,

        // Common Development and Distribution License 1.0
        "CDDL-1.0" to CDDL_FILE_NAME,
        "Common Development and Distribution License 1.0" to CDDL_FILE_NAME,
        "http://opensource.org/licenses/cddl1" to CDDL_FILE_NAME,
        "https://opensource.org/licenses/cddl1" to CDDL_FILE_NAME
    )
}
