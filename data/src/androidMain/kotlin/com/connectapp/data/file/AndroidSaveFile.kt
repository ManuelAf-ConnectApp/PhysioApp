package com.connectapp.data.file

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class AndroidSaveFile(private val context: Context) : SaveFile {

    override fun saveAndExport(fileName: String, bytes: ByteArray): Boolean {
        return try {
            val nameWithExtension = if (fileName.endsWith(".pdf", ignoreCase = true)) {
                fileName
            } else {
                "$fileName.pdf"
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveWithMediaStore(nameWithExtension, bytes)
            } else {
                saveWithLegacyFileApi(nameWithExtension, bytes)
            }
        } catch (_: Exception) {
            false
        }
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveWithMediaStore(fileName: String, bytes: ByteArray): Boolean {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val uri = resolver.insert(collection, contentValues) ?: return false

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(bytes)
            }
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)
            true
        } catch (_: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }

    private fun saveWithLegacyFileApi(fileName: String, bytes: ByteArray): Boolean {
        @Suppress("DEPRECATION")
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            return false
        }
        val file = File(downloadsDir, fileName)
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(bytes)
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun notifyOrShareFile(fileName: String) {
        val nameWithExtension = if (fileName.endsWith(".pdf", ignoreCase = true)) {
            fileName
        } else {
            "$fileName.pdf"
        }

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getFileUriFromMediaStore(nameWithExtension)
        } else {
            getLegacyFileUri(nameWithExtension)
        } ?: return

        // 2. Crear el Intent para abrir el PDF
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        // 3. Crear el PendingIntent (lo que se ejecuta al tocar la notificación)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Verificar permisos en Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!notificationManager.areNotificationsEnabled()) {
                // Si no hay permisos, al menos intentamos abrir el archivo directamente (opcional)
                return
            }
        }

        val channelId = "pdf_downloads"

        // 4. Crear el canal de notificaciones (Obligatorio en Android 8+)
        val channel = NotificationChannel(
            channelId,
            "Descargas de PDF",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)

        // 5. Construir y lanzar la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) // Icono por defecto de Android
            .setContentTitle("Descarga completada")
            .setContentText(nameWithExtension)
            .setAutoCancel(true) // Se borra al tocarla
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(nameWithExtension.hashCode(), notification)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getFileUriFromMediaStore(fileName: String): android.net.Uri? {
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)

        return context.contentResolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                android.content.ContentUris.withAppendedId(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    id
                )
            } else null
        }
    }

    private fun getLegacyFileUri(fileName: String): android.net.Uri? {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        return if (file.exists()) {
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } else null
    }
}
