# Gradle Licenses Plugin [![CircleCI](https://circleci.com/gh/chrimaeon/gradle-licenses-plugin.svg?style=svg)](https://circleci.com/gh/chrimaeon/gradle-licenses-plugin)

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![MavenCentral](https://img.shields.io/maven-central/v/com.cmgapps/gradle-licenses-plugin?style=for-the-badge)](https://repo1.maven.org/maven2/com/cmgapps/gradle-licenses-plugin/)
[![gradlePluginPortal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/cmgapps/licenses/com.cmgapps.licenses.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal&style=for-the-badge)](https://plugins.gradle.org/plugin/com.cmgapps.licenses)

This Gradle plugin provides tasks to generate a file with the licenses used from the project's dependencies.

## Usage

Using the plugins DSL

```groovy
plugins {
  id "com.cmgapps.licenses" version "<version>"
}
```

Using legacy plugin application 
```groovy
buildscript {
    repositories {
        mavenCentral()
    }

  dependencies {
    classpath("com.cmgapps:gradle-licenses-plugin:<version>")
  }
}

apply(plugin: "com.cmgapps.licenses")
```
### Tasks

Applying the plugin will create tasks to generate the license report

For `"java"` and `"java-library"`
*  `licenseReport`

For `"com.android.application"`, `"com.android.library"`, `"com.android.feature"` and `"com.android.dynamic-feature"`
* `license<variant>Report`

### Configuration

#### Output Format

Example:
```groovy
licenses {
    reports {
        html.enabled.set(false) // html is enabled by default
        xml.enabled.set(true)
        xml.destination.set(file("$buildDir/reports/licenses.xml"))
    }
}
```

The plugin can output different formats.

* `HTML`
    generates a formatted HTML website
    * Styling
    
       For a HTML report you can define custom stylesheet using a [TextResource]:
       ```groovy
        licenses {
            reports {
                html.stylesheet = resources.text.fromString("body {background: #FAFAFA}")
            }     
        }
        ```
* `JSON`
    generates a Json file
* `XML`
    generates a valid XML version 1.0 file
* `Text`
    generates a plain text report file
* `Mardown`
    generates a Markdown file
* `Custom`
    add your own reporter as a lambda function
    ```groovy
    licenses {
      custom.enabled.set(true)
      custom.generate = { list -> list.collect { it.name }.join(', ') }
    }
    ```

#### Multi-project Builds

If you have a multi-project build you can add projects you want to collect license information
from in the main project.

```groovy
licenses {
    additionalProjects ':module2', ':module3'
}
```

## License

```text
Copyright (c) 2018. Christian Grach <christian.grach@cmgapps.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[TextResource]: https://docs.gradle.org/current/dsl/org.gradle.api.resources.TextResource.html
