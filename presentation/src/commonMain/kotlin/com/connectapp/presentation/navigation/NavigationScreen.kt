/*
Author: Manuel María Alconchel Fernández
E-mail: incidencias@connectapp.es
Date: 30/06/2006

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.connectapp.presentation.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.connectapp.presentation.MainViewModel
import com.connectapp.presentation.platform
import com.connectapp.presentation.edit_invoice.EditInvoiceScreen
import com.connectapp.presentation.edit_patient.EditPatientScreen
import com.connectapp.presentation.forgot_password.ForgotPasswordScreen
import com.connectapp.presentation.home.HomeScreen
import com.connectapp.presentation.invoice.InvoiceScreen
import com.connectapp.presentation.login.LoginScreen
import com.connectapp.presentation.patient.PatientScreen
import com.connectapp.presentation.professional.ProfessionalScreen
import com.connectapp.presentation.profile.ProfileScreen
import com.connectapp.presentation.register.RegisterScreen
import com.connectapp.presentation.report.ReportScreen
import com.connectapp.presentation.search_invoice.SearchInvoiceScreen
import com.connectapp.presentation.search_patient.SearchPatientScreen
import com.connectapp.presentation.search_professional.SearchProfessionalScreen
import com.connectapp.presentation.search_report.SearchReportScreen
import com.connectapp.presentation.settings.SettingsScreen
import com.connectapp.presentation.splash.SplashScreen


@Composable
fun NavigationScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    windowSizeClass: WindowSizeClass,
    snackBarHostState: SnackbarHostState,
) {
    val backStack = remember { mutableStateListOf<Any>(NavigationRoute.SplashRoute) }

    val navState by viewModel.navState.collectAsState()

    LaunchedEffect(navState.route) {
        val newRoute = navState.route
        if (backStack.lastOrNull() != newRoute) {
            if (newRoute is NavigationRoute.HomeRoute || newRoute == NavigationRoute.SplashRoute) {
                backStack.clear()
                backStack.add(newRoute)
            } else {
                backStack.add(newRoute)
            }
        }
    }

    val isIos = platform() == "iOS"

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLast()
                val previousRoute = backStack.last() as NavigationRoute
                viewModel.updateNavigationState(previousRoute)
            }
        },
        transitionSpec = {
            if (isIos) {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(500)
                )
            } else {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        },
        popTransitionSpec = {
            if (isIos) {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(500)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(500)
                )
            } else {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            }
        },
        entryProvider = { key ->
            when (key) {
                is NavigationRoute.SplashRoute -> {
                    NavEntry(key = key) {
                        SplashScreen {
                            viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                        }
                    }
                }

                is NavigationRoute.LoginRoute -> {
                    NavEntry(key = key) {
                        LoginScreen(
                            modifier = modifier,
                            windowSizeClass = windowSizeClass,
                            snackBarHostState = snackBarHostState,
                            onNavigateToRegister = {
                                viewModel.updateNavigationState(NavigationRoute.RegisterRoute)
                            },
                            onNavigateToForgotPassword = {
                                viewModel.updateNavigationState(NavigationRoute.ForgotPasswordRoute)
                            },
                            onNavigateToHome = { user ->
                                viewModel.updateNavigationState(NavigationRoute.HomeRoute(user))
                            }
                        )
                    }
                }

                is NavigationRoute.RegisterRoute -> {
                    NavEntry(key = key) {
                        RegisterScreen(
                            modifier = modifier,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            }
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
                            user = key.user,
                            onNavigateToPatient = {
                                viewModel.updateNavigationState(NavigationRoute.PatientRoute(user = key.user))
                            },
                            onNavigateToSearchPatient = {
                                viewModel.updateNavigationState(NavigationRoute.SearchPatientRoute)
                            },
                            onNavigateToProfessional = {
                                viewModel.updateNavigationState(NavigationRoute.ProfessionalRoute)
                            },
                            onNavigateToSearchProfessional = {
                                viewModel.updateNavigationState(NavigationRoute.SearchProfessionalRoute)
                            },
                            onNavigateToInvoice = {
                                viewModel.updateNavigationState(NavigationRoute.InvoiceRoute())
                            },
                            onNavigateToSearchInvoice = {
                                viewModel.updateNavigationState(NavigationRoute.SearchInvoiceRoute)
                            },
                            onNavigateToCreateReport = {
                                viewModel.updateNavigationState(NavigationRoute.ReportRoute())
                            },
                            onNavigateToSearchReport = {
                                viewModel.updateNavigationState(NavigationRoute.SearchReportRoute)
                            }
                        )
                    }
                }

                is NavigationRoute.PatientRoute -> {
                    NavEntry(key = key) {
                        PatientScreen(
                            modifier = modifier,
                            user = key.user,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            }
                        )
                    }
                }

                is NavigationRoute.EditPatientRoute -> {
                    NavEntry(key = key) {
                        EditPatientScreen(
                            modifier = modifier,
                            patientDni = key.patientDni,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            }
                        )
                    }
                }

                is NavigationRoute.ProfessionalRoute -> {
                    NavEntry(key = key) {
                        ProfessionalScreen(
                            modifier = modifier,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            }
                        )
                    }
                }

                is NavigationRoute.InvoiceRoute -> {
                    NavEntry(key = key) {
                        InvoiceScreen(
                            modifier = modifier,
                            invoiceId = key.invoiceId,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            },
                            onNavigateToCreatePatient = {
                                viewModel.navState.value.user?.let { user ->
                                    viewModel.updateNavigationState(NavigationRoute.PatientRoute(user))
                                }
                            }
                        )
                    }
                }

                is NavigationRoute.SearchReportRoute -> {
                    NavEntry(key = key) {
                        SearchReportScreen(
                            modifier = modifier,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            }
                        )
                    }
                }

                is NavigationRoute.SettingsRoute -> {
                    NavEntry(key = key) {
                        SettingsScreen(
                            modifier = modifier,
                            snackBarHostState = snackBarHostState,
                            onLogoutSuccess = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            }
                        )
                    }
                }

                is NavigationRoute.SearchInvoiceRoute -> {
                    NavEntry(key = key) {
                        SearchInvoiceScreen(
                            modifier = modifier,
                            snackBarHostState = snackBarHostState,
                        ) {
                            viewModel.updateNavigationState(NavigationRoute.InvoiceRoute())
                        }
                    }
                }

                is NavigationRoute.ProfileRoute -> {
                    NavEntry(key = key) {
                        ProfileScreen(
                            modifier = modifier,
                            onNavigateToLogin = {
                                viewModel.updateNavigationState(NavigationRoute.LoginRoute)
                            }
                        )
                    }
                }

                is NavigationRoute.ReportRoute -> {
                    NavEntry(key = key) {
                        ReportScreen(
                            modifier = modifier,
                            reportId = key.reportId,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            },
                            onNavigateToCreatePatient = {
                                viewModel.navState.value.user?.let { user ->
                                    viewModel.updateNavigationState(NavigationRoute.PatientRoute(user))
                                }
                            }
                        )

                    }
                }

                is NavigationRoute.SearchPatientRoute -> {
                    NavEntry(key = key) {
                        SearchPatientScreen(
                            modifier = modifier,
                            onNavigateToEditPatient = { dni: String ->
                                viewModel.updateNavigationState(NavigationRoute.EditPatientRoute(dni))
                            }
                        )
                    }
                }

                is NavigationRoute.SearchProfessionalRoute -> {
                    NavEntry(key = key) {
                        SearchProfessionalScreen(
                            modifier = modifier,
                        )
                    }
                }

                is NavigationRoute.EditInvoiceRoute -> {
                    NavEntry(key = key) {
                        EditInvoiceScreen(
                            modifier = modifier,
                            invoiceId = key.invoiceId,
                            snackBarHostState = snackBarHostState,
                            onNavigateBack = {
                                viewModel.onBack()
                            }
                        )
                    }
                }

                else -> {
                    NavEntry(key = key) {

                    }
                }
            }

        })

}