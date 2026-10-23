# Changelog

All notable changes to PhysioApp are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project aims to follow [Semantic Versioning](https://semver.org/spec/v2.0.0.html). No version has been released yet; the build currently reports version `1.0`, inherited from the template.

Phase references point to the plan in [`docs/00-auditoria-y-plan.md`](docs/00-auditoria-y-plan.md) (in Spanish).

## [Unreleased]

### Added

- README: "deploying for a clinic" checklist (2026-10-21). As an open-source project, the consent text and the tax validity of receipts are adapted and agreed with each clinic by the developer who deploys it.
- **Encrypted backups** (2026-10-21, decision D20, plan §21.7). Settings → **Create backup** writes the whole database to a file encrypted with a password chosen at that moment (account password rules; not stored anywhere) and opens the system "save to" picker. **Restore backup** replaces all the data on the tablet in one transaction, after the backup password, the sign-in password and a confirmation, and then ends the session. On a tablet without accounts, the sign-in screen offers "New tablet? Restore a backup". Settings shows the date of the last backup and Home reminds you when there is none or it is 7 days old. New modules `:domain:backup`, `:data:backup` and `:feature:backup`; the file picker `DocumentPicker` in `:commonResources`.
- **Settings → Change password** and **Regenerate recovery code** (phase 10, 2026-10-21; decision D15). Both ask for the current password; wrong attempts share the sign-in lockout counter and, on reaching the lockout, also end the session. The new recovery code is shown once and the previous one stops working.
- **Settings → Open-source licences** (2026-10-21, requested by the owner after phase 10): the third-party software in the app grouped by licence, with its copyright holders, and the full licence texts bundled as resources and readable offline. Fulfils the MIT and BSD-3-Clause requirement that their notices accompany the binaries.
- **UI tests on both platforms** (phase 10): Compose UI tests in `commonTest` that run under Robolectric on the Android host (as a landscape tablet) and natively in the iOS simulator test binary, through the new test-only module `:core:uitest`. 30 tests cover sign-in, registration, password recovery, Settings, patients (with a drawn signature), professionals, reports, receipts and the shell.
- **Third-party licence inventory** in `docs/licencias-terceros.md`; `NOTICE` now also reproduces the KVault (MIT) and Skia (BSD-3-Clause, iOS) notices.
- `ErrorMessage` and `LoadingIndicator` shared components.

- **Payment receipts** (phase 9, 2026-10-21; decision D6): internal "justificante de pago", **not a Spanish tax invoice**, labelled as such on screen and in the PDF. Gapless sequential number per yearly series (`2026-000001`) assigned in the same transaction as the insert; the signed-in professional as author; issuer and patient data copied when issued; concept, exact amount, paid flag and optional payment method; "Save" and "Save and export PDF"; find by number, patient name or patient document; only the author marks it as paid/pending or voids it with a reason; receipts are never deleted.
- **PDF export** of receipts and clinical reports (phase 9, D17, D19): `:core:pdf`, an offline A4 PDF generator in common Kotlin with no libraries (standard Helvetica, WinAnsi, line and page breaks, page numbers). Warning before sharing, then the system share sheet or "save to" picker on Android and iPad; the temporary copy lives in the app's private folder and is deleted afterwards.
- Shared `PatientPicker`, `DocumentExportDialog` and `DocumentExporter` (Android `FileProvider`, iOS `UIActivityViewController` / `UIDocumentPickerViewController`).
- **Clinical reports** (phase 8, 2026-10-20): create with a random unique code (`INF-XXXXXXXX`), the signed-in professional as author and a patient picked from suggestions (active patients only); find by report number, patient name or patient document; view with dates, patient and author.
- Report edit history: only the author edits, every edit keeps the previous text and earlier versions can be viewed. Reports are never deleted: the author can annul one with a mandatory reason and it stays visible as annulled.
- Shared date formatting (`dd/MM/yyyy HH:mm` in the device's time zone).
- **Patient records** (phase 7, 2026-10-20): register with an identity document (NIF/NIE, passport or other), find by document, full name or email, view, edit, archive/restore and permanently delete (only without reports or invoices).
- **Signed consent** to the processing of personal and health data at the patient's creation: consent text with its version, signer (patient or legal representative), signature pad with clear/redo, signature history and "sign again". The consent text is a provisional draft pending legal review.
- **Unsaved changes confirmation**: leaving a patient, signature or professional form with changes (back arrow, system back or bottom bar) asks before discarding them.
- `SignaturePad` with PNG export on Android and iOS, `RecordField` and `ConfirmationDialog` shared components.
- **Professional records** (phase 6, 2026-10-20): register, find by one criterion (NIF/NIE, full name or licence number, ignoring accents and case), view, edit, archive/restore and permanently delete (only without account, reports or invoices), with confirmation for archiving and deleting.
- Professional association field (closed list of the 17 regional associations) in the professional record and the onboarding; the licence number is unique within its association.
- The onboarding links an existing record created without an account when NIF, association and licence number match.
- Optional contact email, phone (9–15 digits), address and notes in professional records.
- `SelectField` shared component and multi-line `ValidatedTextField`.
- **Tablet shell** (phase 5, 2026-10-20): Home with the eight section cards (register/find patient, professional, report and invoice) in an adaptive grid (1, 2 or 4 columns depending on the window width) and a greeting with the professional's first name.
- Window width classes (compact/medium/expanded) measured on the app window, so iPad Split View and Android multi-window are handled.
- `ListDetailLayout` (two panes on expanded windows, one at a time on narrower ones) and `EmptyState` shared components, ready for the search screens of phases 6–9.
- The eight sections open a "Coming soon" screen with a back arrow while the bottom bar stays visible with "Home" selected.
- **Local authentication** (phase 4, 2026-10-19): registration, login, password recovery and a mandatory onboarding step that creates the signed-in professional's record (NIF/NIE and licence number).
- One-time recovery code (20 characters, 100 bits) shown once at registration; resetting the password requires the email and the code and issues a new code.
- Inactivity lock in the app shell (proposed default: 5 minutes, also across restarts) and login lockout after 5 consecutive failures for 5 minutes.
- New `:feature:onboarding` module; profile and Settings now read the account and professional record from the database.
- `ValidatedTextField` and `RecoveryCodePanel` shared components; English and Spanish texts.
- Visual identity based on the PhysioApp logo: Android adaptive and themed launcher icons, iOS app icon with dark and tinted variants, Material 3 theme derived from the logo colors (AA contrast) and logo on the splash and login screens.
- Top bar with a back arrow on the Register and Forgot password screens.
- **Secure shared persistence** (phase 3, 2026-10-16): single `PhysioDatabase` v1 schema (`account`, `professional`, `patient`, `patient_signature`, `clinical_report`, `invoice_series`, `invoice`) encrypted with SQLCipher 4.17.0 on Android and iOS, with its passphrase in Keystore/Keychain.
- `:core:crypto` module: secure random, PBKDF2-HMAC-SHA256 password hasher, NFC normalization and constant-time comparison.
- `:domain:shared` module: name, email, password policy and Spanish NIF/NIE validators, and `Money` in cents.
- `verifyModuleBoundaries` checks of the allowed dependencies between layers.
- `README.md` (English), `README.es.md` (Spanish), `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, `SECURITY.md` and this changelog.
- `LICENSE` (Apache License 2.0) and `NOTICE` with provenance and the SQLCipher attribution (phase 0).

### Changed

- Documentation: repository URL (<https://github.com/ManuelAf-ConnectApp/PhysioApp>) and contact address for bugs, security vulnerabilities and conduct reports (<incidencias@connectapp.es>) filled in README, SECURITY, CODE_OF_CONDUCT and CONTRIBUTING, replacing the `TODO` markers (2026-10-21).
- **Accessibility** (phase 10): the "remember session" row is a single checkbox for screen readers; search results announce their selected state in the list-detail layout; Settings section titles are headings; errors are announced when they appear and the loading indicator is announced as "Loading"; buttons have a minimum height instead of a fixed one, so they grow with the system font size; bottom bar icons no longer repeat their visible label.
- Screen intents that carry passwords, recovery codes or emails, and the Home state, hide them in `toString()`.

- Session values confirmed (D14, 2026-10-21): 5 min inactivity lock, 5 failed logins → 5 min lockout, "Remember session" off by default.
- The Home invoice cards read "Create receipt" and "Find receipt", and archive/delete messages speak of receipts (D6).
- The invoice schema now copies issuer and patient data, stores the concept, VAT treatment, payment method and voiding reason, requires an amount above zero and has no physical deletion.
- Each screen on the navigation stack now has its own view models, cleared when leaving it: a reopened form starts empty instead of keeping what was typed, passwords included.
- **Tablets only** (phase 5): the iOS app targets iPad only, and the Android app declares large and extra-large screens only, so Google Play hides it from phones.
- The back stack is owned by the shell view model: switching between bottom bar tabs no longer grows it, and every stacked screen that is not a bottom bar tab shows a back arrow (iPad has no system back button).
- Settings content is centred with a maximum width on tablets, like Profile.
- First name and last names only need to be non-blank (D9): the previous "longer than 3 characters" rule rejected real names such as "Ana". Applies to sign-up and onboarding. The licence number accepts 1–20 characters (letters, digits, inner spaces, `.`, `/`, `-`) with at least one letter or digit, keeping leading zeros and separators.
- Project renamed from the CleanArch template to **PhysioApp** (phase 1, 2026-10-16): application id and bundle id `com.connectapp.physioapp`, Gradle convention plugins `physioapp.*`, visible app name.
- Authentication moved from the template's unencrypted profile cache (`AuthDatabase`) to the encrypted `PhysioDatabase`.
- The authenticated area shows a title-only top bar and a **Home · Profile · Settings** bottom bar; Settings is reduced to the user header, "About" (version) and "Sign out".
- The theme follows the system setting.

### Removed

- The "Coming soon" placeholder screen and its texts: every Home section has its screens (phase 9).
- The 1-second full-screen loader shown on every iOS navigation (phase 5).
- 48 unused strings inherited from the template, and its sample Home cards.
- Template features that do not belong in PhysioApp (phase 2, 2026-10-16): sample orders, backend login (Ktor), Sentry analytics, notifications, Settings preferences, profile editing and the sample privacy policy.
- The `INTERNET` and `POST_NOTIFICATIONS` Android permissions.

### Security

- **Content hidden in screenshots and the app switcher** (2026-10-21, decision D21): Android sets `FLAG_SECURE` on the window; the iPad shows a neutral cover while the app is not active (app switcher snapshot) and while the screen is recorded or mirrored. iOS does not allow blocking screenshots.
- Backups are SQLCipher files keyed with their own password (PBKDF2-HMAC-SHA512, 256,000 iterations, random salt, authenticated pages), never with the device key. They are written to a private temporary folder deleted after each operation, and their name only carries the date. Restoring from Settings re-checks the sign-in password against the login lockout; from the sign-in screen it is refused if the tablet has accounts.
- **iOS:** the database folder is excluded from iCloud/computer backups and its files use `NSFileProtectionCompleteUnlessOpen` (phase 10), matching Android's `allowBackup="false"`; the app's own encrypted backups replace them.

- Removed the hard-coded development credentials inherited from the template; a test prevents them from coming back.
- Android OS backups and device-to-device transfer disabled (`allowBackup="false"`, `data_extraction_rules.xml`).
- The app refuses to open the database if the runtime is not SQLCipher.
- Login errors do not reveal whether an email is registered, and unknown emails take the same time to verify.
- SQL logging from the iOS SQLite driver silenced so no clinical data reaches the console.

[Unreleased]: #unreleased
