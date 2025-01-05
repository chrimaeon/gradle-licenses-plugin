# Gradle Licenses Plugin [![Build & Test](https://github.com/chrimaeon/gradle-licenses-plugin/actions/workflows/main.yml/badge.svg)](https://github.com/chrimaeon/gradle-licenses-plugin/actions/workflows/main.yml) [![codecov](https://codecov.io/gh/chrimaeon/gradle-licenses-plugin/branch/master/graph/badge.svg?token=XY0G488B3B)](https://codecov.io/gh/chrimaeon/gradle-licenses-plugin)

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![Gradle Plugin](https://img.shields.io/badge/Gradle-7.2%2B-%2302303A.svg?style=for-the-badge&logo=Gradle)](https://gradle.org/)
[![MavenCentral](https://img.shields.io/maven-central/v/com.cmgapps/gradle-licenses-plugin?style=for-the-badge&logo=Apache%20Maven)](https://repo1.maven.org/maven2/com/cmgapps/gradle-licenses-plugin/)
[![gradlePluginPortal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/cmgapps/licenses/com.cmgapps.licenses.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal&style=for-the-badge&logo=Gradle)](https://plugins.gradle.org/plugin/com.cmgapps.licenses)

This Gradle plugin provides tasks to generate a file with the licenses used from the project's dependencies.

## Usage

### Integration

#### Using the plugins DSL

<details open="open">
<summary>Kotlin</summary>

```kotlin
plugins {
    id("com.cmgapps.licenses") version "5.0.0"
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
plugins {
    id 'com.cmgapps.licenses' version '5.0.0'
}
```
</details>

#### Using legacy plugin application

<details open="open">
<summary>Kotlin</summary>

```kotlin
buildscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("com.cmgapps:gradle-licenses-plugin:5.0.0")
    }
}

apply(plugin = "com.cmgapps.licenses")
```
</details>

<details>
<summary>Groovy</summary>

```groovy
buildscript {
    repositories {
        maven {
            url 'https://plugins.gradle.org/m2/'
        }
    }
    dependencies {
        classpath 'com.cmgapps:gradle-licenses-plugin:5.0.0'
    }
}

apply plugin: 'com.cmgapps.licenses'
```
</details>

### Tasks

Applying the plugin will create tasks to generate the license report

For `"java"` and `"java-library"`

* `licenseReport`

For `"com.android.application"`, `"com.android.library"`, `"com.android.feature"` and `"com.android.dynamic-feature"`

* `license<variant>Report`

For `"org.jetbrains.kotlin.multiplatform"`

* `licenseMultiplatformReport` collects licenses from all targets
* `licenseMultiplatform<target>Report` collects licenses from `common` and the specified `<target>`

### Configuration

#### Output Format

Example:

```kotlin
licenses {
    reports {
        html.enabled.set(false) // html is enabled by default
        xml {
            enabled.set(true)
            outputFile.set(file("$buildDir/reports/licenses.xml"))
        }
    }
}
```

The plugin can output different formats.

* `HTML`
  generates a formatted HTML website
    * Styling

      For an HTML report you can define custom stylesheet using a `File` or `String`:
       ```kotlin
        licenses {
            reports {
                html.stylesheet("body {background: #FAFAFA}")
            }     
        }
        ```
      or
        ```kotlin
        licenses {
            reports {
                html.stylesheet.set(file("$projectDir/styles/licenses.css"))
            } 
        }
        ```
      
    * On the default CSS style Dark Mode for supported browsers is also enabled by default. It adds a `<meta name="color-scheme" content="dark light">` and a custom css theme.      

      It can be disabled via
      ```kotlin
      licenses {
          reports {
              html.useDarkMode.set(false)
          }
      }
      ```
* `JSON`
  generates a JSON file
* `XML`
  generates a valid XML version 1.0 file
* `Text`
  generates a plain text report file
* `Mardown`
  generates a Markdown file
* `Custom`
  add your own reporter as a lambda function
  
  <details open="open">
  <summary>Kotlin</summary>
    
  ```kotlin
    licenses {
        custom {
            enabled.set(true)
            outputFile.set(buildDir.resolve("reports").resolve("licenses.txt"))
            generateor.set { list -> list.map { it.name }.joinToString() }
        }
    }
    ```
  </details>

  <details>
  <summary>Groovy</summary>
  
  ```groovy
    licenses {
        custom {
            enabled.set(true)
            outputFile.set(file("$buildDir/reports/licenses/licenses.txt"))
            def builder = { list -> list.collect { it.name }.join(', ') } as com.cmgapps.license.reporter.CustomReportGenerator
            generator.set(builder)
        }
   }
   ```
   </details>

#### Multi-project Builds

For multi-project build, you can add projects you want to collect license information from in the main project.

<details open="open">
<summary>Kotlin</summary>

```kotlin
licenses {
    additionalProjects(":module2", ":module3")
}
```
</details>

<details>
<summary>Groovy</summary>

```groovy
licenses {
    additionalProjects ':module2', ':module3'
}
```
</details>

## License

```text
Copyright (c) 2018-2024. Christian Grach <christian.grach@cmgapps.com>

SPDX-License-Identifier: Apache-2.0
```

[TextResource]: https://docs.gradle.org/current/dsl/org.gradle.api.resources.TextResource.html
