package com.example.ui.components.codeeditor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp

/**
 * Custom Canvas Drawer that renders an IDE-style squiggly wavy underline effect
 * under tokens with syntax errors or logic warnings.
 */
object WavyUnderlineDrawer {

    val ColorError = Color(0xFFEF4444)   // Bright Red
    val ColorWarning = Color(0xFFF59E0B) // Amber / Orange
    val ColorInfo = Color(0xFF06B6D4)    // Electric Cyan

    /**
     * Draws wavy squiggly lines under character ranges corresponding to code diagnostics.
     */
    fun drawWavyUnderlines(
        drawScope: DrawScope,
        textLayoutResult: TextLayoutResult,
        diagnostics: List<CodeDiagnostic>,
        textLength: Int
    ) {
        if (textLength == 0 || diagnostics.isEmpty()) return

        val waveLength = with(drawScope) { 6.dp.toPx() }
        val waveAmp = with(drawScope) { 1.6.dp.toPx() }
        val strokeWidth = with(drawScope) { 1.6.dp.toPx() }

        diagnostics.forEach { diagnostic ->
            val color = when (diagnostic.severity) {
                DiagnosticSeverity.ERROR -> ColorError
                DiagnosticSeverity.WARNING -> ColorWarning
                DiagnosticSeverity.INFO -> ColorInfo
            }

            val start = diagnostic.startOffset.coerceIn(0, textLength)
            val end = diagnostic.endOffset.coerceIn(start + 1, textLength)

            try {
                val startLine = textLayoutResult.getLineForOffset(start)
                val endLine = textLayoutResult.getLineForOffset((end - 1).coerceAtLeast(0))

                for (line in startLine..endLine) {
                    val lineStart = textLayoutResult.getLineStart(line)
                    val lineEnd = textLayoutResult.getLineEnd(line)

                    val segStart = maxOf(start, lineStart)
                    val segEnd = minOf(end, lineEnd)

                    if (segEnd > segStart) {
                        val x1 = textLayoutResult.getHorizontalPosition(segStart, true)
                        val x2 = textLayoutResult.getHorizontalPosition(segEnd, true)

                        val left = minOf(x1, x2)
                        val right = maxOf(x1, x2).coerceAtLeast(left + waveLength)
                        val bottom = textLayoutResult.getLineBottom(line)
                        val waveY = bottom - with(drawScope) { 1.5.dp.toPx() }

                        val wavePath = Path()
                        var x = left
                        wavePath.moveTo(x, waveY)

                        while (x < right) {
                            val nextX = minOf(x + waveLength, right)
                            val span = nextX - x
                            val halfW = span / 2f

                            if (halfW > 0.5f) {
                                // Wave crest (upward) then wave trough (downward)
                                wavePath.quadraticBezierTo(
                                    x + halfW / 2f,
                                    waveY - waveAmp,
                                    x + halfW,
                                    waveY
                                )
                                wavePath.quadraticBezierTo(
                                    x + halfW * 1.5f,
                                    waveY + waveAmp,
                                    nextX,
                                    waveY
                                )
                            } else {
                                wavePath.lineTo(nextX, waveY)
                            }
                            x = nextX
                        }

                        drawScope.drawPath(
                            path = wavePath,
                            color = color,
                            style = Stroke(
                                width = strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback in case of layout timing race
            }
        }
    }
}
