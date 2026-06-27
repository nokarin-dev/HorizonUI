# HorizonUI Changelog

All notable changes to HorizonUI will be documented here.

## Legend

- CRITICAL - Security/Crash fixes
- HIGH - Major bugs, important fixes
- MEDIUM - Important features/fixes
- LOW - Minor fixes, cosmetic changes

## Summary of Changes

### Stable Releases

### Beta Releases

- **v1.0.0-beta.8 - 2026-06-28** - [ HIGH ] Improve background rendering, image decode, gif load time, added creator to extension tooltip, supported static background without number namespace, rounding all buttons.
- **v1.0.0-beta.6 - 2026-06-14** - [ CRITICAL ] Fixed memory leak on background system, Fixed discord rpc not working properly, HorizonUI now support gif background, Switching fps now realtime.
- **v1.0.0.beta-5 - 2026-03-15** - [ HIGH ] Introduces new HorizonUI configuration UI. Removed OptionsScreen. Integrated updater with an integrity verification system. Fixed main menu fallback to default ui. etc.
- **v1.0.0-beta.4 - 2026-02-16** - [ MEDIUM ] Integration ReplayMod and Sodium Mods. Bug fix. Improved options screen GUI.
- **v1.0.0-beta.3 - 2026-01-27** - [ CRITICAL HOTFIX ] Fix title screen crash.
- **v1.0.0-beta.2 - 2026-01-26** - [ MEDIUM ] Fix lag issue and background still render on pause screen. Optimize background rendering. Introduced new Options Screen UI [EXPERIIMENTAL].
- **v1.0.0-beta.1 - 2026-01-19** - [ LOW ] UI improvements and visual fixes, including layout refinements, banner rendering fixes, and corrected visibility behavior in Clean Mode.
- **v1.0.0-beta - 2026-01-18** - Initial beta release. Fixed Forge mixin-related crash. Implemented ImageSTB rendering. Introduced new title screen UI. Added auto-update toggle. Added update notification system.

## Stable Release Versions

## Beta Release Versions

## HorizonUI 1.0.0-BETA.8 - 2026-06-28

### Added

- Now tooltip extension will show creator.
- Now static extension supported without number namespace (background1.png > background.png).

### Changed

- Rounding all button background on title screen.
- Improve background rendering performance.
- Improve image decode performance.
- GIF now load faster

## HorizonUI 1.0.0-BETA.6 - 2026-06-14

### Added

- HorizonUI now support gif background.

### Fixed

- Discord Rich Presence not working properly.
- Memory leak on background system.

### Changed

- Switching fps (frame per second) now real time

## HorizonUI 1.0.0-BETA.5 - 2026-03-15

### UI / Layout

- Removed custom OptionsScreen.
- Introducing new HorizonUI mods config UI.
- Fixed main menu fallback to default ui.

### Updater

- Added updater to HorizonUI.
- Added integrity check to updater before update HorizonUI to prevent vulnerability.

## HorizonUI 1.0.0-BETA.4 - 2026-03-00

### Integration

- Added Replay Viewer button from ReplayMod.
- Added button to open Sodium Screen.

### UI / Layout

- Fixed Auto scroll bug.
- Fixed Button position corruption.
- Fixed category switching.
- Fixed widget event handling.

### UI / Layout

- Improved options screen UI.

## HorizonUI 1.0.0-BETA.3 - 2026-01-27 | CRITICAL HOTFIX

### Mixins

- Fixed game crash on title screen caused by improper screen initialization.

## HorizonUI 1.0.0-BETA.2 - 2026-01-26 | MEDIUM

### Background

- Fix lag issue when open another screen.
- Fix background still render on pause screen.
- Optimize background render for better performance.

### UI / Layout

- Override all minecraft options screen [EXPERIMENTAL].

## HorizonUI 1.0.0-BETA.1 - 2026-01-19 | LOW

### UI / Layout

- Revamped agreement screen UI.
- Improved overall UI layout structure.
- Fixed banner being overlapped by gradient background.
- Disabled beta notice interaction.
- Fixed HorizonUI banner visibility issue.
- Fixed beta notice and feedback button visibility in Clean Mode.

### Extension

- Added extension detection system.

## HorizonUI 1.0.0-BETA - 2026-01-18 | Initial Release

### UI / Layout

- New title screen UI

### Config

- Auto-update toggle in settings

### Updater

- Update notification system

### Background

- ImageSTB-based image rendering implementation

### Mixins

- Crash caused by Forge mixin conflicts