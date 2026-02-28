# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

- fixed energy limit being applied per operation instead of per tick
- fixed energy limit not respecting multiple inputs
- improved energy splitting algorithm to distribute leftover energy

## [0.3.0] - 2026-02-27

- added new GUI tab showing a graphical representation of the last measured energy rates
- added proper client action registration system to avoid unhandled synced actions
- added fallback system to clear output caches when a neighbor block changes without invalidating its capability
- fixed cable connections with mods relying on block updates instead of capability invalidation when an I/O config is changed
- fixed cable connections with mods checking for non-directional energy storages
- fixed players entering or re-entering the chunk not getting the latest energy rates

## [0.2.0] - 2026-02-25

- added button to reset the total energy transferred
- added option to set custom interval length for exact/instant mode
- fixed output priority being zero when switching from split to transfer mode
- fixed I/O config widgets not being centered in their group box
- fixed meter configuration values not always being saved
- improved performance by drastically simplifying interval average calculation logic
- renamed measuring modes from "exact" and "interval" to "instant" and "smoothed"

## [0.1.0] - 2026-02-25

Initial 1.21.1 release! The mod is still subject to change and lacks some features, as well as explanations.

The new version is a complete rewrite of the 1.20.1 version.

<!-- Versions -->

[0.3.0]: https://github.com/AlmostReliable/energymeter/releases/tag/v1.21.1-neoforge-0.3.0
[0.2.0]: https://github.com/AlmostReliable/energymeter/releases/tag/v1.21.1-neoforge-0.2.0
[0.1.0]: https://github.com/AlmostReliable/energymeter/releases/tag/v1.21.1-neoforge-0.1.0
