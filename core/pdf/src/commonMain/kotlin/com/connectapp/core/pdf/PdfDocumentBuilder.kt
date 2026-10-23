package com.connectapp.core.pdf

/**
 * Lays out a simple A4 document top to bottom — headings, labelled fields, paragraphs and
 * highlighted notices — wrapping lines and adding pages as needed, then serialises it as PDF.
 *
 * ```
 * val bytes = PdfDocumentBuilder(title = "Justificante 2026-000001", footer = "PhysioApp")
 *     .heading("Justificante de pago")
 *     .notice("Este documento no es una factura")
 *     .field("Paciente", "Íñigo López")
 *     .build()
 * ```
 *
 * [title] goes to the document properties, which some viewers show: keep personal data out of it.
 * [footer] and the page number ([pageLabel]) are printed at the bottom of every page.
 */
class PdfDocumentBuilder(
    private val title: String,
    private val footer: String = "",
    private val pageLabel: (page: Int, pageCount: Int) -> String = { page, count -> "$page / $count" },
) {
    private val pages = mutableListOf<MutableList<DrawOp>>()
    private var cursorY = 0f

    init {
        newPage()
    }

    fun heading(text: String): PdfDocumentBuilder = apply {
        lines(text, HEADING_SIZE, bold = true, gray = BLACK)
        space(PARAGRAPH_GAP)
    }

    fun subheading(text: String): PdfDocumentBuilder = apply {
        space(PARAGRAPH_GAP)
        lines(text, SUBHEADING_SIZE, bold = true, gray = BLACK)
        space(SMALL_GAP)
    }

    fun paragraph(text: String, bold: Boolean = false): PdfDocumentBuilder = apply {
        lines(text, BODY_SIZE, bold = bold, gray = BLACK)
        space(PARAGRAPH_GAP)
    }

    /** Small grey text, for legal mentions and other remarks. */
    fun note(text: String): PdfDocumentBuilder = apply {
        lines(text, NOTE_SIZE, bold = false, gray = MUTED)
        space(PARAGRAPH_GAP)
    }

    /** A label in small grey capitals above its [value]; the label never ends a page alone. */
    fun field(label: String, value: String): PdfDocumentBuilder = apply {
        ensureSpace(lineHeight(LABEL_SIZE) + lineHeight(BODY_SIZE))
        lines(label.uppercase(), LABEL_SIZE, bold = true, gray = MUTED)
        space(LABEL_GAP)
        lines(value.ifBlank { "—" }, BODY_SIZE, bold = false, gray = BLACK)
        space(PARAGRAPH_GAP)
    }

    /** Bold text on a grey band, e.g. "Este documento no es una factura" or "ANULADO". */
    fun notice(text: String): PdfDocumentBuilder = apply {
        val wrapped = wrap(text, NOTICE_SIZE, bold = true, CONTENT_WIDTH - 2 * NOTICE_PADDING)
        val height = wrapped.size * lineHeight(NOTICE_SIZE) + 2 * NOTICE_PADDING
        // A notice longer than a page (never expected) is printed without its band.
        if (height <= CONTENT_HEIGHT) {
            ensureSpace(height)
            page += DrawOp.Rect(MARGIN, cursorY - height, CONTENT_WIDTH, height, NOTICE_BAND)
        }
        cursorY -= NOTICE_PADDING
        wrapped.forEach { line ->
            ensureSpace(lineHeight(NOTICE_SIZE))
            cursorY -= lineHeight(NOTICE_SIZE)
            page += DrawOp.Text(MARGIN + NOTICE_PADDING, cursorY + descent(NOTICE_SIZE), line, NOTICE_SIZE, true, BLACK)
        }
        cursorY -= NOTICE_PADDING
        space(PARAGRAPH_GAP)
    }

    fun rule(): PdfDocumentBuilder = apply {
        ensureSpace(PARAGRAPH_GAP)
        page += DrawOp.Line(MARGIN, cursorY, MARGIN + CONTENT_WIDTH, cursorY, RULE)
        space(PARAGRAPH_GAP)
    }

    fun space(points: Float): PdfDocumentBuilder = apply {
        cursorY -= points
    }

    fun build(): ByteArray {
        val count = pages.size
        val contents = pages.mapIndexed { index, ops ->
            val footerOps = buildList {
                add(DrawOp.Line(MARGIN, FOOTER_RULE_Y, MARGIN + CONTENT_WIDTH, FOOTER_RULE_Y, RULE))
                if (footer.isNotBlank()) {
                    val text = fitToWidth(footer, NOTE_SIZE, CONTENT_WIDTH * FOOTER_TEXT_SHARE)
                    add(DrawOp.Text(MARGIN, FOOTER_TEXT_Y, text, NOTE_SIZE, false, MUTED))
                }
                val label = pageLabel(index + 1, count)
                val x = MARGIN + CONTENT_WIDTH - textWidth(label, NOTE_SIZE, bold = false)
                add(DrawOp.Text(x, FOOTER_TEXT_Y, label, NOTE_SIZE, false, MUTED))
            }
            (ops + footerOps).joinToString("\n") { it.toContent() }
        }
        return PdfWriter.write(title, PAGE_WIDTH, PAGE_HEIGHT, contents)
    }

    private val page: MutableList<DrawOp> get() = pages.last()

    private fun newPage() {
        pages += mutableListOf<DrawOp>()
        cursorY = PAGE_HEIGHT - MARGIN
    }

    private fun ensureSpace(height: Float) {
        if (cursorY - height < BOTTOM_LIMIT) newPage()
    }

    private fun lines(text: String, size: Float, bold: Boolean, gray: Float) {
        wrap(text, size, bold, CONTENT_WIDTH).forEach { line ->
            ensureSpace(lineHeight(size))
            cursorY -= lineHeight(size)
            if (line.isNotEmpty()) page += DrawOp.Text(MARGIN, cursorY + descent(size), line, size, bold, gray)
        }
    }

    internal companion object {
        // A4 in points (1/72 inch).
        const val PAGE_WIDTH = 595.28f
        const val PAGE_HEIGHT = 841.89f
        const val MARGIN = 56f
        const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
        const val FOOTER_RULE_Y = 48f
        const val FOOTER_TEXT_Y = 34f
        const val BOTTOM_LIMIT = 64f
        const val CONTENT_HEIGHT = PAGE_HEIGHT - MARGIN - BOTTOM_LIMIT
        const val FOOTER_TEXT_SHARE = 0.75f

        const val HEADING_SIZE = 18f
        const val SUBHEADING_SIZE = 13f
        const val BODY_SIZE = 11f
        const val NOTICE_SIZE = 11f
        const val NOTE_SIZE = 8.5f
        const val LABEL_SIZE = 8f
        const val LINE_SPACING = 1.35f
        const val DESCENT = 0.3f

        const val PARAGRAPH_GAP = 10f
        const val SMALL_GAP = 4f
        const val LABEL_GAP = 1f
        const val NOTICE_PADDING = 8f

        // Grey levels: 0 is black, 1 is white.
        const val BLACK = 0f
        const val MUTED = 0.4f
        const val RULE = 0.75f
        const val NOTICE_BAND = 0.9f

        fun lineHeight(size: Float) = size * LINE_SPACING
        fun descent(size: Float) = (LINE_SPACING - 1f + DESCENT) * size / 2
    }
}

/** Width of [text] set in Helvetica (or Helvetica-Bold) at [size] points. */
internal fun textWidth(text: String, size: Float, bold: Boolean): Float =
    text.sumOf { char -> WinAnsi.encode(char)?.let { WinAnsi.width(it, bold) } ?: 0 } * size / 1000f

/**
 * Splits [text] into lines no wider than [maxWidth]: on spaces where possible, inside a word only
 * when the word alone is too wide. Line breaks of the text are kept, blank lines included.
 */
internal fun wrap(text: String, size: Float, bold: Boolean, maxWidth: Float): List<String> =
    text.replace("\r\n", "\n").replace('\r', '\n').split('\n').flatMap { paragraph ->
        val lines = mutableListOf<String>()
        var current = ""
        paragraph.split(' ').filter { it.isNotEmpty() }.forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            when {
                textWidth(candidate, size, bold) <= maxWidth -> current = candidate
                textWidth(word, size, bold) <= maxWidth -> {
                    lines += current
                    current = word
                }
                else -> {
                    if (current.isNotEmpty()) lines += current
                    val pieces = splitLongWord(word, size, bold, maxWidth)
                    lines += pieces.dropLast(1)
                    current = pieces.last()
                }
            }
        }
        lines + current
    }

private fun splitLongWord(word: String, size: Float, bold: Boolean, maxWidth: Float): List<String> {
    val pieces = mutableListOf<String>()
    var current = ""
    word.forEach { char ->
        if (current.isNotEmpty() && textWidth(current + char, size, bold) > maxWidth) {
            pieces += current
            current = ""
        }
        current += char
    }
    return pieces + current
}

/** [text] cut with an ellipsis so that it fits in one line. */
internal fun fitToWidth(text: String, size: Float, maxWidth: Float): String {
    if (textWidth(text, size, bold = false) <= maxWidth) return text
    var cut = text
    while (cut.isNotEmpty() && textWidth("$cut…", size, bold = false) > maxWidth) cut = cut.dropLast(1)
    return "$cut…"
}

/** One drawing operation of a page, already in PDF coordinates (origin at the bottom left). */
internal sealed interface DrawOp {
    fun toContent(): String

    data class Text(
        val x: Float,
        val y: Float,
        val text: String,
        val size: Float,
        val bold: Boolean,
        val gray: Float,
    ) : DrawOp {
        override fun toContent(): String {
            val font = if (bold) "/F2" else "/F1"
            val position = "${num(x)} ${num(y)} Td"
            return "BT $font ${num(size)} Tf ${num(gray)} g $position (${PdfWriter.encodeText(text)}) Tj ET"
        }
    }

    data class Rect(val x: Float, val y: Float, val width: Float, val height: Float, val gray: Float) : DrawOp {
        override fun toContent(): String = "${num(gray)} g ${num(x)} ${num(y)} ${num(width)} ${num(height)} re f"
    }

    data class Line(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val gray: Float) : DrawOp {
        override fun toContent(): String =
            "${num(gray)} G 0.5 w ${num(x1)} ${num(y1)} m ${num(x2)} ${num(y2)} l S"
    }
}

/** A PDF number with two decimals at most, independent of the device locale. */
internal fun num(value: Float): String {
    val hundredths = kotlin.math.round(value * 100).toLong()
    val sign = if (hundredths < 0) "-" else ""
    val absolute = kotlin.math.abs(hundredths)
    val fraction = absolute % 100
    return when {
        fraction == 0L -> "$sign${absolute / 100}"
        fraction % 10 == 0L -> "$sign${absolute / 100}.${fraction / 10}"
        else -> "$sign${absolute / 100}.${fraction.toString().padStart(2, '0')}"
    }
}
