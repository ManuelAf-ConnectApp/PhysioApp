package com.connectapp.domain.shared.security

/**
 * One-time offline recovery code (docs/00-auditoria-y-plan.md, D4 — confirmed 2026-10-16).
 *
 * [LENGTH] characters from a 32-symbol alphabet without look-alike characters (no `0`/`O`, `1`/`I`)
 * give 100 bits of entropy, so the code cannot be guessed even without attempt limits. It is shown
 * in groups of [GROUP_SIZE] (`ABCDE-FGHJK-MNPQR-STUVW`) and accepted in any case, with or without
 * separators.
 *
 * Generation needs a CSPRNG, which lives in `:core:crypto`; this object only maps random bytes to
 * symbols, so the domain stays platform-free and testable.
 */
object RecoveryCode {
    const val ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
    const val LENGTH = 20
    const val GROUP_SIZE = 5
    private const val SEPARATOR = '-'

    /**
     * Builds a code from [LENGTH] random bytes. The alphabet has 32 symbols, a divisor of 256, so
     * taking each byte modulo 32 keeps every symbol equally likely.
     */
    fun fromRandomBytes(bytes: ByteArray): String {
        require(bytes.size == LENGTH) { "A recovery code needs exactly $LENGTH random bytes" }
        return bytes.joinToString("") { ALPHABET[(it.toInt() and 0xFF) % ALPHABET.length].toString() }
    }

    /** Upper-case, without spaces or hyphens: the form that is hashed and compared. */
    fun normalize(input: String): String =
        input.filterNot { it.isWhitespace() || it == SEPARATOR }.uppercase()

    fun isWellFormed(input: String): Boolean {
        val code = normalize(input)
        return code.length == LENGTH && code.all { it in ALPHABET }
    }

    /** Groups a normalized code for display: `ABCDE-FGHJK-MNPQR-STUVW`. */
    fun format(code: String): String = normalize(code).chunked(GROUP_SIZE).joinToString(SEPARATOR.toString())
}
