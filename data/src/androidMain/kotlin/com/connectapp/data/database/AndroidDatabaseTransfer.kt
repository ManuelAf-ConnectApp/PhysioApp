package com.connectapp.data.database

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.connectapp.data.storage.CryptoManager
import com.connectapp.domain.database.DatabaseTransfer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class AndroidDatabaseTransfer(
    val context: Context,
    private val cryptoManager: CryptoManager
) : DatabaseTransfer {
    override fun exportDataBase(databaseName: String): String? {
        try {
            // Buscamos el archivo de la base de datos.
            var dbFile = context.getDatabasePath(databaseName)
            if (!dbFile.exists()) {
                val dbFileWithExt = context.getDatabasePath("$databaseName.db")
                if (dbFileWithExt.exists()) {
                    dbFile = dbFileWithExt
                } else {
                    return null
                }
            }

            val fileName = dbFile.name

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Para Android 10+ usamos MediaStore para guardar en Descargas
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return null

                resolver.openOutputStream(uri).use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }

                // Intentamos copiar también archivos auxiliares si existen, aunque en MediaStore
                // aparecerán como archivos separados en la carpeta de Descargas.
                val suffixes = listOf("-wal", "-shm", "-journal")
                suffixes.forEach { suffix ->
                    val auxFile = File(dbFile.path + suffix)
                    if (auxFile.exists()) {
                        val auxValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + suffix)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }
                        val auxUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, auxValues)
                        auxUri?.let {
                            resolver.openOutputStream(it).use { out ->
                                auxFile.inputStream().use { inp -> inp.copyTo(out!!) }
                            }
                        }
                    }
                }

                return "Carpeta de Descargas ($fileName)"
            } else {
                // Para versiones anteriores a Android 10
                val exportDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!exportDir.exists() && !exportDir.mkdirs()) return null

                val exportedFile = File(exportDir, fileName)
                dbFile.copyTo(exportedFile, overwrite = true)

                val suffixes = listOf("-wal", "-shm", "-journal")
                suffixes.forEach { suffix ->
                    val auxFile = File(dbFile.path + suffix)
                    if (auxFile.exists()) {
                        auxFile.copyTo(File(exportDir, fileName + suffix), overwrite = true)
                    }
                }

                return exportedFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun importDataBase(
        sourceFilePath: String,
        databaseName: String
    ): Boolean {
        try {
            val sourceFile = if (sourceFilePath.startsWith("content://")) {
                copyContentUriToTempFile(sourceFilePath) ?: return false
            } else {
                File(sourceFilePath)
            }

            if (!sourceFile.exists()) return false

            // Ruta de la base de datos activa de la app
            // Buscamos cuál es el archivo real que se está usando
            var destFile = context.getDatabasePath(databaseName)
            if (!destFile.exists()) {
                val destFileWithExt = context.getDatabasePath("$databaseName.db")
                // Si el archivo con .db existe, lo usamos como destino
                if (destFileWithExt.exists()) {
                    destFile = destFileWithExt
                }
            }

            // 1. Limpiar los archivos Journal/WAL antiguos del destino si existen.
            val suffixes = listOf("-wal", "-shm", "-journal")
            suffixes.forEach { suffix ->
                val auxFile = File(destFile.path + suffix)
                if (auxFile.exists()) auxFile.delete()
            }

            // 2. Copiar el nuevo archivo sobre el original
            sourceFile.copyTo(destFile, overwrite = true)
            
            // Si el archivo origen tenía archivos auxiliares (y no es un content:// copiado a temp), 
            // también deberíamos intentar traerlos? 
            // Normalmente para un import simple desde un archivo .db, el WAL no es necesario
            // si la DB se cerró limpiamente.
            
            // Si era un archivo temporal, lo borramos
            if (sourceFilePath.startsWith("content://")) {
                sourceFile.delete()
            }
            
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    private fun copyContentUriToTempFile(uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("import_db", ".db", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
