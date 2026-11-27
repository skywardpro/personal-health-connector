# Personal Health Connector

An open-source Android app that exports **your own** raw health data from Huawei Health (read-only) to a file in **your own** cloud drive, so you can use it in your own tools, bots, AI agents and analysis apps.

Status: planning. The app is not written yet; the Huawei Health Service Kit application is being prepared (see [docs/APPLICATION.md](docs/APPLICATION.md)).

## What it does
- Reads health data (sleep first; more types as Huawei grants them) through Huawei's official Health Service Kit on the phone.
- Writes raw JSON, one file per day, into a folder in the user's own Google Drive.
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
