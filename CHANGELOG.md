# Changelog

## [Unreleased]
### Added
- Better support for Android Variants

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [4.4.0]
### Added
- [SPDX License Identifier](https://spdx.org/licenses/) for various reports

### Changed
- HTML and Markdown reports merge licenses with a more sophisticated algorithm

### Deprecated

### Removed

### Fixed
- CSV Reporter reports all licenses not only the first one

### Security

## [4.3.0]
### Changed
- add maven coordinates to Library model
- `version` is not part of the `mavenCoordinates` in the Library model
- Improved Markdown reporter

## [4.2.0]
### Added
- XSD Schema for the XML reporter

### Changed
- XML Schema changed
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
