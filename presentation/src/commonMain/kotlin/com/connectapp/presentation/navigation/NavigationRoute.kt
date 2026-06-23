package com.connectapp.presentation.navigation


import com.connectapp.domain.model.User
import kotlinx.serialization.Serializable

sealed interface NavigationRoute {
    @Serializable
    data object SplashRoute : NavigationRoute

    @Serializable
    data class HomeRoute(val user: User) : NavigationRoute

    @Serializable
    data object LoginRoute : NavigationRoute

    @Serializable
    data object RegisterRoute : NavigationRoute

    @Serializable
    data object ForgotPasswordRoute : NavigationRoute

    @Serializable
    data class PatientRoute(val user: User) : NavigationRoute

    @Serializable
    data class EditPatientRoute(val patientDni: String) : NavigationRoute

    @Serializable
    data object ProfessionalRoute : NavigationRoute

    @Serializable
    data class InvoiceRoute(val invoiceId: String? = null) : NavigationRoute

    data class EditInvoiceRoute(val invoiceId: String) : NavigationRoute

    @Serializable
    data object SearchPatientRoute : NavigationRoute

    @Serializable
    data object SearchProfessionalRoute : NavigationRoute

    @Serializable
    data object SearchInvoiceRoute : NavigationRoute

    @Serializable
    data object SearchReportRoute : NavigationRoute

    @Serializable
    data class ReportRoute(val reportId: String? = null) : NavigationRoute

    data object SettingsRoute : NavigationRoute

    data object ProfileRoute : NavigationRoute

    val showBackButton: Boolean get() = this !is HomeRoute
    val showTopBar: Boolean get() = this !is SplashRoute && this !is LoginRoute
    val showBottomBar: Boolean
        get() = this !is SplashRoute && this !is LoginRoute && this !is RegisterRoute && this !is ForgotPasswordRoute && this !is PatientRoute && this !is EditPatientRoute && this !is ProfessionalRoute && this !is InvoiceRoute && this !is ReportRoute
}
