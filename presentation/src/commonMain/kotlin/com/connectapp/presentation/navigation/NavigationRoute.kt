package com.connectapp.presentation.navigation

import com.connectapp.commonresources.*
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

sealed interface NavigationRoute {
    @Serializable
    data object SplashRoute : NavigationRoute

    @Serializable
    data object HomeRoute : NavigationRoute

    @Serializable
    data object LoginRoute : NavigationRoute

    @Serializable
    data object RegisterRoute : NavigationRoute

    @Serializable
    data object ForgotPasswordRoute : NavigationRoute

    /** Mandatory professional profile after the first login (D3). */
    @Serializable
    data object OnboardingRoute : NavigationRoute

    @Serializable
    data object SettingsRoute : NavigationRoute

    @Serializable
    data object ProfileRoute : NavigationRoute

    /** Settings → change password (D15). */
    @Serializable
    data object ChangePasswordRoute : NavigationRoute

    /** Settings → regenerate recovery code (D15). */
    @Serializable
    data object RegenerateRecoveryCodeRoute : NavigationRoute

    /** Settings → open-source licences. */
    @Serializable
    data object LicensesRoute : NavigationRoute

    /** Settings → create backup (plan §21.7), also opened from Home's reminder. */
    @Serializable
    data object CreateBackupRoute : NavigationRoute

    /**
     * Restore a backup (plan §21.7): from Settings, or [fromSignIn] from the sign-in screen of a
     * tablet without accounts, outside the authenticated area.
     */
    @Serializable
    data class RestoreBackupRoute(val fromSignIn: Boolean = false) : NavigationRoute

    /** After a restore: any session has ended, so it starts a new back stack like Login. */
    @Serializable
    data object BackupRestoredRoute : NavigationRoute

    // The eight Home sections. Phases 6–9 replace their placeholder screens.
    @Serializable
    data object PatientRegisterRoute : NavigationRoute

    @Serializable
    data object PatientSearchRoute : NavigationRoute

    @Serializable
    data object ProfessionalRegisterRoute : NavigationRoute

    @Serializable
    data object ProfessionalSearchRoute : NavigationRoute

    @Serializable
    data object ReportCreateRoute : NavigationRoute

    @Serializable
    data object ReportSearchRoute : NavigationRoute

    @Serializable
    data object InvoiceCreateRoute : NavigationRoute

    @Serializable
    data object InvoiceSearchRoute : NavigationRoute

    /** Professional record opened from the search when list and detail do not fit side by side. */
    @Serializable
    data class ProfessionalDetailRoute(val professionalId: String) : NavigationRoute

    @Serializable
    data class ProfessionalEditRoute(val professionalId: String) : NavigationRoute

    @Serializable
    data class PatientDetailRoute(val patientId: String) : NavigationRoute

    @Serializable
    data class PatientEditRoute(val patientId: String) : NavigationRoute

    @Serializable
    data class ReportDetailRoute(val reportId: String) : NavigationRoute

    @Serializable
    data class ReportEditRoute(val reportId: String) : NavigationRoute

    /** A payment receipt (D6); with [exportOnOpen] ("Guardar y exportar PDF") the export dialog opens at once. */
    @Serializable
    data class InvoiceDetailRoute(val invoiceId: String, val exportOnOpen: Boolean = false) : NavigationRoute

    /** "Volver a firmar" the consent (D13). */
    @Serializable
    data class PatientSignRoute(val patientId: String) : NavigationRoute

    /** Destinations of the Inicio/Perfil/Ajustes bottom bar. */
    val isBottomBarDestination: Boolean
        get() = this is HomeRoute || this is ProfileRoute || this is SettingsRoute

    /** Screens reached from Home; they belong to the "Inicio" tab of the bottom bar. */
    val isHomeSection: Boolean
        get() = this in homeSections

    /** A Home section or a screen opened from one (detail, edit). */
    val isSectionScreen: Boolean
        get() = isHomeSection ||
            this is ProfessionalDetailRoute ||
            this is ProfessionalEditRoute ||
            this is PatientDetailRoute ||
            this is PatientEditRoute ||
            this is PatientSignRoute ||
            this is ReportDetailRoute ||
            this is ReportEditRoute ||
            this is InvoiceDetailRoute

    /** A screen opened from Ajustes; it keeps the "Ajustes" tab selected. */
    val isSettingsScreen: Boolean
        get() = this is ChangePasswordRoute ||
            this is RegenerateRecoveryCodeRoute ||
            this is LicensesRoute ||
            this is CreateBackupRoute ||
            (this is RestoreBackupRoute && !fromSignIn)

    /**
     * Splash, login and onboarding show no navigation bars; the
     * authenticated area shows the top bar and the Inicio/Perfil/Ajustes bottom bar, also on the
     * sections reached from Home ("sin perder acceso claro a la navegación principal").
     */
    val isAuthenticatedArea: Boolean
        get() = isBottomBarDestination || isSectionScreen || isSettingsScreen

    /**
     * Entering a root route clears the back stack, so system back can never return to a screen
     * of a different session state (e.g. Home after logging out, or Login after signing in).
     */
    val isRoot: Boolean
        get() = this is SplashRoute ||
            this is LoginRoute ||
            this is OnboardingRoute ||
            this is HomeRoute ||
            this is BackupRestoredRoute

    /**
     * Back arrow in the top bar (pattern agreed on 2026-10-20): every stacked screen that is not a
     * bottom bar destination — register and forgot password (agreed on 2026-10-19) and the Home
     * sections. iPad has no system back button, so these screens would otherwise be dead ends.
     */
    val showTopBarBackArrow: Boolean
        get() = this is RegisterRoute ||
            this is ForgotPasswordRoute ||
            this is RestoreBackupRoute ||
            isSectionScreen ||
            isSettingsScreen

    val showTopBar: Boolean get() = isAuthenticatedArea || showTopBarBackArrow
    val showBottomBar: Boolean get() = isAuthenticatedArea

    /** Bottom bar item highlighted while this route is shown. */
    val bottomBarDestination: NavigationRoute?
        get() = when {
            isBottomBarDestination -> this
            isSectionScreen -> HomeRoute
            isSettingsScreen -> SettingsRoute
            else -> null
        }

    val titleRes: StringResource
        get() = when (this) {
            SplashRoute -> main_title
            HomeRoute -> main_title
            LoginRoute -> login_title
            RegisterRoute -> register_title
            ForgotPasswordRoute -> forgot_password_title
            OnboardingRoute -> onboarding_title
            SettingsRoute -> settings
            ProfileRoute -> nav_profile
            ChangePasswordRoute -> settings_item_change_password
            RegenerateRecoveryCodeRoute -> settings_item_recovery_code
            LicensesRoute -> settings_item_licenses
            CreateBackupRoute -> settings_item_backup_create
            is RestoreBackupRoute -> settings_item_backup_restore
            BackupRestoredRoute -> backup_restored_title
            PatientRegisterRoute -> home_patient_register_title
            PatientSearchRoute -> home_patient_search_title
            ProfessionalRegisterRoute -> home_professional_register_title
            ProfessionalSearchRoute -> home_professional_search_title
            ReportCreateRoute -> home_report_create_title
            ReportSearchRoute -> home_report_search_title
            InvoiceCreateRoute -> home_invoice_create_title
            InvoiceSearchRoute -> home_invoice_search_title
            is ProfessionalDetailRoute -> professional_detail_title
            is ProfessionalEditRoute -> professional_edit_title
            is PatientDetailRoute -> patient_detail_title
            is PatientEditRoute -> patient_edit_title
            is PatientSignRoute -> patient_sign_title
            is ReportDetailRoute -> report_detail_title
            is ReportEditRoute -> report_edit_title
            is InvoiceDetailRoute -> invoice_detail_title
        }

    companion object {
        val homeSections: List<NavigationRoute> = listOf(
            PatientRegisterRoute,
            PatientSearchRoute,
            ProfessionalRegisterRoute,
            ProfessionalSearchRoute,
            ReportCreateRoute,
            ReportSearchRoute,
            InvoiceCreateRoute,
            InvoiceSearchRoute,
        )
    }
}
