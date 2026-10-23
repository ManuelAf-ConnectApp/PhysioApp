package com.connectapp.domain.shared.text

/**
 * Accent-insensitive, case-insensitive search form of a name (docs/00-auditoria-y-plan.md §6):
 * lower-case, without diacritics and with runs of whitespace collapsed, so "Íñigo  López" and
 * "inigo lopez" match with a plain SQLite `LIKE` (no ICU collation available).
 *
 * Common Kotlin has no Unicode decomposition, so diacritics are folded with an explicit table that
 * covers Latin-1 and Latin Extended-A letters — enough for Spanish and other Western European
 * names. Characters outside the table are kept as they are.
 */
object SearchText {
    private const val ACCENTED =
        "àáâãäåāăąçćĉċčďđèéêëēĕėęěĝğġģĥħìíîïĩīĭįıĵķĺļ" +
            "ľŀłñńņňŉòóôõöøōŏőŕŗřśŝşšţťŧùúûüũūŭůűųŵýÿŷźżž"
    private const val PLAIN =
        "aaaaaaaaacccccddeeeeeeeeegggghhiiiiiiiiijkll" +
            "lllnnnnnooooooooorrrsssstttuuuuuuuuuuwyyyzzz"
    private val FOLDED_LIGATURES = mapOf('æ' to "ae", 'œ' to "oe", 'ß' to "ss")
    private val WHITESPACE = Regex("\\s+")

    init {
        check(ACCENTED.length == PLAIN.length) { "Diacritics table is misaligned" }
    }

    fun normalize(value: String): String = buildString {
        value.trim().lowercase().replace(WHITESPACE, " ").forEach { char ->
            val index = ACCENTED.indexOf(char)
            when {
                index >= 0 -> append(PLAIN[index])
                char in FOLDED_LIGATURES -> append(FOLDED_LIGATURES.getValue(char))
                else -> append(char)
            }
        }
    }

    /** Search form of a person's full name: `"<first name> <last name>"`, normalized. */
    fun fullName(firstName: String, lastName: String): String = normalize("$firstName $lastName")
}
