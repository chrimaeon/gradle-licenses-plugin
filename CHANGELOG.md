# Changelog

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [5.1.0]

### Added

- Add [ISC License](https://opensource.org/license/isc-license-txt)

## [5.0.0]

Use Gradle's Reports API to generate reports

### Changed

- `text` report renamed to `plainText`
- `destination` is now `outputLocation`
- Extension values are now `org.gradle.api.provider.Property`

## [4.8.0]

### Changed

- Minimum Gradle Version is now **7.2**

### Deprecated

- Android Gradle Plugin **4.x**

## [4.7.0]

### Added

- EPL v1.0 License - #15
- Additional URL for LGPL-2.1

## [4.6.1]

### Changed

- update AGP version
- update maven model library

## [4.6.0]

### Added

- Dark Mode for HTML report

### Changed

- Sort libraries by name or maven coordinated on HTML report

## [4.5.0]

### Added

- Better support for Android Variants

### Changed

- Internal handling of the license mappings

## [4.4.0]

### Added

- [SPDX License Identifier](https://spdx.org/licenses/) for various reports

### Changed

- HTML and Markdown reports merge licenses with a more sophisticated algorithm

### Fixed

- CSV Reporter reports all licenses not only the first one

## [4.3.0]

### Changed

- add maven coordinates to Library model
- `version` is not part of the `mavenCoordinates` in the Library model
- Improved Markdown reporter

## [4.2.0]

### Added

- XSD Schema for the XML reporter

### Changed

- 
    - `<version>` is now attribute to `<library>`
    - `<url>` is now a attribute to `<license>`
    - `<library>` has an `id` attribute now

### Fixed

- HTML Reports includes all libraries again

## [4.1.0]

### Changed

- Sort dependencies by name and version

## [4.0.0]

### Added

- Kotlin Multiplatform support

### Changed

- Extension property to set `enabled` and `destination`

## [3.3.0]

### Changed

- Update Android Gradle plugin to version 7+
- Warning when license has no mapping for html reports

## [3.2.0]

### Added

- set task outputs to report files
- add DSL to configure reports

### Changed

- use library to create CSV report

## [3.1.0]

### Changed

- All public properties are now provided Properties
- Use Kotlin Serialization instead of Moshi

[Unreleased]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/5.1.0...HEAD
[5.1.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/5.0.0...5.1.0
[5.0.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.8.0...5.0.0
[4.8.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.7.0...4.8.0
[4.7.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.6.1...4.7.0
[4.6.1]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.6.0...4.6.1
[4.6.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.5.0...4.6.0
[4.5.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.4.0...4.5.0
[4.4.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.3.0...4.4.0
[4.3.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.2.0...4.3.0
[4.2.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.1.0...4.2.0
[4.1.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/4.0.0...4.1.0
[4.0.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/3.3.0...4.0.0
[3.3.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/3.2.0...3.3.0
[3.2.0]: https://github.com/chrimaeon/gradle-licenses-plugin/compare/3.1.0...3.2.0
[3.1.0]: https://github.com/chrimaeon/gradle-licenses-plugin/commits/3.1.0
