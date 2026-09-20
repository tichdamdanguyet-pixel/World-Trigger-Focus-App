package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParameterItem
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RadarChartComposable(
    parameters: List<ParameterItem>,
    title: String = "TRION PARAMETER",
    subtitle: String = "パラメーター",
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(parameters) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate900, RoundedCornerShape(12.dp))
            .border(1.dp, Slate700, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header tag
            Column(
                modifier = Modifier
                    .border(
                        width = 0.dp,
                        color = Color.Transparent
                    )
                    .padding(bottom = 6.dp)
            ) {
                Text(
                    text = subtitle,
                    color = Cyan400,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = title,
                    color = Cyan400,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                val sizeVal = min(size.width, size.height)
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = (sizeVal / 2f) - 48.dp.toPx()
                val levels = 5
                val maxValue = 10f
                val totalCount = parameters.size
                val angleStep = (Math.PI * 2) / totalCount

                // Outer decorative dashed ring
                drawCircle(
                    color = Slate800,
                    radius = radius + 6.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 8.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f))
                    )
                )

                // Grid polygons
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                for (level in 1..levels) {
                    val levelValue = (maxValue / levels) * level
                    val gridPath = Path()
                    for (i in 0 until totalCount) {
                        val angle = (i * angleStep) - (Math.PI / 2)
                        val r = (levelValue / maxValue) * radius
                        val x = center.x + (r * cos(angle)).toFloat()
                        val y = center.y + (r * sin(angle)).toFloat()
                        if (i == 0) gridPath.moveTo(x, y) else gridPath.lineTo(x, y)
                    }
                    gridPath.close()
                    drawPath(
                        path = gridPath,
                        color = Slate500.copy(alpha = 0.6f),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = if (level == levels) null else dashEffect
                        )
                    )
                }

                // Grid radial lines from center to outer vertex
                for (i in 0 until totalCount) {
                    val angle = (i * angleStep) - (Math.PI / 2)
                    val endX = center.x + (radius * cos(angle)).toFloat()
                    val endY = center.y + (radius * sin(angle)).toFloat()
                    drawLine(
                        color = Slate500.copy(alpha = 0.6f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Data polygon
                val dataPath = Path()
                val vertexPoints = mutableListOf<Offset>()
                for (i in 0 until totalCount) {
                    val angle = (i * angleStep) - (Math.PI / 2)
                    val rawValue = parameters[i].value.toFloat() * animatedProgress.value
                    val r = (rawValue / maxValue) * radius
                    val x = center.x + (r * cos(angle)).toFloat()
                    val y = center.y + (r * sin(angle)).toFloat()
                    val pt = Offset(x, y)
                    vertexPoints.add(pt)
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                // Fill with translucent cyan
                drawPath(
                    path = dataPath,
                    color = Cyan500.copy(alpha = 0.35f)
                )
                // Stroke with solid cyan
                drawPath(
                    path = dataPath,
                    color = Cyan500,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Vertex points
                vertexPoints.forEach { pt ->
                    drawCircle(
                        color = Cyan400,
                        radius = 4.dp.toPx(),
                        center = pt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = pt,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }

                // Draw Labels
                val labelRadius = radius + 22.dp.toPx()
                for (i in 0 until totalCount) {
                    val item = parameters[i]
                    val angle = (i * angleStep) - (Math.PI / 2)
                    val labelX = center.x + (labelRadius * cos(angle)).toFloat()
                    val labelY = center.y + (labelRadius * sin(angle)).toFloat()

                    val combinedText = buildString {
                        item.displayLines.forEach { append(it).append("\n") }
                        append(item.value.toString())
                    }

                    val textResult = textMeasurer.measure(
                        text = combinedText,
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Slate100,
                            lineHeight = 11.sp
                        )
                    )

                    val textOffset = Offset(
                        x = labelX - (textResult.size.width / 2f),
                        y = labelY - (textResult.size.height / 2f)
                    )

                    drawText(
                        textLayoutResult = textResult,
                        topLeft = textOffset
                    )
                }
            }
        }
    }
}
