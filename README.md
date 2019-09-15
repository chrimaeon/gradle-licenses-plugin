# Gradle Licenses Plugin

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg?style=for-the-badge)](http://www.apache.org/licenses/LICENSE-2.0)
[![Bintray](https://www.cmgapps.com/badge/chrimaeon/maven/com.cmgapps:gradle-licenses-plugin/badge.svg)](https://bintray.com/chrimaeon/maven/com.cmgapps:gradle-licenses-plugin)
[![gradlePluginPortal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/cmgapps/licenses/com.cmgapps.licenses.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal&style=for-the-badge)](https://plugins.gradle.org/plugin/com.cmgapps.licenses)

This Gradle plugin provides tasks to generate a HTML / XML / Json file with the licenses used from the libraries.

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
    jcenter()
  }

  dependencies {
    classpath("com.cmgapps:gradle-licenses-plugin:<version>")
  }
}

apply(plugin: "com.cmgapps.licenses")
```
### Tasks

Applying the plugin will create tasks to generate the license report

For `"java"` and `"java-library`
*  `licenseReport`

For `"com.android.application"`, `"com.android.library"` and `"com.android.feature"`
* `license<variant>Report`

### Configuration

#### Output Format

The plugin can output different formats.

* `OutputType.HTML`
    generates a formatted HTML website
* `OutputType.JSON`
    generates a Json file
* `OutputType.XML`
    generates a valid XML version 1.0 file
* `OutputType.TEXT`
    generates a plain text report file
* `OutputType.MD`
    generates a Markdown file

```groovy
import com.cmgapps.license.OutputType

licenses {
    outputType = OutputType.HTML
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

### CSS Styles for HTML Report

For a HTML report you can define custom `body`and `pre` styles using:

```groovy
licenses {
    bodyCss = "body {font-family: sans-serif; background-color: #eee}"
    preCss = "pre, .license {background-color: #ddd; padding:1em} pre {white-space: pre-wrap}"
}
```

additionally to the `pre` tag, you'll have to provide a styling for 
the `.license` class which is applied to URL licenses.

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

