package com.connectapp.domain.shared.validation

/**
 * Spanish personal identifiers: NIF of nationals (DNI: 8 digits + control letter) and NIE of
 * foreigners (X/Y/Z + 7 digits + control letter), using the official modulo-23 control letter.
 *
 * Which entities require one, and whether people without NIF/NIE (minors, foreigners without
 * NIE) need an alternative identifier, is still pending (docs/00-auditoria-y-plan.md, D9).
 * Special NIF types K/L/M are not accepted.
 */
object SpanishIdValidator {
    private const val CONTROL_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE"
    private val DNI = Regex("^[0-9]{8}[A-Z]$")
    private val NIE = Regex("^[XYZ][0-9]{7}[A-Z]$")

    /** Upper-case, without spaces, hyphens or dots — the form used for storage and search. */
    fun normalize(value: String): String =
        value.filterNot { it.isWhitespace() || it == '-' || it == '.' }.uppercase()

    fun isValid(value: String): Boolean {
        val id = normalize(value)
        val digits = when {
            DNI.matches(id) -> id.substring(0, 8)
            NIE.matches(id) -> "XYZ".indexOf(id[0]).toString() + id.substring(1, 8)
            else -> return false
        }
        return CONTROL_LETTERS[(digits.toInt() % CONTROL_LETTERS.length)] == id.last()
    }
}
