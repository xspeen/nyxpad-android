# Privacy

NyxPad is built so that your writing never leaves your device.

## What NyxPad collects

**Nothing.** NyxPad has no analytics, crash reporting, advertising, telemetry or accounts.

## What NyxPad can access

NyxPad requests only these Android permissions:

- `USE_BIOMETRIC` and `USE_FINGERPRINT` — to show the system biometric prompt when you enable app lock or a note lock.
- `VIBRATE` — for small haptic feedback.

NyxPad does **not** request the `INTERNET` permission. It cannot open a network connection even if a dependency tried to. It also does not request location, contacts, camera, microphone, storage or notification access.

## Where your data lives

- Notes, templates and note metadata are stored in a private Room database inside the app's sandbox.
- Settings, streak history and onboarding state are stored in a private DataStore file inside the app's sandbox.
- Android cloud backup is disabled (`android:allowBackup="false"`).
- Exported Markdown files are written to the app's cache directory and shared only with the app you choose in the Android share sheet.

Uninstalling NyxPad removes its private storage, including all notes and settings.

## Biometrics

When you use app lock or a note lock, Android shows its own biometric or device-PIN prompt. NyxPad receives only a success or failure result. It never sees, stores or transmits your fingerprint, face data or PIN.

## Sharing and exporting

Sharing a note or exporting all notes is always started by you. The content goes to the app you select in the share sheet. NyxPad does not upload it anywhere.

## Children

NyxPad does not knowingly collect personal information from anyone, including children.

## Changes

If this policy ever changes, the new version will be published in this repository. The app's lack of the `INTERNET` permission is the technical guarantee behind the policy.

## Contact

Open an issue on the repository for general privacy questions. For a sensitive report, contact the maintainer privately through their GitHub profile.
