package com.connectapp.domain.shared.validation

/**
 * Password policy (docs/00-auditoria-y-plan.md, D16 — confirmed 2026-10-16):
 * at least [MIN_LENGTH] characters, with at least one upper-case letter, one lower-case letter,
 * one digit and one special character.
 *
 * - Letters include non-ASCII ones (`Ñ` counts as upper-case, `ñ` as lower-case).
 * - A special character is anything that is neither a letter, a digit nor whitespace.
 * - Spaces are allowed (passphrases) but do not count as special characters.
 * - [MAX_LENGTH] bounds the input of the key-derivation function.
 */
object PasswordPolicy {
    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 128

    enum class Violation { TOO_SHORT, TOO_LONG, NO_UPPERCASE, NO_LOWERCASE, NO_DIGIT, NO_SPECIAL }

    /** Every rule [password] breaks; empty when it complies. */
    fun violations(password: String): Set<Violation> = buildSet {
        if (password.length < MIN_LENGTH) add(Violation.TOO_SHORT)
        if (password.length > MAX_LENGTH) add(Violation.TOO_LONG)
        if (password.none { it.isUpperCase() }) add(Violation.NO_UPPERCASE)
        if (password.none { it.isLowerCase() }) add(Violation.NO_LOWERCASE)
        if (password.none { it.isDigit() }) add(Violation.NO_DIGIT)
        if (password.none { !it.isLetterOrDigit() && !it.isWhitespace() }) add(Violation.NO_SPECIAL)
    }

    fun isValid(password: String): Boolean = violations(password).isEmpty()

    /** The confirmation must match exactly and satisfy the same policy. */
    fun isValidConfirmation(password: String, confirmation: String): Boolean =
        confirmation == password && isValid(confirmation)
}
