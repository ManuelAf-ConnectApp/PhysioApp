package com.connectapp.presentation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.connectapp.presentation.component.CustomBottomBar
import com.connectapp.presentation.component.CustomNavigationRail
import com.connectapp.presentation.component.CustomTopBar
import com.connectapp.presentation.navigation.NavigationRoute
import com.connectapp.presentation.navigation.NavigationScreen
import com.connectapp.presentation.util.NavigationType
import com.connectapp.presentation.util.getNavigationType
import com.connectapp.designresources.TokensResources
import com.connectapp.designresources.DimensResources
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainScreen(
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
) {
    val viewModel: MainViewModel = koinViewModel()
    val navState by viewModel.navState.collectAsStateWithLifecycle()
    val navigationType = getNavigationType(windowSizeClass.widthSizeClass)
    val snackBarHostState = remember { SnackbarHostState() }

    val currentRoute = navState.route

    Row(modifier = Modifier.fillMaxSize()) {
        if (currentRoute.showBottomBar && navigationType != NavigationType.BOTTOM_NAVIGATION) {
            CustomNavigationRail(
                currentRoute = currentRoute,
                user = navState.user,
                onNavigate = { viewModel.updateNavigationState(it) }
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            snackbarHost = { SnackbarHost(snackBarHostState) },
            topBar = {
                if (currentRoute.showTopBar) {
                    CustomTopBar(
                        showBackButton = currentRoute.showBackButton,
                        title = when (currentRoute) {
                            is NavigationRoute.SettingsRoute -> stringResource(TokensResources.settings)
                            is NavigationRoute.ProfileRoute -> stringResource(TokensResources.profile)
                            is NavigationRoute.HomeRoute -> stringResource(TokensResources.appName)
                            is NavigationRoute.PatientRoute -> stringResource(TokensResources.patient)
                            is NavigationRoute.ProfessionalRoute -> stringResource(TokensResources.professional)
                            is NavigationRoute.InvoiceRoute -> stringResource(TokensResources.invoice)
                            is NavigationRoute.ReportRoute -> if (currentRoute.reportId != null) stringResource(
                                TokensResources.editReport
                            ) else stringResource(TokensResources.newReport)

                            is NavigationRoute.SearchPatientRoute -> stringResource(TokensResources.searchPatient)
                            is NavigationRoute.SearchProfessionalRoute -> stringResource(
                                TokensResources.searchProfessional
                            )

                            else -> stringResource(TokensResources.appName)
                        },
                        onNavigationClick = {
                            viewModel.onBack()
                        },
                    )
                }
            },
            bottomBar = {
                if (currentRoute.showBottomBar && navigationType == NavigationType.BOTTOM_NAVIGATION) {
                    CustomBottomBar(
                        currentRoute = currentRoute,
                        user = navState.user,
                        onNavigate = { viewModel.updateNavigationState(it) }
                    )
                }
            }
        ) { paddingValues ->
            NavigationScreen(
                modifier = modifier.padding(paddingValues).imePadding().padding(horizontal = DimensResources.spacing16),
                viewModel = viewModel,
                windowSizeClass = windowSizeClass,
                snackBarHostState = snackBarHostState
            )
        }
    }
}
