package com.connectapp.domain.shared.validation

/**
 * Professional association membership number ("número de colegiado").
 *
 * There is no national format: each of the 17 regional associations numbers its members its own
 * way. Only a syntactic check is made (D9, confirmed 2026-10-20):
 * 1–[MAX_LENGTH] characters after trimming, from ASCII letters, digits, inner spaces, `.`, `/` and
 * `-`, with at least one letter or digit. Leading zeros, prefixes and separators are kept. A valid
 * shape never means the person is actually a registered member.
 */
object LicenseNumberValidator {
    const val MAX_LENGTH = 20
    private val ALLOWED = Regex("^[A-Za-z0-9 ./-]+$")

    /**
     * Key for duplicate detection: trimmed, upper-case, inner whitespace collapsed to one space.
     * Zeros and separators are kept, because "01234" and "1234" may be different members.
     */
    fun normalize(value: String): String = value.trim().replace(WHITESPACE_RUN, " ").uppercase()

    fun isValid(value: String): Boolean {
        val license = value.trim()
        return license.length in 1..MAX_LENGTH &&
            ALLOWED.matches(license) &&
            license.any { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' }
    }

    private val WHITESPACE_RUN = Regex("\\s+")
}
