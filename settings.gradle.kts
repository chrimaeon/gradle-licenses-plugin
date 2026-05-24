/*
 * Copyright (c) 2019. Christian Grach <christian.grach@cmgapps.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

@file:Suppress("UnstableApiUsage")

import de.fayard.refreshVersions.core.FeatureFlag
import de.fayard.refreshVersions.core.StabilityLevel

rootProject.name = "gradle-licenses-plugin"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("de.fayard.refreshVersions") version "0.60.6"
}

refreshVersions {
    featureFlags {
        enable(FeatureFlag.GRADLE_UPDATES)
    }

    rejectVersionIf {
        candidate.stabilityLevel != StabilityLevel.Stable
    }
}
