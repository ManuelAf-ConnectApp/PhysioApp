package com.connectapp.domain.database

interface DatabaseTransfer {

    fun exportDataBase(databaseName: String): String?

    fun importDataBase(sourceFilePath: String, databaseName: String): Boolean

}