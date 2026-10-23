package com.connectapp.commonresources.export

import com.connectapp.commonresources.format.formatDateTime
import com.connectapp.commonresources.pdf_footer
import com.connectapp.commonresources.pdf_page
import org.jetbrains.compose.resources.getString

/** Footer and page numbering shared by every exported PDF, in the app's language. */
class PdfPageTexts(val footer: String, private val pageTemplate: String) {
    fun pageLabel(page: Int, pageCount: Int): String =
        pageTemplate.replace("%1\$d", page.toString()).replace("%2\$d", pageCount.toString())

    companion object {
        suspend fun load(generatedAtEpochMillis: Long): PdfPageTexts = PdfPageTexts(
            footer = getString(pdf_footer, formatDateTime(generatedAtEpochMillis)),
            pageTemplate = getString(pdf_page),
        )
    }
}
