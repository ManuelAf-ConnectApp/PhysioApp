package com.connectapp.domain.shared.security

/**
 * Visible code of a clinical report (D17): `INF-` followed by [RANDOM_LENGTH]
 * random symbols of the [RecoveryCode.ALPHABET] (no look-alike characters), e.g. `INF-7K3M9QX2`.
 * 32^8 ≈ 10^12 combinations make collisions rare; the repository still checks for them.
 * Like [RecoveryCode], generation takes the random bytes from `:core:crypto`.
 */
object ReportCode {
    const val PREFIX = "INF-"
    const val RANDOM_LENGTH = 8

    fun fromRandomBytes(bytes: ByteArray): String {
        require(bytes.size == RANDOM_LENGTH) { "A report code needs exactly $RANDOM_LENGTH random bytes" }
        val alphabet = RecoveryCode.ALPHABET
        return PREFIX + bytes.joinToString("") { alphabet[(it.toInt() and 0xFF) % alphabet.length].toString() }
    }

    /** Search form of what the user typed: upper-case without spaces. */
    fun normalizeQuery(input: String): String = input.filterNot { it.isWhitespace() }.uppercase()
}
