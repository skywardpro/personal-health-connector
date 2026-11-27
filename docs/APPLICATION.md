# Huawei Health Service Kit application

Facts submitted on the HUAWEI Developers console (individual developer).

| Field | Value |
| --- | --- |
| Product name | Personal Health Connector |
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
