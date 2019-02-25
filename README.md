# Gradle Licenses Plugin

[![License](https://img.shields.io/badge/license-Apache%202.0-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Bintray](https://img.shields.io/bintray/v/chrimaeon/maven/com.cmgapps:gradle-licenses-plugin.svg)](https://bintray.com/chrimaeon/maven/com.cmgapps:gradle-licenses-plugin)

This Gradle plugin provides tasks to generate a Html/Xml/Json file with the licenses used from the libraries.

## Usage

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath("com.cmgapps:gradle-licenses-plugin:<version>")
  }
}

apply(plugin: "com.android.application")
apply(plugin: "com.cmgapps.licenses")
```
### Tasks

Applying the plugin will create tasks to generate the license report

For `"java"` and `"java-library`
*  `licenseReport`

For `"com.android.application"`, `"com.android.library"` and `"com.android.feature"`
* `licenar<variant>Report`

### Configuration

The plugin can output different formats.

* `OutputType.HTML`
    generates a formated HTML website
* `OutputType.JSON`
    generates a Json file
* `OutputType.XML`
    generates a valid XML version 1.0 file

```groovy
apply(plugin: "com.cmgapps.licenses")

licenses {
    outputType = OutputType.HTML
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

