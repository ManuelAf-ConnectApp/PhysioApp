package com.connectapp.commonresources.signature

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import java.io.ByteArrayOutputStream

private const val PNG_QUALITY = 100 // Ignored for PNG (lossless), required by the API.

internal actual fun ImageBitmap.encodeToPng(): ByteArray =
    ByteArrayOutputStream().use { output ->
        asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, output)
        output.toByteArray()
    }
