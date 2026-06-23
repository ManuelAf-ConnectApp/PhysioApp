package com.connectapp.data.database

import com.connectapp.domain.database.DatabaseTransfer
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
class IOSDatabaseTransfer : DatabaseTransfer {
    
    override fun exportDataBase(databaseName: String): String? {
        val fileManager = NSFileManager.defaultManager

        // Localizar el directorio de soporte de la aplicación (donde SQLDelight guarda la DB por defecto)
        val appSupportDir = fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: return null

        // Directorio de documentos (donde el usuario puede ver el archivo si File Sharing está activado)
        val documentsDir = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: return null

        val originalDbPath = appSupportDir.path + "/$databaseName"
        val exportedFilePath = documentsDir.path + "/$databaseName"

        try {
            // 1. Intentar copiar el archivo principal
            if (fileManager.fileExistsAtPath(originalDbPath)) {
                if (fileManager.fileExistsAtPath(exportedFilePath)) {
                    fileManager.removeItemAtPath(exportedFilePath, null)
                }
                fileManager.copyItemAtPath(originalDbPath, exportedFilePath, null)
            } else {
                // Si no está en AppSupport, quizás esté en Documents (depende de la configuración del Driver)
                val alternativePath = documentsDir.path + "/$databaseName"
                if (originalDbPath != alternativePath && fileManager.fileExistsAtPath(alternativePath)) {
                    // Ya está en documentos, devolvemos esa ruta
                    return alternativePath
                }
                return null
            }

            // 2. Intentar copiar archivos auxiliares de SQLite (-wal, -shm)
            val suffixes = listOf("-wal", "-shm")
            suffixes.forEach { suffix ->
                val srcAux = originalDbPath + suffix
                val destAux = exportedFilePath + suffix
                if (fileManager.fileExistsAtPath(srcAux)) {
                    if (fileManager.fileExistsAtPath(destAux)) {
                        fileManager.removeItemAtPath(destAux, null)
                    }
                    fileManager.copyItemAtPath(srcAux, destAux, null)
                }
            }

            return exportedFilePath

        } catch (e: Exception) {
            println("Error exportando DB en iOS: ${e.message}")
            return null
        }
    }

    override fun importDataBase(
        sourceFilePath: String,
        databaseName: String
    ): Boolean {
        val fileManager = NSFileManager.defaultManager

        val appSupportDir = fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null
        ) ?: return false

        val destDbPath = appSupportDir.path + "/$databaseName"

        try {
            if (!fileManager.fileExistsAtPath(sourceFilePath)) return false

            // 1. Eliminar la base de datos actual y sus archivos auxiliares
            val pathsToDelete = listOf(destDbPath, "$destDbPath-wal", "$destDbPath-shm")
            pathsToDelete.forEach { path ->
                if (fileManager.fileExistsAtPath(path)) {
                    fileManager.removeItemAtPath(path, null)
                }
            }

            // 2. Copiar el archivo importado a la ruta interna de la app
            fileManager.copyItemAtPath(sourceFilePath, destDbPath, null)
            
            // Nota: Normalmente el import viene de un archivo .db único sin -wal.
            // SQLite lo recreará al abrirse.
            
            return true

        } catch (e: Exception) {
            println("Error importando DB en iOS: ${e.message}")
            return false
        }
    }
}
