# SIM Pilot

[日本語](README.md) · English

Manage default SIMs for data, calls, and messages separately on a dual-SIM device. Compare signal and connection quality to switch the data SIM automatically.

## Automatic startup after boot

Automatic startup after device boot can be switched on or off in settings. The default is on when no choice has been saved; an existing off choice is preserved. Startup only restores configured behavior and does not override feature switches, permissions, or lock-state restrictions. The switch is in Settings. Opening the app can still resume configured monitoring and Wi-Fi restoration.

## Features

- Manual switching and automatic switching when connectivity degrades.
- Separate data/call/message return preferences while connected to Wi-Fi.
- Per-SIM signal, cell, and connection-quality information.
- Data-plan management for monthly plans, custom periods, multiple top-ups, and manually entered remaining allowances.
- Suppress switching during calls, with consecutive checks and cooldowns.
- Stop monitoring while the screen is off; optional restoration after startup or updates.
- Material 3 **Home**, **Radio**, and **Settings** sections, with local diagnostic-log viewing/export.

## Requirements and setup

A supported DSDV device running Android 12 or later and Shizuku / Shizuku Plus are required. Earlier versions were primarily tested on a Galaxy S26 Ultra (SM-S948Q) with Android 16 / One UI 8.5. On other devices and OS versions, features are enabled only when a compatible switching method is detected.

1. Install the distributed APK from [Releases](https://github.com/OneUI-Mods/sim-pilot/releases).
2. Start Shizuku and authorize SIM Pilot.
3. Grant phone and location permissions; exclude the app from battery optimization if needed.
4. Review quality thresholds, Wi-Fi behavior, and data plans in **Settings**.
5. Enable **Automatically switch data SIM**.

## Data use and limitations

Quality measurements use network data. Automatic failover stops on Wi-Fi, and monitoring/network measurements stop while the screen is off. Switching or comparison may briefly interrupt connectivity. Switching during calls is suppressed, but uninterrupted operation of every communication app is not guaranteed.

OS data-usage accounting can lag. Consult the carrier for billing and exact remaining allowances; use manual balances when needed. Even with automatic restoration enabled, switching waits until Shizuku is running.

## Privacy and disabling

Diagnostic logs are stored locally. Phone numbers, ICCIDs, and IMEIs are not logged. Logs do contain carrier names, signal values, cell identifiers, traffic usage, and switching history; review them before sharing. Connectivity checks use Android's connectivity-check endpoints, and speed measurements use Cloudflare Speed Test.

Turn the automatic-switching option OFF to stop it. Restore the default data/call/message SIMs manually if needed.

## Development and license

Build instructions, architecture, and verification are in the [developer guide (Japanese)](docs/development.md). Report problems through [Issues](https://github.com/OneUI-Mods/sim-pilot/issues), including device, OS and target-app versions, and reproduction steps.

SIM Pilot is **All rights reserved**, as stated in [LICENSE](LICENSE). A public repository does not grant redistribution permission. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for third-party components.

[OneUI Mods app catalog](https://github.com/OneUI-Mods)

[Build environment / ビルド環境](docs/build-environment.md)
