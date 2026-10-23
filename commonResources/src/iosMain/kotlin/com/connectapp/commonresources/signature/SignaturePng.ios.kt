package com.connectapp.commonresources.signature

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

internal actual fun ImageBitmap.encodeToPng(): ByteArray =
    checkNotNull(Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)) {
        "PNG encoding failed"
    }.bytes
