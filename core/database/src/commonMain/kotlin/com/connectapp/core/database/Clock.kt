package com.connectapp.core.database

/**
 * Wall-clock epoch milliseconds, used to stamp cache rows with when they were last synced
 * (`fetchedAt` — see specs/010-offline-cache-layer/data-model.md). Kept as a tiny expect/actual
 * here instead of adding a `kotlinx-datetime` dependency (plan.md Technical Context: no new
 * dependencies beyond SQLDelight) since this is the only place in the app that needs one.
 */
expect fun currentEpochMillis(): Long
