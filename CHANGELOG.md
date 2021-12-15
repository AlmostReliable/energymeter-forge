# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /


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
- fixed none-gamebreaking array out of bounds exception on world load
- fixed some dedicated server synchronization issues

### Removed
- removed unnecessary exception throws


## [2.0.0] - 2021-10-02
- initial release

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/energymeter/compare/v1.17-2.2.0...HEAD
[2.2.0]: https://github.com/AlmostReliable/energymeter/compare/v1.17-2.1.0..v1.17-2.2.0
[2.1.0]: https://github.com/AlmostReliable/energymeter/compare/v1.17-2.0.0..v1.17-2.1.0
[2.0.0]: https://github.com/AlmostReliable/energymeter/releases/tag/v1.17-2.0.0