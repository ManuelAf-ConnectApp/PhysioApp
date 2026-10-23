package com.connectapp.domain.shared.validation

/**
 * Identity document of a patient (D9, confirmed 2026-10-20). Not every patient has a NIF/NIE:
 * minors under 14 often have no DNI and foreign visitors may only carry a passport. The [name] is
 * stored in the database: never rename an entry.
 */
enum class IdentityDocumentType { NIF_NIE, PASSPORT, OTHER }

/**
 * - [IdentityDocumentType.NIF_NIE]: checked with its control letter ([SpanishIdValidator]).
 * - [IdentityDocumentType.PASSPORT]: 3–20 ASCII letters or digits (spaces are ignored).
 * - [IdentityDocumentType.OTHER]: 1–40 characters from letters, digits, inner spaces, `.`, `/`
 *   and `-`, with at least one letter or digit, e.g. "DNI tutor 12345678Z".
 */
object IdentityDocumentValidator {
    private const val PASSPORT_MIN = 3
    private const val PASSPORT_MAX = 20
    const val OTHER_MAX = 40
    private val PASSPORT = Regex("^[A-Z0-9]+$")
    private val OTHER = Regex("^[A-Za-z0-9 ./-]+$")
    private val WHITESPACE_RUN = Regex("\\s+")

    /** Key for uniqueness and search: what two spellings of the same document have in common. */
    fun normalize(type: IdentityDocumentType, number: String): String = when (type) {
        IdentityDocumentType.NIF_NIE -> SpanishIdValidator.normalize(number)
        IdentityDocumentType.PASSPORT -> number.filterNot { it.isWhitespace() }.uppercase()
        IdentityDocumentType.OTHER -> number.trim().replace(WHITESPACE_RUN, " ").uppercase()
    }

    fun isValid(type: IdentityDocumentType, number: String): Boolean = when (type) {
        IdentityDocumentType.NIF_NIE -> SpanishIdValidator.isValid(number)
        IdentityDocumentType.PASSPORT -> normalize(type, number).let {
            it.length in PASSPORT_MIN..PASSPORT_MAX && PASSPORT.matches(it)
        }
        IdentityDocumentType.OTHER -> number.trim().let { other ->
            other.length in 1..OTHER_MAX && OTHER.matches(other) && other.any { it.isAsciiLetterOrDigit() }
        }
    }

    private fun Char.isAsciiLetterOrDigit() = this in 'A'..'Z' || this in 'a'..'z' || this in '0'..'9'
}
