# NyxPad

**A private, offline home for your words.**

NyxPad is a free and open-source notes app for Android, built with Kotlin and Jetpack Compose. It is designed for writing without limits: no accounts, no internet permission, no analytics, no ads and no cloud. Every note stays in the app's private storage on your device.

[![Android CI](https://github.com/xspeen/nyxpad-android/actions/workflows/android.yml/badge.svg)](https://github.com/xspeen/nyxpad-android/actions/workflows/android.yml)

## Download

Install the latest APK from the [Releases page](https://github.com/xspeen/nyxpad-android/releases/latest). Every release is built by GitHub Actions directly from the tagged source.

You can also download the **nyxpad-apk** artifact from the latest successful **Android CI** run in the **Actions** tab.

NyxPad runs on Android 8.0 (API 26) and newer. Release APKs are signed with the project's CI key so later versions install over earlier ones without uninstalling. Android may ask you to allow installation from your browser or file manager the first time.

## Features

- **Private by design** — no internet permission, accounts, tracking, Firebase, ads or cloud backup. Android backup is disabled.
- **A complete writing environment** — distraction-free editor, focus mode, auto-save, word and character counts, reading time and writing moods.
- **Make it yours** — Lumina Dark, Pure AMOLED and Midnight Blue themes; eight accent colours; Inter, JetBrains Mono and system fonts; adjustable editor size and line spacing.
- **Stay organised** — pin, archive, search, sort, colour-tag and duplicate notes; grid and list layouts; pull to refresh.
- **Templates** — built-in prompts plus your own reusable templates.
- **Writing stats** — streaks, a 90-day calendar, weekly rhythm and six-month history.
- **Trash with auto-purge** — restore notes or let WorkManager clean them up after a chosen period.
- **Locks** — optional app lock and per-note locks using your device's biometric or PIN prompt. NyxPad never stores biometric data.
- **Export and share** — share individual notes or export every note as one Markdown file through the Android share sheet.
- **Home-screen widget** — one tap opens a fresh note.
- **No placeholders** — every screen is implemented, from onboarding and the navigation drawer to About and settings.

## Screens

Onboarding · Home · Pinned · Editor · Settings · Trash · Archive · Writing Stats · About

## Tech stack

| Layer | Technology |
| --- | --- |
| Language | Kotlin 1.9.24 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM + Clean Architecture |
| Persistence | Room 2.6.1 |
| Settings | DataStore Preferences |
| Dependency injection | Hilt 2.51.1 |
| Background work | WorkManager 2.9.0 |
| Build | Gradle 8.9, AGP 8.7.3, JDK 17 |

- Package: `com.nyxpad.app`
- Min SDK: 26 (Android 8.0)
- Target / compile SDK: 35

## Build from source

You need JDK 17 and the Android SDK with platform 35 installed.

```bash
git clone https://github.com/xspeen/nyxpad-android.git
cd nyxpad-android
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions runs the same build on every push and pull request, and uploads the APK as an artifact.

## Project structure

```
app/src/main/java/com/nyxpad/app/
├── data/          Room entities, DAOs, DataStore and repository implementation
├── di/            Hilt modules
├── domain/        Models and repository interfaces
├── ui/            Theme, navigation, components and screens
├── utils/         Biometrics, export, dates, word counts and preferences keys
├── widget/        Home-screen quick capture widget
└── worker/        Trash auto-purge worker
```

## Privacy

NyxPad does not request the `INTERNET` permission. There are no servers to talk to, no accounts to create and no telemetry to send. Notes, templates, settings and streak data live in the app's private storage and are removed when you uninstall the app. See [PRIVACY.md](PRIVACY.md) for the full promise.

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request. By participating you agree to keep NyxPad offline, private and free of trackers.

## License

App source code is released under the [MIT License](LICENSE).

Bundled fonts:

- **Inter** — Copyright 2020 The Inter Project Authors, SIL Open Font License 1.1. See `licenses/Inter-OFL.txt`.
- **JetBrains Mono** — Copyright 2020 The JetBrains Mono Project Authors, SIL Open Font License 1.1. See `licenses/JetBrainsMono-OFL.txt`.

NyxPad is not affiliated with Google, JetBrains or the Inter project.
