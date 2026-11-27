# Personal Health Connector

An open-source Android app that exports **your own** raw health data from Huawei Health (read-only) to a file in **your own** cloud drive, so you can use it in your own tools, bots, AI agents and analysis apps.

Status: first working version (v0.1.0). The UI and flow run; reading real data waits for Huawei to approve the sleep scope (see [docs/APPLICATION.md](docs/APPLICATION.md)). Until then the app shows "not granted yet". Material 3 UI, teal theme with dynamic colour on Android 12+, light and dark. Screenshots: [docs/screenshots](docs/screenshots).

## Build
JDK 17 and the Android SDK (platform 34), then `./gradlew assembleDebug`. Debug builds are signed with the release key when a git-ignored `keystore.properties` (`storeFile`, `alias`, `password`) exists in the repo root, otherwise with the default debug key (Google sign-in then will not match). Huawei's Maven repo is declared in `settings.gradle.kts`; no `agconnect-services.json` is used so far.

## Dependencies
Compose (BOM, Material 3), activity-compose, WorkManager (schedule), DocumentFile (local folder), play-services-auth (Google authorization for the `drive.file` scope; Drive itself is plain REST), Huawei `com.huawei.hms:health` 6.16.0.300 (Health Service Kit).

## What it does
- Reads health data (sleep first; more types as Huawei grants them) through Huawei's official Health Service Kit on the phone.
- Writes raw JSON, one file per day, into the user's own Google Drive (folder "Personal Health Connector", `drive.file` scope) and/or a folder on the phone.
- Nothing else: no developer-side server, no analytics, no accounts.

## What it does not do
- It never writes to Huawei Health.
- It never sends data to the developer or to any third party.

## Privacy
See [PRIVACY.md](PRIVACY.md) and the [user agreement](TERMS.md). Published pages: https://skywardpro.github.io/personal-health-connector/PRIVACY and https://skywardpro.github.io/personal-health-connector/TERMS

## Not affiliated with Huawei
Huawei and Huawei Health are trademarks of their owners. This project is independent and only uses Huawei's public developer API.

## Security
Signing keys, keystores and tokens never belong in this repository (see `.gitignore`).

## License
MIT, see [LICENSE](LICENSE).
