package com.connectapp.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.RequestQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.connectapp.commonresources.*
import com.connectapp.domain.backup.BackupReminder
import org.jetbrains.compose.resources.StringResource

/**
 * The eight Home cards, in pairs per entity — "register" then "search" — so
 * a two-column grid shows one entity per row and a four-column grid two entities per row.
 */
enum class HomeDestination(val title: StringResource, val subtitle: StringResource, val icon: ImageVector) {
    PATIENT_REGISTER(home_patient_register_title, home_patient_register_subtitle, Icons.Filled.PersonAdd),
    PATIENT_SEARCH(home_patient_search_title, home_patient_search_subtitle, Icons.Filled.PersonSearch),
    PROFESSIONAL_REGISTER(home_professional_register_title, home_professional_register_subtitle, Icons.Filled.Badge),
    PROFESSIONAL_SEARCH(home_professional_search_title, home_professional_search_subtitle, Icons.Filled.Search),
    REPORT_CREATE(home_report_create_title, home_report_create_subtitle, Icons.AutoMirrored.Filled.NoteAdd),
    REPORT_SEARCH(home_report_search_title, home_report_search_subtitle, Icons.Filled.Description),
    INVOICE_CREATE(home_invoice_create_title, home_invoice_create_subtitle, Icons.Filled.RequestQuote),
    INVOICE_SEARCH(home_invoice_search_title, home_invoice_search_subtitle, Icons.AutoMirrored.Filled.ReceiptLong),
}

data class HomeState(
    /** First name of the signed-in professional for the greeting; null until it is loaded. */
    val firstName: String? = null,
    val destinations: List<HomeDestination> = HomeDestination.entries,
    /** Shown above the cards while a backup is due (plan §21.7). */
    val backupReminder: BackupReminder = BackupReminder.NoReminder,
) {
    // Personal data never reaches logs.
    override fun toString(): String = "HomeState(loaded=${firstName != null})"
}
