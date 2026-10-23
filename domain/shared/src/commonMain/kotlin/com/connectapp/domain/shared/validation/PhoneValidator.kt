package com.connectapp.domain.shared.validation

/**
 * Optional contact phone (D9, confirmed 2026-10-20): digits, spaces and `+` only, with 9 to 15
 * digits (a Spanish number has 9; E.164 allows up to 15 with the country code).
 */
object PhoneValidator {
    private const val MIN_DIGITS = 9
    private const val MAX_DIGITS = 15
    private val ALLOWED = Regex("^\\+?[0-9 ]+$")

    /** Trimmed, with inner whitespace collapsed to one space. */
    fun normalize(value: String): String = value.trim().replace(Regex("\\s+"), " ")

    fun isValid(value: String): Boolean {
        val phone = normalize(value)
        return ALLOWED.matches(phone) && phone.count { it.isDigit() } in MIN_DIGITS..MAX_DIGITS
    }
}
