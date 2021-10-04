# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /


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
- fixed none-gamebreaking array out of bounds exception on world load

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
[unreleased]: https://github.com/RLNT/minecraft_energymeter/compare/v1.16-1.0.4...HEAD
[1.0.4]: https://github.com/RLNT/minecraft_energymeter/compare/v1.16-1.0.3..v1.16-1.0.4
[1.0.3]: https://github.com/RLNT/minecraft_energymeter/compare/v1.16-1.0.2..v1.16-1.0.3
[1.0.2]: https://github.com/RLNT/minecraft_energymeter/compare/v1.16-1.0.1..v1.16-1.0.2
[1.0.1]: https://github.com/RLNT/minecraft_energymeter/compare/v1.16-1.0.0..v1.16-1.0.1
[1.0.0]: https://github.com/RLNT/minecraft_energymeter/releases/tag/v1.16-1.0.0
