package com.connectapp.physioapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.connectapp.commonresources.theme.ConnectAppTheme
import com.connectapp.physioapp.navigation.appEntryProvider
import com.connectapp.presentation.MainScreen
import com.connectapp.presentation.MainViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.context.startKoin

@Composable
fun App() {
    // startKoin must run before the first koinViewModel() call below.
    startKoin {
        modules(appModules)
    }

    val viewModel: MainViewModel = koinViewModel()

    // The theme follows the OS light/dark setting: an in-app appearance preference is not a
    // confirmed requirement (Ajustes pendiente de definición).
    ConnectAppTheme(darkTheme = isSystemInDarkTheme()) {
        MainScreen(
            entryProvider = appEntryProvider(modifier = Modifier, viewModel = viewModel)
        )
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
