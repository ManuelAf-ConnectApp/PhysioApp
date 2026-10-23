package com.connectapp.physioapp.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.backup.create.CreateBackupScreen
import com.connectapp.presentation.backup.restore.BackupRestoredScreen
import com.connectapp.presentation.backup.restore.RestoreBackupScreen
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeDestination
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.presentation.invoices.detail.InvoiceDetailScreen
import com.connectapp.presentation.invoices.form.InvoiceFormScreen
import com.connectapp.presentation.invoices.search.InvoiceSearchScreen
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.onboarding.OnboardingScreen
import com.connectapp.presentation.patients.detail.PatientDetailScreen
import com.connectapp.presentation.patients.form.PatientFormScreen
import com.connectapp.presentation.patients.search.PatientSearchScreen
import com.connectapp.presentation.patients.sign.PatientSignScreen
import com.connectapp.presentation.professionals.detail.ProfessionalDetailScreen
import com.connectapp.presentation.professionals.form.ProfessionalFormScreen
import com.connectapp.presentation.professionals.search.ProfessionalSearchScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.reports.detail.ReportDetailScreen
import com.connectapp.presentation.reports.form.ReportFormScreen
import com.connectapp.presentation.reports.search.ReportSearchScreen
import com.connectapp.presentation.settings.SettingsDestination
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.settings.licenses.LicensesScreen
import com.connectapp.presentation.settings.password.ChangePasswordScreen
import com.connectapp.presentation.settings.recoverycode.RegenerateRecoveryCodeScreen
import com.connectapp.presentation.splash.SplashScreen

/**
 * Único punto del proyecto que importa las 8 `Screen` de feature a la vez y construye
 * el mapeo ruta -> pantalla (FR-004). Recrea, idéntico en contenido/lambdas de navegación,
 * el `when(key) { ... }` que antes vivía dentro de `NavigationScreen.kt` (`:presentation`).
 */
fun appEntryProvider(
    modifier: Modifier,
    viewModel: MainViewModel,
): (Any) -> NavEntry<out Any> = { key ->
    sectionEntry(key, modifier, viewModel) ?: when (key) {
        is NavigationRoute.SplashRoute -> {
            NavEntry(key = key) {
                SplashScreen(
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                    onNavigateToOnboarding = {
                        viewModel.updateNavigationState(NavigationRoute.OnboardingRoute)
                    },
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    }
                )
            }
        }

        is NavigationRoute.LoginRoute -> {
            NavEntry(key = key) {
                LoginScreen(
                    modifier = modifier,
                    onNavigateToRegister = {
                        viewModel.updateNavigationState(NavigationRoute.RegisterRoute)
                    },
                    onNavigateToForgotPassword = {
                        viewModel.updateNavigationState(NavigationRoute.ForgotPasswordRoute)
                    },
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    },
                    onNavigateToOnboarding = {
                        viewModel.updateNavigationState(NavigationRoute.OnboardingRoute)
                    },
                    onNavigateToRestoreBackup = {
                        viewModel.updateNavigationState(NavigationRoute.RestoreBackupRoute(fromSignIn = true))
                    },
                )
            }
        }

        is NavigationRoute.RegisterRoute -> {
            NavEntry(key = key) {
                RegisterScreen(
                    modifier = modifier,
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                )
            }
        }

        is NavigationRoute.OnboardingRoute -> {
            NavEntry(key = key) {
                OnboardingScreen(
                    modifier = modifier,
                    onNavigateToHome = {
                        viewModel.updateNavigationState(NavigationRoute.HomeRoute)
                    },
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                )
            }
        }

        is NavigationRoute.ForgotPasswordRoute -> {
            NavEntry(key = key) {
                ForgotPasswordScreen(
                    modifier = modifier,
                    onNavigateBack = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                )
            }
        }

        is NavigationRoute.HomeRoute -> {
            NavEntry(key = key) {
                HomeScreen(
                    modifier = modifier,
                    onNavigate = { destination ->
                        viewModel.updateNavigationState(destination.toRoute())
                    },
                    onOpenBackup = {
                        viewModel.updateNavigationState(NavigationRoute.CreateBackupRoute)
                    },
                )
            }
        }

        is NavigationRoute.SettingsRoute -> {
            NavEntry(key = key) {
                SettingsScreen(
                    modifier = modifier,
                    onLogout = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    },
                    onNavigate = { destination ->
                        viewModel.updateNavigationState(destination.toRoute())
                    },
                )
            }
        }

        is NavigationRoute.ChangePasswordRoute -> {
            NavEntry(key = key) {
                ChangePasswordScreen(
                    onDone = { viewModel.onBack() },
                    onNavigateToLogin = { viewModel.updateNavigationState(NavigationRoute.LoginRoute) },
                    modifier = modifier,
                )
            }
        }

        is NavigationRoute.LicensesRoute -> {
            NavEntry(key = key) {
                LicensesScreen(modifier = modifier)
            }
        }

        is NavigationRoute.RegenerateRecoveryCodeRoute -> {
            NavEntry(key = key) {
                RegenerateRecoveryCodeScreen(
                    onDone = { viewModel.onBack() },
                    onNavigateToLogin = { viewModel.updateNavigationState(NavigationRoute.LoginRoute) },
                    modifier = modifier,
                )
            }
        }

        is NavigationRoute.ProfileRoute -> {
            NavEntry(key = key) {
                ProfileScreen(
                    modifier = modifier,
                    onNavigateBack = {
                        viewModel.onBack()
                    },
                    onNavigateToLogin = {
                        viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                    }
                )
            }
        }

        // Every Home section has its screen since phase 9; an unknown route shows nothing.
        else -> NavEntry(key = key) {}
    }
}

/** Route of each Settings row. */
internal fun SettingsDestination.toRoute(): NavigationRoute = when (this) {
    SettingsDestination.CHANGE_PASSWORD -> NavigationRoute.ChangePasswordRoute
    SettingsDestination.RECOVERY_CODE -> NavigationRoute.RegenerateRecoveryCodeRoute
    SettingsDestination.CREATE_BACKUP -> NavigationRoute.CreateBackupRoute
    SettingsDestination.RESTORE_BACKUP -> NavigationRoute.RestoreBackupRoute()
    SettingsDestination.LICENSES -> NavigationRoute.LicensesRoute
}

/** Route of each Home card. */
internal fun HomeDestination.toRoute(): NavigationRoute = when (this) {
    HomeDestination.PATIENT_REGISTER -> NavigationRoute.PatientRegisterRoute
    HomeDestination.PATIENT_SEARCH -> NavigationRoute.PatientSearchRoute
    HomeDestination.PROFESSIONAL_REGISTER -> NavigationRoute.ProfessionalRegisterRoute
    HomeDestination.PROFESSIONAL_SEARCH -> NavigationRoute.ProfessionalSearchRoute
    HomeDestination.REPORT_CREATE -> NavigationRoute.ReportCreateRoute
    HomeDestination.REPORT_SEARCH -> NavigationRoute.ReportSearchRoute
    HomeDestination.INVOICE_CREATE -> NavigationRoute.InvoiceCreateRoute
    HomeDestination.INVOICE_SEARCH -> NavigationRoute.InvoiceSearchRoute
}

/** Phase 6: professionals. Null for any other route. */
private fun professionalEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? {
    val openDetail = { id: String -> viewModel.updateNavigationState(NavigationRoute.ProfessionalDetailRoute(id)) }
    val openEdit = { id: String -> viewModel.updateNavigationState(NavigationRoute.ProfessionalEditRoute(id)) }
    return when (key) {
        is NavigationRoute.ProfessionalRegisterRoute -> NavEntry(key = key) {
            ProfessionalFormScreen(
                professionalId = null,
                // The new record replaces the form, so back returns to Home.
                onSaved = { id -> viewModel.replaceCurrentRoute(NavigationRoute.ProfessionalDetailRoute(id)) },
                modifier = modifier,
            )
        }
        is NavigationRoute.ProfessionalSearchRoute -> NavEntry(key = key) {
            ProfessionalSearchScreen(onOpenDetail = openDetail, onEdit = openEdit, modifier = modifier)
        }
        is NavigationRoute.ProfessionalDetailRoute -> NavEntry(key = key) {
            ProfessionalDetailScreen(
                professionalId = key.professionalId,
                onEdit = openEdit,
                onDeleted = viewModel::onBack,
                modifier = modifier,
            )
        }
        is NavigationRoute.ProfessionalEditRoute -> NavEntry(key = key) {
            ProfessionalFormScreen(
                professionalId = key.professionalId,
                onSaved = { viewModel.onBack() },
                modifier = modifier,
            )
        }
        else -> null
    }
}

/** Phase 7: patients and their signed consent (D13). Null for any other route. */
private fun patientEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? {
    val openDetail = { id: String -> viewModel.updateNavigationState(NavigationRoute.PatientDetailRoute(id)) }
    val openEdit = { id: String -> viewModel.updateNavigationState(NavigationRoute.PatientEditRoute(id)) }
    val openSign = { id: String -> viewModel.updateNavigationState(NavigationRoute.PatientSignRoute(id)) }
    return when (key) {
        is NavigationRoute.PatientRegisterRoute -> NavEntry(key = key) {
            PatientFormScreen(
                patientId = null,
                // The new record replaces the form, so back returns to Home.
                onSaved = { id -> viewModel.replaceCurrentRoute(NavigationRoute.PatientDetailRoute(id)) },
                modifier = modifier,
            )
        }
        is NavigationRoute.PatientSearchRoute -> NavEntry(key = key) {
            PatientSearchScreen(
                onOpenDetail = openDetail,
                onEdit = openEdit,
                onSignAgain = openSign,
                modifier = modifier,
            )
        }
        is NavigationRoute.PatientDetailRoute -> NavEntry(key = key) {
            PatientDetailScreen(
                patientId = key.patientId,
                onEdit = openEdit,
                onSignAgain = openSign,
                onDeleted = viewModel::onBack,
                modifier = modifier,
            )
        }
        is NavigationRoute.PatientEditRoute -> NavEntry(key = key) {
            PatientFormScreen(patientId = key.patientId, onSaved = { viewModel.onBack() }, modifier = modifier)
        }
        is NavigationRoute.PatientSignRoute -> NavEntry(key = key) {
            PatientSignScreen(patientId = key.patientId, onSaved = viewModel::onBack, modifier = modifier)
        }
        else -> null
    }
}

/** Plan §21.7: backups, from Settings, Home's reminder and the sign-in screen. Null for any other route. */
private fun backupEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? = when (key) {
    is NavigationRoute.CreateBackupRoute -> NavEntry(key = key) {
        CreateBackupScreen(modifier = modifier)
    }
    is NavigationRoute.RestoreBackupRoute -> NavEntry(key = key) {
        RestoreBackupScreen(
            fromSignIn = key.fromSignIn,
            onRestored = { viewModel.updateNavigationState(NavigationRoute.BackupRestoredRoute) },
            onNavigateToLogin = { viewModel.updateNavigationState(NavigationRoute.LoginRoute) },
            modifier = modifier,
        )
    }
    is NavigationRoute.BackupRestoredRoute -> NavEntry(key = key) {
        BackupRestoredScreen(
            onGoToLogin = { viewModel.updateNavigationState(NavigationRoute.LoginRoute) },
            modifier = modifier,
        )
    }
    else -> null
}

/** The screens of the Home sections and of backups; null for any other route. */
private fun sectionEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? =
    backupEntry(key, modifier, viewModel)
        ?: professionalEntry(key, modifier, viewModel)
        ?: patientEntry(key, modifier, viewModel)
        ?: reportEntry(key, modifier, viewModel)
        ?: invoiceEntry(key, modifier, viewModel)

/** Phase 8: clinical reports (D17). Null for any other route. */
private fun reportEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? {
    val openEdit = { id: String -> viewModel.updateNavigationState(NavigationRoute.ReportEditRoute(id)) }
    return when (key) {
        is NavigationRoute.ReportCreateRoute -> NavEntry(key = key) {
            ReportFormScreen(
                reportId = null,
                // The new report replaces the form, so back returns to Home.
                onSaved = { id -> viewModel.replaceCurrentRoute(NavigationRoute.ReportDetailRoute(id)) },
                modifier = modifier,
            )
        }
        is NavigationRoute.ReportSearchRoute -> NavEntry(key = key) {
            ReportSearchScreen(
                onOpenDetail = { id -> viewModel.updateNavigationState(NavigationRoute.ReportDetailRoute(id)) },
                onEdit = openEdit,
                modifier = modifier,
            )
        }
        is NavigationRoute.ReportDetailRoute -> NavEntry(key = key) {
            ReportDetailScreen(reportId = key.reportId, onEdit = openEdit, modifier = modifier)
        }
        is NavigationRoute.ReportEditRoute -> NavEntry(key = key) {
            ReportFormScreen(reportId = key.reportId, onSaved = { viewModel.onBack() }, modifier = modifier)
        }
        else -> null
    }
}

/** Phase 9: payment receipts (D6). Null for any other route. */
private fun invoiceEntry(key: Any, modifier: Modifier, viewModel: MainViewModel): NavEntry<out Any>? = when (key) {
    is NavigationRoute.InvoiceCreateRoute -> NavEntry(key = key) {
        InvoiceFormScreen(
            // The new receipt replaces the form, so back returns to Home.
            onSaved = { id, export ->
                viewModel.replaceCurrentRoute(NavigationRoute.InvoiceDetailRoute(id, exportOnOpen = export))
            },
            modifier = modifier,
        )
    }
    is NavigationRoute.InvoiceSearchRoute -> NavEntry(key = key) {
        InvoiceSearchScreen(
            onOpenDetail = { id -> viewModel.updateNavigationState(NavigationRoute.InvoiceDetailRoute(id)) },
            modifier = modifier,
        )
    }
    is NavigationRoute.InvoiceDetailRoute -> NavEntry(key = key) {
        InvoiceDetailScreen(invoiceId = key.invoiceId, exportOnOpen = key.exportOnOpen, modifier = modifier)
    }
    else -> null
}
