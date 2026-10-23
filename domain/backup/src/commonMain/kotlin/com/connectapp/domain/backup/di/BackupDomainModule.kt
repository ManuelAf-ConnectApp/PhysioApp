package com.connectapp.domain.backup.di

import com.connectapp.domain.backup.CreateBackupUseCase
import com.connectapp.domain.backup.MarkBackupSavedUseCase
import com.connectapp.domain.backup.ObserveBackupReminderUseCase
import com.connectapp.domain.backup.ObserveLastBackupUseCase
import com.connectapp.domain.backup.RestoreBackupUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/** The [com.connectapp.domain.backup.BackupRepository] comes from :data:backup. */
val backupDomainModule = module {
    factoryOf(::CreateBackupUseCase)
    factoryOf(::MarkBackupSavedUseCase)
    factoryOf(::RestoreBackupUseCase)
    factoryOf(::ObserveLastBackupUseCase)
    factoryOf(::ObserveBackupReminderUseCase)
}
