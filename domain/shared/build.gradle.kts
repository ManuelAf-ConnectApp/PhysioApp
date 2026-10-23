plugins {
    id("physioapp.kmp.base")
}

// Pure-Kotlin business rules shared by several bounded contexts (auth, patients, professionals,
// invoices): input validators and exact money arithmetic. No dependencies.
