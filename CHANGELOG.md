# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /


## [1.4.0] - 2021-12-08

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
[#6]: https://github.com/AlmostReliable/minecraft_energymeter/issues/6


## [1.3.0] - 2021-10-14

### Added
- added the ability to define a custom update interval ([Enigmatica6/#3415])
  - this will change how often the transfer rate is recalculated
  - higher amounts will even out spikes but result in less updates of the value
- added German translation

### Changed
- improved English localization
- improved rendering performance of tooltips

<!-- Links -->
[Enigmatica6/#3415]: https://github.com/NillerMedDild/Enigmatica6/issues/3415


## [1.2.1] - 2021-10-10

### Changed
- slightly decreased the amount of time which is required to break the block

### Fixed
- fixed item not dropping after breaking the block ([#4], [#5])

<!-- Links -->
[#4]: https://github.com/AlmostReliable/minecraft_energymeter/issues/4
[#5]: https://github.com/AlmostReliable/minecraft_energymeter/pull/5


## [1.2.0] - 2021-10-10

### Info
- this update will reset your Energy Meter I/O configurations
- if you experience funky behavior of existing Meters, replace them

### Changed
- overhauled registration logic
- moved SyncFlags to Constants
- improved performance when quickly changing I/O configuration

### Fixed
- fixed some dedicated server synchronization issues
- fixed a bug where I/O configuration would be flipped when the Meter is facing up or down


## [1.1.1] - 2021-10-06

### Fixed
- fixed a crash when running the mod on a dedicated server ([#3])

<!-- Links -->
[#3]: https://github.com/AlmostReliable/minecraft_energymeter/issues/3


## [1.1.0] - 2021-10-05

### Info
- this update will either reset or completely remove all your existing Energy Meters
- this wasn't preventable in order to implement the new changes, sorry

### Added
- added full support for all directions for the facing side
  - this means you can also place Energy Meters with the screen at the top or the bottom

### Changed
- simplified the logic of the side configuration


## [1.0.4] - 2021-10-04

### Changed
- increased performance a lot
- increased the render distance of the Meter screen to 30 blocks (20 previous)
- drastically decreased the network traffic from the mod
- switched to client packet syncing
- ensured packets are only sent to the right logical side
- ensured that the facing side of the block is properly stored in the side configuration
- code cleanup

### Fixed
- fixed none-game-breaking array out of bounds exception on world load

### Removed
- removed unnecessary exception throws


## [1.0.3] - 2021-09-30

### Changed
- ensured that Energy Meters are properly setup on world load before transferring energy

### Fixed
- fixed that outputs which can't accept any energy are counted as valid


## [1.0.2] - 2021-09-30

### Changed
- refactored energy transfer logic
- improved energy transfer performance a lot
- improved performance when block is configured but no valid outputs are found
- greatly reduced simulation calls for energy connections


## [1.0.1] - 2021-09-30

### Changed
- improved energy transfer logic to cover edge cases
- improved the performance for some rare cases


## [1.0.0] - 2021-09-29
- initial release

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.4.0...HEAD
[1.4.0]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.3.0..v1.16-1.4.0
[1.3.0]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.2.1..v1.16-1.3.0
[1.2.1]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.2.0..v1.16-1.2.1
[1.2.0]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.1.1..v1.16-1.2.0
[1.1.1]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.1.0..v1.16-1.1.1
[1.1.0]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.0.4..v1.16-1.1.0
[1.0.4]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.0.3..v1.16-1.0.4
[1.0.3]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.0.2..v1.16-1.0.3
[1.0.2]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.0.1..v1.16-1.0.2
[1.0.1]: https://github.com/AlmostReliable/minecraft_energymeter/compare/v1.16-1.0.0..v1.16-1.0.1
[1.0.0]: https://github.com/AlmostReliable/minecraft_energymeter/releases/tag/v1.16-1.0.0