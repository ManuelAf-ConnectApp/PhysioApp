# Security Policy

PhysioApp stores **health data** (a special category under the GDPR) on the user's device. We take security reports seriously and appreciate responsible disclosure.

## Supported versions

PhysioApp is **pre-alpha** and has not been released yet. Security fixes are applied to the `main` branch only.

| Version | Supported |
|---|---|
| `main` (unreleased) | ✅ |
| Any other | ❌ |

> **Do not use the current version with real patient data.** The app is still under development and has not passed a security audit.

## Reporting a vulnerability

**Please do not report vulnerabilities through public issues, pull requests or discussions.**

Report them privately to <incidencias@connectapp.es>, or through GitHub's private reporting: [*Security → Report a vulnerability*](https://github.com/ManuelAf-ConnectApp/PhysioApp/security/advisories/new).

Please include:

- A description of the issue and its potential impact.
- The affected platform (Android / iPadOS), OS version and app version or commit.
- Steps to reproduce, or a proof of concept.
- Any suggested fix or mitigation.

**Never include real personal or clinical data** in your report. Use synthetic data.

### What to expect

These are target times while the project is maintained by a small team, not contractual commitments:

- Acknowledgement within **5 business days**.
- An initial assessment and severity estimate within **15 business days**.
- Coordinated disclosure: we will agree on a publication date with you once a fix is available. We ask you not to disclose the issue before then.

We are happy to credit reporters in the [CHANGELOG](CHANGELOG.md) unless you prefer to remain anonymous.

## Scope

In scope:

- The PhysioApp source code in this repository (Android and iPadOS apps and shared Kotlin Multiplatform modules).
- Local authentication: password hashing, recovery codes, login lockout, session handling and the inactivity lock.
- Local data protection: database encryption, key storage in Keystore/Keychain, backup exclusion, the encrypted backup files and their restore, data leaking through logs, exported files or OS features.
- Any network traffic or data leaving the device (the app is designed to have none).

Out of scope:

- Attacks that require a device that is already rooted/jailbroken or fully compromised.
- Vulnerabilities in third-party dependencies that are already publicly known and have no PhysioApp-specific impact (report them upstream; tell us if we ship an affected version).
- Physical access to an unlocked device with an active session before the inactivity lock triggers.
- Social engineering and denial of service.

## Security design summary

See the [Security section of the README](README.md#security) for current status. Main measures:

- **No network:** no HTTP client, analytics or telemetry; the Android app does not request the `INTERNET` permission.
- **Encrypted database:** SQLCipher 4.17.0 with a random 256-bit passphrase stored in Android Keystore / iOS Keychain (`WhenUnlockedThisDeviceOnly`). The app refuses to open the database if the runtime is not SQLCipher.
- **Passwords:** PBKDF2-HMAC-SHA256 with 600,000 iterations, a 16-byte random salt per password, Unicode NFC normalization and constant-time comparison. Minimum 12 characters with uppercase, lowercase, digit and symbol.
- **Account recovery:** a one-time 100-bit recovery code shown once at registration, stored only as a hash. It can be regenerated from Settings after entering the password; the old code then stops working.
- **Password change:** from Settings, requires the current password; wrong attempts count towards the sign-in lockout and reaching it ends the session.
- **Brute force and enumeration:** login lockout after repeated failures, and the same error message and cost for unknown emails and wrong passwords.
- **Session:** inactivity lock after 5 minutes, also across restarts (decision D14).
- **Backups:** Android OS backups and device-to-device transfer are disabled; on iOS the database folder is excluded from backups and protected while the device is locked (`NSFileProtectionCompleteUnlessOpen`), and the database key never leaves the device. App-level backups are manual: a SQLCipher file encrypted with its own password (never the device key, not stored anywhere), restored in one transaction after the backup password, the sign-in password and a confirmation; from the sign-in screen only on a tablet without accounts. The backup file is as sensitive as the tablet itself.
- **Signatures and PDFs:** consent signatures are PNGs inside the encrypted database, shown only in the patient record. Exported PDFs are written to the app's private temporary folder, handed to the system share or "save to" sheet after a warning, and deleted afterwards.
- **Logs:** the app writes no logs; models and screen states holding personal data, passwords or codes hide them in `toString()`.
- **Screenshots and app switcher:** Android sets `FLAG_SECURE` (no screenshots or recordings, blank recent-apps thumbnail); the iPad shows a neutral cover while the app is not active and while the screen is recorded or mirrored. iOS cannot block screenshots.

Known gaps: stopping the keyboard from learning words typed in clinical texts (not exposed by Compose).

## Responsibility of the data controller

Storing data locally does not remove the legal obligations of the clinic or professional using PhysioApp (legal basis, retention of clinical records, device security, backups). PhysioApp does not replace a legal or data-protection review.
