package com.connectapp.domain.usecase

import com.connectapp.domain.database.DatabaseTransfer

class ImportDatabaseUseCase(
    private val transfer: DatabaseTransfer
) {
    operator fun invoke(sourceFilePath: String, databaseName: String): Boolean {
        return transfer.importDataBase(
            sourceFilePath = sourceFilePath,
            databaseName = databaseName
        )
    }
}