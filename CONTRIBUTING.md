# Contributing to NyxPad

Thanks for wanting to make NyxPad better. The project is small, so a few clear rules keep it healthy.

## The non-negotiables

NyxPad is built around a privacy promise. Pull requests must not:

- add the `INTERNET` permission or any network dependency;
- add accounts, analytics, crash reporting, advertising or tracking SDKs;
- enable Android cloud backup or add cloud sync;
- send note content, settings or device identifiers anywhere;
- introduce hardcoded UI colours outside `ui/theme/Color.kt`.

If a change needs any of the above to work, it does not belong in NyxPad.

## Before you open a pull request

1. Search existing issues and pull requests.
2. Keep the change focused. One feature or fix per pull request.
3. Follow the existing package structure and naming.
4. Keep all user-visible strings in the app or clearly documented in the source.
5. Test on an Android 8.0 (API 26) emulator or device when possible.
6. Make sure `./gradlew assembleDebug` passes.

## Code style

- Kotlin, four-space indentation, trailing commas where the project uses them.
- MVVM + Clean Architecture: UI talks to view models; view models talk to repository interfaces; Room and DataStore stay in `data/`.
- Prefer immutable state and `StateFlow` for screen state.
- Use the existing Compose components and `LocalNyxPalette` / `LocalNyxAccent` rather than introducing parallel styling.
- Add comments only where the reason is not obvious from the code.

## Commits and pull requests

- Write short, imperative commit messages (`Add archive screen`, `Fix editor autosave`).
- Describe what changed and why in the pull request.
- Include screenshots or a short screen recording for visible UI changes.
- Link the issue the pull request closes, if there is one.

## Licensing

By contributing, you agree that your contribution is licensed under the MIT License, the same licence as the rest of the app. Bundled fonts remain under the SIL Open Font License 1.1.

## Reporting security or privacy issues

Please do not open a public issue for a security or privacy problem. Contact the maintainer privately through the GitHub profile linked on the repository.
