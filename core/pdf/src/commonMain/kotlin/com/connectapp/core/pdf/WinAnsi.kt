package com.connectapp.core.pdf

/**
 * WinAnsiEncoding (PDF 1.7 annex D) of the standard Helvetica fonts and their glyph widths, in
 * thousandths of the font size (Adobe AFM metrics).
 *
 * Covers Spanish and the other Western European languages. Letters of Central and Eastern
 * European names that WinAnsi lacks (ă, ș, ł, č…) are written without their diacritic; anything
 * else becomes `?`. The PDF never embeds a font, which keeps it small and identical on both
 * platforms.
 */
internal object WinAnsi {

    private const val ASCII_FIRST = 0x20
    private const val ASCII_LAST = 0x7E
    private const val LATIN1_FIRST = 0xA0
    private const val LATIN1_LAST = 0xFF
    private const val LATIN1_LETTERS = 0xC0
    private const val QUESTION_MARK = 0x3F
    private const val SPACE = 0x20
    private const val ACCENTED_I_WIDTH = 278

    private val specials: Map<Char, Int> = mapOf(
        '€' to 0x80, '‚' to 0x82, 'ƒ' to 0x83, '„' to 0x84, '…' to 0x85, '†' to 0x86, '‡' to 0x87,
        'ˆ' to 0x88, '‰' to 0x89, 'Š' to 0x8A, '‹' to 0x8B, 'Œ' to 0x8C, 'Ž' to 0x8E, '‘' to 0x91,
        '’' to 0x92, '“' to 0x93, '”' to 0x94, '•' to 0x95, '–' to 0x96, '—' to 0x97, '˜' to 0x98,
        '™' to 0x99, 'š' to 0x9A, '›' to 0x9B, 'œ' to 0x9C, 'ž' to 0x9E, 'Ÿ' to 0x9F,
    )

    private val specialWidths: Map<Int, Int> = mapOf(
        0x80 to 556, 0x82 to 222, 0x83 to 556, 0x84 to 333, 0x85 to 1000, 0x86 to 556, 0x87 to 556,
        0x88 to 333, 0x89 to 1000, 0x8A to 667, 0x8B to 333, 0x8C to 1000, 0x8E to 611, 0x91 to 222,
        0x92 to 222, 0x93 to 333, 0x94 to 333, 0x95 to 350, 0x96 to 556, 0x97 to 1000, 0x98 to 333,
        0x99 to 1000, 0x9A to 500, 0x9B to 333, 0x9C to 944, 0x9E to 500, 0x9F to 667,
    )

    /** Latin Extended-A letters of European names, written with their base letter. */
    internal const val FALLBACK_FROM = "ĀāĂăĄąĆćĈĉĊċČčĎďĐđĒēĔĕĖėĘęĚěĞğĠġĢģĪīĮįİıĶķĹĺĻ" +
        "ļĽľŁłŃńŅņŇňŌōŐőŔŕŖŗŘřŚśŞşȘșŢţȚțŤťŪūŮůŰűŲųŹźŻż"
    internal const val FALLBACK_TO = "AaAaAaCcCcCcCcDdDdEeEeEeEeEeGgGgGgIiIiIiKkLlL" +
        "lLlLlNnNnNnOoOoRrRrRrSsSsSsTtTtTtUuUuUuUuZzZz"

    private val fallbacks: Map<Char, Char> = FALLBACK_FROM.zip(FALLBACK_TO).toMap()

    // Helvetica and Helvetica-Bold, characters 0x20..0x7E.
    private val regularAscii = intArrayOf(
        278, 278, 355, 556, 556, 889, 667, 191, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 278, 278, 584, 584, 584, 556,
        1015, 667, 667, 722, 722, 667, 611, 778, 722, 278, 500, 667, 556, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 278, 278, 278, 469, 556,
        333, 556, 556, 500, 556, 556, 278, 556, 556, 222, 222, 500, 222, 833, 556, 556,
        556, 556, 333, 500, 278, 556, 500, 722, 500, 500, 500, 334, 260, 334, 584,
    )
    private val boldAscii = intArrayOf(
        278, 333, 474, 556, 556, 889, 722, 238, 333, 333, 389, 584, 278, 333, 278, 278,
        556, 556, 556, 556, 556, 556, 556, 556, 556, 556, 333, 333, 584, 584, 584, 611,
        975, 722, 722, 722, 722, 667, 611, 778, 722, 278, 556, 722, 611, 833, 722, 778,
        667, 778, 722, 667, 611, 722, 667, 944, 667, 667, 611, 333, 278, 333, 584, 556,
        333, 556, 611, 556, 611, 556, 333, 611, 611, 278, 278, 556, 278, 889, 611, 611,
        611, 611, 389, 556, 333, 611, 556, 778, 556, 556, 500, 389, 280, 389, 584,
    )

    // 0xA0..0xBF (symbols; the bold metrics differ by a few units only).
    private val latin1Symbols = intArrayOf(
        278, 333, 556, 556, 556, 556, 260, 556, 333, 737, 370, 556, 584, 333, 737, 333,
        400, 584, 333, 333, 333, 556, 537, 278, 333, 333, 365, 556, 834, 834, 834, 611,
    )

    // 0xC0..0xFF: the base letter whose width the accented one shares, or '*' for its own width.
    private const val LATIN1_BASE = "AAAAAA*CEEEE****DNOOOOO*OUUUUY**aaaaaa*ceeee****onooooo*ouuuuy*y"
    private val latin1OwnWidths: Map<Int, Int> = mapOf(
        0xC6 to 1000,
        0xD7 to 584,
        0xDE to 667,
        0xDF to 611,
        0xE6 to 889,
        0xF7 to 584,
        0xFE to 556,
    )

    /** The WinAnsi byte that represents [char] (0..255), or null for characters that are skipped. */
    fun encode(char: Char): Int? {
        val code = char.code
        return when {
            char == '\t' -> SPACE
            code < ASCII_FIRST -> null
            code <= ASCII_LAST -> code
            code in LATIN1_FIRST..LATIN1_LAST -> code
            else -> specials[char] ?: fallbacks[char]?.code ?: QUESTION_MARK
        }
    }

    /** Width of the glyph of a WinAnsi [byte] in thousandths of the font size. */
    fun width(byte: Int, bold: Boolean): Int = when {
        byte in ASCII_FIRST..ASCII_LAST -> (if (bold) boldAscii else regularAscii)[byte - ASCII_FIRST]
        byte < LATIN1_FIRST -> specialWidths[byte] ?: regularAscii[0]
        byte < LATIN1_LETTERS -> latin1Symbols[byte - LATIN1_FIRST]
        else -> latin1LetterWidth(byte, bold)
    }

    private fun latin1LetterWidth(byte: Int, bold: Boolean): Int {
        val base = LATIN1_BASE[byte - LATIN1_LETTERS]
        return when {
            base != '*' -> width(base.code, bold)
            byte in 0xCC..0xCF || byte in 0xEC..0xEF -> ACCENTED_I_WIDTH
            else -> latin1OwnWidths.getValue(byte)
        }
    }
}
