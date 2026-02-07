/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

plugins {
    id("java")
    id("com.cmgapps.licenses")
}

licenses {
    reports {
        csv {
            enabled.set(true)
        }
        custom {
            enabled.set(true)
            generator.set({ licenses -> licenses.joinToString(separator = "\n") { "${it.name} -> ${it.licenses.first().name}" } })
        }
        html {
            enabled.set(true)
            stylesheet("body {background: #FAFAFA}")
        }
        json {
            enabled.set(true)
        }
        markdown {
            enabled.set(true)
        }
        plainText {
            enabled.set(true)
        }
        xml {
            enabled.set(true)
        }
    }
}

dependencies {
    implementation("group:name:1.0.0")
}
