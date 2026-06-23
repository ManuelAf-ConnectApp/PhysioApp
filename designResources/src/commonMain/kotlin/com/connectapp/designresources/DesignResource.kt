package com.connectapp.designresources

import com.connectapp.designresources.DimensResources.COLOR_GRAY_LIGHT
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_B
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_G
import com.connectapp.designresources.DimensResources.COLOR_PRIMARY_R
import com.connectapp.designresources.DimensResources.HEADER_HEIGHT
import com.connectapp.designresources.DimensResources.MARGIN_X
import io.github.yuroyami.kitepdf.writer.ContentStreamBuilder
import io.github.yuroyami.kitepdf.writer.StandardFont


fun ContentStreamBuilder.drawHeader(width: Double, height: Double, title: String) {
    save()
    setFillRgb(COLOR_PRIMARY_R, COLOR_PRIMARY_G, COLOR_PRIMARY_B)
    rectangle(0.0, height - HEADER_HEIGHT, width, HEADER_HEIGHT)
    fill()

    setFillRgb(1.0, 1.0, 1.0)
    text(
        StandardFont.HelveticaBold,
        18.0,
        MARGIN_X,
        height - (HEADER_HEIGHT / 2) - 6.0,
        title
    )
    restore()
}

fun ContentStreamBuilder.drawSection(
    width: Double,
    title: String,
    content: String,
    startY: Double
): Double {
    var y = startY

    // Section Title Background
    save()
    setFillGray(COLOR_GRAY_LIGHT)
    rectangle(MARGIN_X - 5.0, y - 5.0, width - (2 * MARGIN_X) + 10.0, 20.0)
    fill()
    restore()

    // Section Title
    setFillRgb(COLOR_PRIMARY_R, COLOR_PRIMARY_G, COLOR_PRIMARY_B)
    text(StandardFont.HelveticaBold, 12.0, MARGIN_X, y, title)

    y -= 25.0

    // Section Content
    setFillGray(0.0)
    y = drawWrappedText(
        content,
        StandardFont.Helvetica,
        11.0,
        MARGIN_X,
        y,
        width - (2 * MARGIN_X)
    )

    return y
}

fun ContentStreamBuilder.drawWrappedText(
    text: String,
    font: StandardFont,
    size: Double,
    x: Double,
    startY: Double,
    maxWidth: Double
): Double {
    var y = startY
    val words = text.split(" ")
    var currentLine = StringBuilder()

    // Very rough estimation: average char width is about 0.5 * size for Helvetica
    val approxCharsPerLine = (maxWidth / (size * 0.5)).toInt()

    for (word in words) {
        if (currentLine.length + word.length + 1 > approxCharsPerLine) {
            text(font, size, x, y, currentLine.toString())
            y -= (size + 4.0)
            currentLine = StringBuilder(word)
        } else {
            if (currentLine.isNotEmpty()) currentLine.append(" ")
            currentLine.append(word)
        }
    }

    if (currentLine.isNotEmpty()) {
        text(font, size, x, y, currentLine.toString())
        y -= (size + 4.0)
    }

    return y
}

fun ContentStreamBuilder.drawFooter(text: String) {
    save()
    setFillGray(0.8)
    text(
        StandardFont.HelveticaOblique,
        8.0,
        MARGIN_X,
        30.0,
        text
    )
    restore()
}


