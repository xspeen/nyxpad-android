# Privacy

NyxPad is built so that your writing never leaves your device.

## What NyxPad collects

**Nothing.** NyxPad has no analytics, crash reporting, advertising, telemetry or accounts.

## What NyxPad can access

NyxPad requests only these Android permissions:

- `USE_BIOMETRIC` and `USE_FINGERPRINT` — to show the system biometric prompt when you enable app lock or a note lock.
- `VIBRATE` — for small haptic feedback.
- `CAMERA` — only while you are scanning a NyxPad QR code. The camera is never used in the background.
- `INTERNET` and `ACCESS_NETWORK_STATE` — only for opt-in NyxDrop sharing. NyxDrop is off by default and serves a note to a browser on your own local network.

NyxPad does **not** request location, contacts, microphone, storage or notification access. Voice input is handled by your device's speech-recogniser app through an Android intent; NyxPad itself does not record audio.

## Where your data lives

- Notes, templates and note metadata are stored in a private Room database inside the app's sandbox.
- Settings, streak history and onboarding state are stored in a private DataStore file inside the app's sandbox.
- A custom background photo is copied into the app's private files directory.
- Android cloud backup is disabled (`android:allowBackup="false"`).
- Exported Markdown and JSON backups are written through Android's Storage Access Framework, to the location you choose.
- NyxDrop serves a note from memory over a temporary local HTTP server. Nothing is written to a server or uploaded to the internet.

Uninstalling NyxPad removes its private storage, including all notes and settings.

## Biometrics

When you use app lock or a note lock, Android shows its own biometric or device-PIN prompt. NyxPad receives only a success or failure result. It never sees, stores or transmits your fingerprint, face data or PIN.

## Sharing and exporting

Sharing a note, exporting Markdown, creating a QR code or starting NyxDrop is always started by you.

- The Android share sheet sends content only to the app you select.
- A QR code is drawn on your screen and contains the note text; anyone who scans it can read the note.
- NyxDrop starts a temporary web server bound to your local network. The address includes a random token, and sharing stops when you close the dialog or after ten minutes. It is off by default and never contacts a cloud service.

NyxPad does not upload your notes anywhere.

## Children

NyxPad does not knowingly collect personal information from anyone, including children.

## Changes

If this policy ever changes, the new version will be published in this repository. NyxPad has no analytics or telemetry, and online features remain off by default.

## Contact

Open an issue on the repository for general privacy questions. For a sensitive report, contact the maintainer privately through their GitHub profile.
