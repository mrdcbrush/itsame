# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.1] - 2026-10-12

### Changed
- Updated for Minecraft 26.3 (Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, ModMenu 21.0.0).
- Adapted the keybind registration to the renamed `InputConstants.Type.KEYBOARD`.

## [1.0.0] - 2026-07-26

Initial release.

### Added
- Detection of vanilla join/leave messages via translation key (with a plain-text fallback for robustness).
- Animated banner HUD element showing the player's avatar and "has joined"/"has left", drawn via Fabric API's `HudElementRegistry`.
- Real-time fade-in/hold/fade-out timing, independent per notification, decoupled from tick rate.
- Multiple simultaneous notifications stack and animate out of the way as new ones arrive.
- 9-point anchor positioning (clockwise cycling) with pixel offset, resolved against GUI-scaled coordinates so it's correct at any resolution/GUI Scale.
- Configurable banner size (5%-20% of screen height, default 10%).
- Configurable background color (curated preset palette) and opacity, including fully transparent.
- Per-server whitelist (multiplayer keyed by server address, singleplayer/LAN as its own scope) with a dedicated management screen: searchable live list of currently online players to add from, manual add/remove.
- In-game settings screen with a live preview, reachable via the `/itsame` command, an optional (unbound by default) keybind, and an optional [ModMenu](https://modrinth.com/mod/modmenu) integration.
- **Test** button in settings to preview the banner using your own name/skin without needing a real join/leave.
