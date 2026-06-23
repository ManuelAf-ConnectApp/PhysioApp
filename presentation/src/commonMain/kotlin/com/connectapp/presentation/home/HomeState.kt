package com.connectapp.presentation.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AddChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.connectapp.domain.model.User
import com.connectapp.designresources.TokensResources
import org.jetbrains.compose.resources.StringResource

data class HomeState(
    val user: User? = null,
    val items: List<DashboardItem> = defaultDashboardItems
)

data class DashboardItem(
    val id: StringResource,
    val title: StringResource,
    val subtitle: StringResource,
    val icon: ImageVector
)

val defaultDashboardItems = listOf(
    DashboardItem(
        TokensResources.idNewPatient,
        TokensResources.newPatient,
        TokensResources.newPatientDesc,
        Icons.Default.PersonAdd
    ),
    DashboardItem(
        TokensResources.idSearchPatient,
        TokensResources.searchPatient,
        TokensResources.searchPatientDesc,
        Icons.Default.Search
    ),
    DashboardItem(
        TokensResources.idNewProfessional,
        TokensResources.newProfessional,
        TokensResources.newProfessionalDesc,
        Icons.Default.Group
    ),
    DashboardItem(
        TokensResources.idSearchProfessional,
        TokensResources.searchProfessional,
        TokensResources.searchProfessionalDesc,
        Icons.Default.PersonSearch
    ),
    DashboardItem(
        TokensResources.idCreateReport,
        TokensResources.createReport,
        TokensResources.createReportDesc,
        Icons.Default.AddChart
    ),
    DashboardItem(
        TokensResources.idSearchReport,
        TokensResources.searchReport,
        TokensResources.searchReportDesc,
        Icons.Default.Analytics
    ),
    DashboardItem(
        TokensResources.idCreateInvoice,
        TokensResources.createInvoice,
        TokensResources.createInvoiceDesc,
        Icons.AutoMirrored.Filled.ReceiptLong
    ),
    DashboardItem(
        TokensResources.idSearchInvoice,
        TokensResources.searchInvoice,
        TokensResources.searchInvoiceDesc,
        Icons.Default.Search
    ),
)
