# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /


## [2.3.0] - 2021-12-15

### Notes
- this will most likely be the EOL version for this Minecraft version
- 1.16 is LTS and 1.18 will be the main version for the mod

### Added
- accuracy mode ([#6])
  - you can now switch between two modes
  - exact: will work as the previous version and refresh the rate every 5 seconds
  - interval: will respect your custom interval and calculate the average within that interval

### Changed
- completely overhauled the GUI
- made the exact mode more precise
- slightly improved performance
- refactored a lot of code
- improved localization

### Fixed
- fixed that zero flow rate is not taken into consideration when measuring with interval ([#6])

<!-- Links -->
[#6]: https://github.com/AlmostReliable/energymeter-forge/issues/6


## [2.2.0] - 2021-10-22

### Added
- added the ability to define a custom update interval ([Enigmatica6/#3415])
  - this will change how often the transfer rate is recalculated
  - higher amounts will even out spikes but result in less updates of the value
  - ported from 1.16 version
- added German translation

### Changed
- improved English localization
- improved rendering performance of tooltips

<!-- Links -->
[Enigmatica6/#3415]: https://github.com/NillerMedDild/Enigmatica6/issues/3415


## [2.1.0] - 2021-10-10

### Info
- this update will either reset or completely remove all your existing Energy Meters
- this wasn't preventable in order to implement the new changes, sorry

### Added
- added full support for all directions for the facing side
  - this means you can also place Energy Meters with the screen at the top or the bottom

### Changed
- increased performance a lot
- increased the render distance of the Meter screen to 30 blocks (20 previous)
- drastically decreased the network traffic from the mod
- switched to client packet syncing
- ensured packets are only sent to the right logical side
- ensured that the facing side of the block is properly stored in the side configuration
- simplified the logic of the side configuration
- overhauled registration logic
- improved performance when quickly changing I/O configuration
- code cleanup

### Fixed
- fixed none game breaking array out of bounds exception on world load
- fixed some dedicated server synchronization issues

### Removed
- removed unnecessary exception throws


## [2.0.0] - 2021-10-02
- initial release


<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/energymeter-forge/compare/v1.17-2.3.0...HEAD
[2.3.0]: https://github.com/AlmostReliable/energymeter-forge/compare/v1.17-2.2.0..v1.17-2.3.0
[2.2.0]: https://github.com/AlmostReliable/energymeter-forge/compare/v1.17-2.1.0..v1.17-2.2.0
[2.1.0]: https://github.com/AlmostReliable/energymeter-forge/compare/v1.17-2.0.0..v1.17-2.1.0
[2.0.0]: https://github.com/AlmostReliable/energymeter-forge/releases/tag/v1.17-2.0.0