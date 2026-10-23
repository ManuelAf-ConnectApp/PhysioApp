package com.connectapp.commonresources.signature

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** One continuous line of a signature, in coordinates relative to the pad (0..1 on both axes). */
typealias SignatureStroke = List<Offset>

/** Width / height of the pad and of the stored image, so strokes survive rotation unchanged. */
const val SIGNATURE_ASPECT_RATIO = 3f
private const val PNG_WIDTH = 900
private const val PNG_HEIGHT = 300
private const val PNG_STROKE_WIDTH = 6f
private val PadStrokeWidth = 3.dp

/**
 * Handwritten signature surface for finger or stylus, always dark ink on
 * white (also in dark mode), because it becomes a PNG. Each gesture adds a stroke when the finger
 * lifts; clearing is done by the caller with an empty list. Touches inside the pad are consumed,
 * so signing never scrolls the form around it.
 */
@Composable
fun SignaturePad(
    strokes: List<SignatureStroke>,
    onStrokesChange: (List<SignatureStroke>) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val currentStrokes by rememberUpdatedState(strokes)
    val currentOnChange by rememberUpdatedState(onStrokesChange)
    var inProgress by remember { mutableStateOf<SignatureStroke>(emptyList()) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(SIGNATURE_ASPECT_RATIO)
            .background(Color.White, MaterialTheme.shapes.medium)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        val normalize = { position: Offset ->
                            Offset(
                                (position.x / size.width).coerceIn(0f, 1f),
                                (position.y / size.height).coerceIn(0f, 1f),
                            )
                        }
                        inProgress = listOf(normalize(down.position))
                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (change.positionChange() != Offset.Zero) {
                                inProgress = inProgress + normalize(change.position)
                            }
                            change.consume()
                        } while (change.pressed)
                        currentOnChange(currentStrokes + listOf(inProgress))
                        inProgress = emptyList()
                    }
                },
        ) {
            val ink = Stroke(width = PadStrokeWidth.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            (strokes + listOf(inProgress)).forEach { drawSignatureStroke(it, ink) }
        }
    }
}

/**
 * Renders the strokes as a [PNG_WIDTH] x [PNG_HEIGHT] PNG with dark ink on white, independent of
 * the pad's size on screen. Empty strokes give an empty array.
 */
fun renderSignaturePng(strokes: List<SignatureStroke>): ByteArray {
    if (strokes.none { it.isNotEmpty() }) return ByteArray(0)
    val bitmap = ImageBitmap(PNG_WIDTH, PNG_HEIGHT)
    val size = Size(PNG_WIDTH.toFloat(), PNG_HEIGHT.toFloat())
    CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, androidx.compose.ui.graphics.Canvas(bitmap), size) {
        drawRect(Color.White)
        val ink = Stroke(width = PNG_STROKE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round)
        strokes.forEach { drawSignatureStroke(it, ink) }
    }
    return bitmap.encodeToPng()
}

private fun DrawScope.drawSignatureStroke(stroke: SignatureStroke, ink: Stroke) {
    if (stroke.isEmpty()) return
    val points = stroke.map { Offset(it.x * size.width, it.y * size.height) }
    if (points.size == 1) {
        drawCircle(Color.Black, radius = ink.width / 2, center = points.first())
        return
    }
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { lineTo(it.x, it.y) }
    }
    drawPath(path, Color.Black, style = ink)
}

/** PNG encoding of a bitmap; each platform uses its own image codec. */
internal expect fun ImageBitmap.encodeToPng(): ByteArray
