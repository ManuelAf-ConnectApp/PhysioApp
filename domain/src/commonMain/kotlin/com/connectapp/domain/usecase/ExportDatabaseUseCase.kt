package com.connectapp.domain.usecase

import com.connectapp.domain.database.DatabaseTransfer

class ExportDatabaseUseCase(
    private val transfer: DatabaseTransfer
) {
    operator fun invoke(databaseName: String): String? {
        return transfer.exportDataBase(
            databaseName = databaseName
        )
    }
}