package com.connectapp.presentation.backup.di

import com.connectapp.presentation.backup.create.CreateBackupViewModel
import com.connectapp.presentation.backup.restore.RestoreBackupViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val backupModule = module {
    viewModelOf(::CreateBackupViewModel)
    viewModel { (fromSignIn: Boolean) -> RestoreBackupViewModel(fromSignIn, get()) }
}
