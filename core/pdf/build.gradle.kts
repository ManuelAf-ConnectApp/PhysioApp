plugins {
    id("physioapp.kmp.base")
}

// Offline PDF generation for receipts and clinical reports (phase 9, D17, D19). Pure Kotlin, no
// third-party or platform dependencies: the standard Helvetica fonts of every PDF reader with
// WinAnsiEncoding, so the same bytes come out on Android and iOS and the layout is unit-tested.
