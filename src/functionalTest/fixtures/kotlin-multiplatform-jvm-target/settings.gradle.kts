/*
 * Copyright (c) 2026. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

rootProject.name = "multi-project-collect-additional"

pluginManagement {
    includeBuild("../../test-build-logic")
}

plugins {
    id("licenseTests")
}
