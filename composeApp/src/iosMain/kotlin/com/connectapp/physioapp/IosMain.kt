package com.connectapp.physioapp

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.ComposeUIViewController
import com.connectapp.data.storage.CryptoManager
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.UIKit.UIViewController

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun MainViewController(): UIViewController = ComposeUIViewController {
    val windowSizeClass = calculateWindowSizeClass()
    App(windowSizeClass = windowSizeClass)
}

fun startKoinIos() {
    startKoin {
        modules(appModules())
    }
}
