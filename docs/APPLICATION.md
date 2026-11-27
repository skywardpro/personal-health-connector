# Huawei Health Service Kit application

Facts submitted on the HUAWEI Developers console (individual developer).

| Field | Value |
| --- | --- |
| Product name | Personal Health Connector |
| HUAWEI Developers app ID | `120011809` (not secret) |
| Product type | Mobile app, product: new |
| Package name | `app.personalhealthconnector` |
| Certificate SHA-256 (1) | `BC:E1:94:ED:96:F5:A5:47:2D:35:70:7F:BD:D3:43:4C:2D:93:0D:E7:00:A0:1D:CF:72:55:E7:4A:F3:BF:DD:65` |
| Callback URL | empty, or placeholder `https://127.0.0.1/callback` if the form requires one (unused) |
| First scope | sleep read, `https://www.huawei.com/healthkit/sleep.read`; further basic-level scopes added later via Manage |

The package name and the signing certificate cannot change without a new application. The signing key is kept outside this repository, with an offline copy; losing it means a new application.

## Description (English, as submitted)
Open-source Android application that lets a user export their own raw health data from Huawei Health (read-only) to a file in the user's own cloud drive, for personal use and for the user's own tools, bots and analysis apps. The developer does not collect, store or receive any user data. Each user accesses only their own data.

## Huawei rules that shape the plan
- Individual developers: app must be released on AppGallery; only basic-level data; no adverse credit record.
- Manual review about 10 to 15 workdays; test scopes limited to 100 users; verification within 6 to 12 months or scopes are revoked.
- Least privilege: request only the scopes the app really uses.

## Credentials
The app ID above is public. The app SECRET is a credential and is NOT stored in this repository: it lives next to the signing key in `D:\Projects\PersonalHealthConnector-signing\huawei-app-credentials.txt` (outside the repo), and the Android app must read it from a local, git-ignored config, never from source.

## Google Drive (app side)
Google OAuth Android client ID (public, not a secret): `802062018625-63iq41488a0a5mkcbhnarmt28fpllgk8.apps.googleusercontent.com`; package `app.personalhealthconnector`, release certificate SHA-1 `A3:3B:06:F1:39:6C:75:E8:4E:C2:DE:B7:64:C0:C9:E1:02:E2:ED:AE`. Scope: `drive.file` only. It is a build config field (`GOOGLE_CLIENT_ID`).

## App status
v0.1.0 builds and runs (screens in `docs/screenshots`). Real sleep reads need Huawei's scope approval; until then the app says "not granted yet". Huawei sign-in on a real phone may need an `agconnect-services.json` from AppGallery Connect (not present; the build does not require it).
