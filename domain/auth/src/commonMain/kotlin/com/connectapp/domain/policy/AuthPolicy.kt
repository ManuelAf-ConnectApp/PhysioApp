package com.connectapp.domain.policy

/**
 * Session policy (docs/00-auditoria-y-plan.md, D14 — PROPUESTA pending confirmation): a session
 * ends after [IDLE_TIMEOUT_MILLIS] without user interaction, also across app restarts, so a
 * remembered session only survives a restart that happens within that window.
 */
object SessionPolicy {
    const val IDLE_TIMEOUT_MILLIS: Long = 5 * 60 * 1000L
}

/**
 * Brute-force protection for login (docs/00-auditoria-y-plan.md §14, PROPUESTA):
 * [MAX_FAILED_ATTEMPTS] consecutive failures lock the email for [LOCKOUT_MILLIS].
 */
object LoginLockoutPolicy {
    const val MAX_FAILED_ATTEMPTS = 5
    const val LOCKOUT_MILLIS: Long = 5 * 60 * 1000L
}

/** Wall-clock time source, injected so session and lockout rules are testable. */
fun interface EpochClock {
    fun nowMillis(): Long
}
