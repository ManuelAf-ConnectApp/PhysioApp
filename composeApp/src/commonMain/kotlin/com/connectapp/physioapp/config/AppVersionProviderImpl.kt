package com.connectapp.physioapp.config

import com.connectapp.domain.provider.AppVersionProvider

/** Backs [AppVersionProvider] with the build-time-generated [AppConfig.VERSION_NAME]. */
internal class AppVersionProviderImpl : AppVersionProvider {
    override val versionName: String = AppConfig.VERSION_NAME
}
