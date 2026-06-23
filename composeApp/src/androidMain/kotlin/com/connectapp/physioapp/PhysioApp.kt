package com.connectapp.physioapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class PhysioApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            allowOverride(true)
            androidContext(this@PhysioApp)
            modules(appModules())
        }
    }
}
