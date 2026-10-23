package com.connectapp.core.pdf

/**
 * Serialises pages of content-stream operators as a PDF 1.4 file: catalogue, page tree, the two
 * standard Helvetica fonts, one page object and one uncompressed content stream per page, a
 * cross-reference table with exact byte offsets and the trailer. The output is plain ASCII except
 * for the binary marker comment, and the same input always gives the same bytes.
 */
internal object PdfWriter {

    private const val CATALOG = 1
    private const val PAGES = 2
    private const val FONT_REGULAR = 3
    private const val FONT_BOLD = 4
    private const val INFO = 5
    private const val FIRST_PAGE = 6
    private const val OCTAL = 8
    private const val PRINTABLE_FIRST = 0x20
    private const val PRINTABLE_LAST = 0x7E
    private val BINARY_MARKER = byteArrayOf(0x25, 0xE2.toByte(), 0xE3.toByte(), 0xCF.toByte(), 0xD3.toByte(), 0x0A)

    fun write(title: String, width: Float, height: Float, pageContents: List<String>): ByteArray {
        val out = ByteOutput()
        val offsets = mutableMapOf<Int, Int>()
        fun obj(number: Int, body: String) {
            offsets[number] = out.size
            out.ascii("$number 0 obj\n$body\nendobj\n")
        }

        out.ascii("%PDF-1.4\n")
        // Bytes above 127 tell transfer tools that the file is binary (PDF 1.7 §7.5.2).
        out.bytes(BINARY_MARKER)

        val pageNumbers = pageContents.indices.map { FIRST_PAGE + 2 * it }
        obj(CATALOG, "<< /Type /Catalog /Pages $PAGES 0 R >>")
        val kids = pageNumbers.joinToString(" ") { "$it 0 R" }
        obj(PAGES, "<< /Type /Pages /Kids [$kids] /Count ${pageContents.size} >>")
        obj(FONT_REGULAR, font("Helvetica"))
        obj(FONT_BOLD, font("Helvetica-Bold"))
        obj(INFO, "<< /Title (${encodeText(title)}) /Producer (PhysioApp) >>")
        pageContents.forEachIndexed { index, content ->
            val pageNumber = pageNumbers[index]
            obj(
                pageNumber,
                "<< /Type /Page /Parent $PAGES 0 R /MediaBox [0 0 ${num(width)} ${num(height)}] " +
                    "/Resources << /Font << /F1 $FONT_REGULAR 0 R /F2 $FONT_BOLD 0 R >> >> " +
                    "/Contents ${pageNumber + 1} 0 R >>",
            )
            obj(pageNumber + 1, "<< /Length ${content.length} >>\nstream\n$content\nendstream")
        }

        val objectCount = FIRST_PAGE + 2 * pageContents.size
        val xref = out.size
        out.ascii("xref\n0 $objectCount\n")
        // Each entry is exactly 20 bytes: 10-digit offset, generation, type and a two-byte EOL.
        out.ascii("0000000000 65535 f \n")
        for (number in 1 until objectCount) {
            out.ascii("${offsets.getValue(number).toString().padStart(10, '0')} 00000 n \n")
        }
        out.ascii("trailer\n<< /Size $objectCount /Root $CATALOG 0 R /Info $INFO 0 R >>\nstartxref\n$xref\n%%EOF\n")
        return out.toByteArray()
    }

    /**
     * [text] as the inside of a PDF literal string in WinAnsi: printable ASCII as is (with `\`,
     * `(` and `)` escaped) and every other byte as a three-digit octal escape, so content streams
     * stay ASCII and their length in characters is their length in bytes.
     */
    fun encodeText(text: String): String = buildString {
        text.forEach { char ->
            val byte = WinAnsi.encode(char) ?: return@forEach
            when {
                byte == '\\'.code || byte == '('.code || byte == ')'.code -> append('\\').append(byte.toChar())
                byte in PRINTABLE_FIRST..PRINTABLE_LAST -> append(byte.toChar())
                else -> append('\\').append(byte.toString(OCTAL).padStart(3, '0'))
            }
        }
    }

    private fun font(name: String) = "<< /Type /Font /Subtype /Type1 /BaseFont /$name /Encoding /WinAnsiEncoding >>"

    private class ByteOutput {
        private var buffer = ByteArray(INITIAL_CAPACITY)
        var size = 0
            private set

        fun ascii(text: String) {
            ensure(text.length)
            text.forEach { buffer[size++] = it.code.toByte() }
        }

        fun bytes(data: ByteArray) {
            ensure(data.size)
            data.copyInto(buffer, size)
            size += data.size
        }

        fun toByteArray(): ByteArray = buffer.copyOf(size)

        private fun ensure(extra: Int) {
            if (size + extra > buffer.size) buffer = buffer.copyOf(maxOf(buffer.size * 2, size + extra))
        }

        private companion object {
            const val INITIAL_CAPACITY = 16 * 1024
        }
    }
}
