package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.model.SoloRankHistoryStats
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessDark
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RankARank
import com.example.ui.theme.RankBRank
import com.example.ui.theme.RankCRank
import com.example.ui.theme.RankSRank
import com.example.ui.theme.RedFailDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class PerformanceChartType {
    POINTS_TREND,    // D3 Area / Recharts AreaChart with Bezier Spline
    COMBAT_ROLLS,    // Recharts Grouped BarChart (Player Roll vs Enemy Roll)
    POINTS_DELTA     // Recharts Zero-centered Diverging Delta BarChart (+/- PTS)
}

enum class ChartRangeFilter {
    ALL,
    LAST_10,
    LAST_20
}

/**
 * SoloRankPerformanceVisualization:
 * A rich, interactive D3 & Recharts-style charting visualization component
 * for player performance history stored in the Solo Rank War Room database.
 */
@Composable
fun SoloRankPerformanceVisualization(
    matches: List<SoloRankMatchEntity>,
    stats: SoloRankHistoryStats,
    missions: List<com.example.data.local.entity.DailySoloRankMissionEntity> = emptyList(),
    showMissionAnalytics: Boolean = false,
    onNavigateToArena: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedChartType by remember { mutableStateOf(PerformanceChartType.POINTS_TREND) }
    var selectedRange by remember { mutableStateOf(ChartRangeFilter.ALL) }

    // Filter and prepare chronological dataset (Oldest to Newest for time-series progression)
    val chronologicalMatches = remember(matches, selectedRange) {
        val reversed = matches.reversed() // Oldest -> Newest
        when (selectedRange) {
            ChartRangeFilter.ALL -> reversed
            ChartRangeFilter.LAST_10 -> reversed.takeLast(10)
            ChartRangeFilter.LAST_20 -> reversed.takeLast(20)
        }
    }

    // Default inspected match index (defaults to latest match if available)
    var hoveredIndex by remember(chronologicalMatches) {
        mutableIntStateOf(if (chronologicalMatches.isNotEmpty()) chronologicalMatches.lastIndex else -1)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Cyan500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("solo_rank_performance_visualization_container"),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar: Tactical Title and D3 / Recharts Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Cyan500.copy(alpha = 0.18f))
                            .border(1.dp, Cyan400.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "Visualizer",
                            tint = Cyan300,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "TRION COMBAT VISUALIZER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate100,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Cyan500.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "D3/RECHARTS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300
                                )
                            }
                        }
                        Text(
                            text = "Trực quan hóa hiệu suất tác chiến từ cơ sở dữ liệu Solo Rank Room",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }
                }

                // Range Selector Pills
                if (matches.size > 5) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .background(Slate950, RoundedCornerShape(6.dp))
                            .padding(2.dp)
                    ) {
                        val ranges = listOf(
                            ChartRangeFilter.ALL to "ALL",
                            ChartRangeFilter.LAST_20 to "20",
                            ChartRangeFilter.LAST_10 to "10"
                        )
                        ranges.forEach { (range, label) ->
                            val isSelected = selectedRange == range
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) Cyan500 else Color.Transparent)
                                    .clickable { selectedRange = range }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("chart_range_${label.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 8.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Slate950 else Slate400
                                )
                            }
                        }
                    }
                }
            }

            // Top KPI Gauge Strip (Win Rate ring, Roll averages, Net Delta)
            PerformanceKpiStrip(
                matches = chronologicalMatches,
                stats = stats
            )

            if (chronologicalMatches.isEmpty()) {
                // Empty state for visualization
                EmptyVisualizationPrompt(onNavigateToArena = onNavigateToArena)
            } else {
                // Chart Type Selector Segmented Row (Area Chart, Grouped Bars, Delta Bars)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    val chartTypes = listOf(
                        Triple(PerformanceChartType.POINTS_TREND, "TIẾN TRÌNH ĐIỂM", Icons.Default.ShowChart),
                        Triple(PerformanceChartType.COMBAT_ROLLS, "ĐỐI ĐẦU ROLL", Icons.Default.BarChart),
                        Triple(PerformanceChartType.POINTS_DELTA, "BIẾN ĐỘNG +/-", Icons.Default.TrendingUp)
                    )

                    chartTypes.forEach { (type, label, icon) ->
                        val isSelected = selectedChartType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Cyan500.copy(alpha = 0.25f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Cyan400 else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedChartType = type }
                                .padding(vertical = 5.dp)
                                .testTag("btn_chart_${type.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Cyan300 else Slate500,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSelected) Cyan300 else Slate400
                                )
                            }
                        }
                    }
                }

                // Main Interactive Canvas Visualizer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .testTag("interactive_chart_canvas_box")
                ) {
                    when (selectedChartType) {
                        PerformanceChartType.POINTS_TREND -> {
                            D3AreaPointsTrendChart(
                                matches = chronologicalMatches,
                                hoveredIndex = hoveredIndex,
                                onHoverIndexChange = { hoveredIndex = it }
                            )
                        }
                        PerformanceChartType.COMBAT_ROLLS -> {
                            RechartsGroupedRollsBarChart(
                                matches = chronologicalMatches,
                                hoveredIndex = hoveredIndex,
                                onHoverIndexChange = { hoveredIndex = it }
                            )
                        }
                        PerformanceChartType.POINTS_DELTA -> {
                            RechartsDeltaPointsBarChart(
                                matches = chronologicalMatches,
                                hoveredIndex = hoveredIndex,
                                onHoverIndexChange = { hoveredIndex = it }
                            )
                        }
                    }
                }

                // Interactive HUD Tooltip Inspector Card
                if (hoveredIndex in chronologicalMatches.indices) {
                    val inspectedMatch = chronologicalMatches[hoveredIndex]
                    InspectedMatchTooltipCard(
                        match = inspectedMatch,
                        matchNumber = hoveredIndex + 1,
                        totalInView = chronologicalMatches.size
                    )
                }

                if (showMissionAnalytics && missions.isNotEmpty()) {
                    MissionPerformanceAnalyticsSection(
                        missions = missions,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * KPI Gauge Strip showing overall win rate donut ring, roll averages, and net score delta.
 */
@Composable
private fun PerformanceKpiStrip(
    matches: List<SoloRankMatchEntity>,
    stats: SoloRankHistoryStats,
    modifier: Modifier = Modifier
) {
    val avgPlayerRoll = remember(matches) {
        if (matches.isEmpty()) 0f else matches.map { it.playerRoll }.average().toFloat()
    }
    val avgEnemyRoll = remember(matches) {
        if (matches.isEmpty()) 0f else matches.map { it.enemyRoll }.average().toFloat()
    }
    val peakPoints = remember(matches) {
        if (matches.isEmpty()) 0 else matches.maxOf { it.finalPoints }
    }
    val netDelta = stats.totalPointsGained - stats.totalPointsLost

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate950, RoundedCornerShape(8.dp))
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Win Rate Ring Gauge
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 3.5.dp.toPx()
                    // Track circle
                    drawCircle(
                        color = Slate800,
                        radius = (size.minDimension - strokeWidth) / 2f,
                        style = Stroke(width = strokeWidth)
                    )
                    // Active arc
                    val sweep = (stats.winRatePercent / 100f) * 360f
                    drawArc(
                        color = if (stats.winRatePercent >= 50) GreenSuccess else OrangeWarning,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${stats.winRatePercent}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Slate100
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = "TỶ LỆ THẮNG",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate500
                )
                Text(
                    text = "${stats.wins}W - ${stats.losses}L",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate300
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(26.dp)
                .background(Slate800)
        )

        // Average Roll Score comparison
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "TB XÚC XẮC",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", avgPlayerRoll),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
                Text(
                    text = " vs ",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate500
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f", avgEnemyRoll),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
            }
        }

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(26.dp)
                .background(Slate800)
        )

        // Peak Points or Net Delta
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "ĐỈNH ĐIỂM / NET",
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500
            )
            Text(
                text = "$peakPoints PTS (${if (netDelta >= 0) "+" else ""}$netDelta)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = if (netDelta >= 0) Cyan300 else RankSRank
            )
        }
    }
}

/**
 * D3 / Recharts Area & Spline Trend Chart:
 * - Plots chronological Trion Points curve
 * - Smooth Cubic Bezier Splines
 * - High-tech Cartesian gridlines
 * - Cyan gradient under-fill (Recharts style <defs><linearGradient>)
 * - Win (Green) / Loss (Red) node points
 * - Touch & drag scrubber / crosshair line
 */
@Composable
private fun D3AreaPointsTrendChart(
    matches: List<SoloRankMatchEntity>,
    hoveredIndex: Int,
    onHoverIndexChange: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(matches) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
    }

    val minPoints = remember(matches) {
        val min = matches.minOfOrNull { it.finalPoints } ?: 4000
        (min - 50).coerceAtLeast(0)
    }
    val maxPoints = remember(matches) {
        val max = matches.maxOfOrNull { it.finalPoints } ?: 4200
        max + 50
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(matches) {
                detectTapGestures { offset ->
                    val paddingLeft = 45.dp.toPx()
                    val paddingRight = 15.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val stepX = plotWidth / max(1, matches.size - 1)
                        val relativeX = (offset.x - paddingLeft).coerceIn(0f, plotWidth)
                        val index = (relativeX / stepX).roundToInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
            .pointerInput(matches) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val paddingLeft = 45.dp.toPx()
                    val paddingRight = 15.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val stepX = plotWidth / max(1, matches.size - 1)
                        val relativeX = (change.position.x - paddingLeft).coerceIn(0f, plotWidth)
                        val index = (relativeX / stepX).roundToInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 45.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 22.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        if (plotWidth <= 0f || plotHeight <= 0f || matches.isEmpty()) return@Canvas

        val progress = animatedProgress.value
        val rangeY = max(1, maxPoints - minPoints).toFloat()

        // 1. Draw Cartesian Grid Lines (like Recharts <CartesianGrid strokeDasharray="3 3" />)
        val gridSteps = 3
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

        for (i in 0..gridSteps) {
            val ratio = i.toFloat() / gridSteps
            val y = paddingTop + plotHeight * ratio
            val valueAtGrid = (maxPoints - ratio * rangeY).roundToInt()

            // Horizontal grid line
            drawLine(
                color = Slate800,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            // Y-Axis tick label
            val labelText = "$valueAtGrid"
            val textLayout = textMeasurer.measure(
                text = labelText,
                style = TextStyle(
                    color = Slate500,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            )
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(paddingLeft - textLayout.size.width - 6.dp.toPx(), y - textLayout.size.height / 2f)
            )
        }

        // Calculate (X, Y) pixel coordinates for all data points
        val stepX = if (matches.size > 1) plotWidth / (matches.size - 1) else plotWidth / 2f
        val points = matches.mapIndexed { index, match ->
            val x = paddingLeft + (if (matches.size > 1) index * stepX else plotWidth / 2f)
            val normalizedY = (match.finalPoints - minPoints) / rangeY
            val targetY = paddingTop + plotHeight * (1f - normalizedY)
            // Animated entrance
            val baselineY = paddingTop + plotHeight
            val animatedY = baselineY + (targetY - baselineY) * progress
            Offset(x, animatedY)
        }

        // 2. Build Smooth Cubic Bezier Path (D3 Monotone spline interpolation)
        val strokePath = Path()
        val areaPath = Path()

        if (points.isNotEmpty()) {
            strokePath.moveTo(points.first().x, points.first().y)
            areaPath.moveTo(points.first().x, paddingTop + plotHeight)
            areaPath.lineTo(points.first().x, points.first().y)

            if (points.size == 1) {
                strokePath.lineTo(points.first().x, points.first().y)
                areaPath.lineTo(points.first().x, points.first().y)
            } else {
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                    val controlY1 = p0.y
                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                    val controlY2 = p1.y
                    strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                    areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                }
            }

            areaPath.lineTo(points.last().x, paddingTop + plotHeight)
            areaPath.close()

            // Draw Area Gradient Under Spline (Recharts <Area fill="url(#gradient)" />)
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Cyan400.copy(alpha = 0.35f),
                        Cyan500.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    startY = paddingTop,
                    endY = paddingTop + plotHeight
                )
            )

            // Draw Curve Stroke
            drawPath(
                path = strokePath,
                color = Cyan400,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        // 3. Draw Data Nodes (Win = Green Node, Loss = Red Node)
        points.forEachIndexed { index, point ->
            val match = matches[index]
            val isWin = match.won
            val nodeColor = if (isWin) GreenSuccess else RankSRank
            val isHovered = index == hoveredIndex

            // Outer glow ring
            drawCircle(
                color = nodeColor.copy(alpha = if (isHovered) 0.4f else 0.15f),
                radius = if (isHovered) 7.dp.toPx() else 4.5.dp.toPx(),
                center = point
            )

            // Inner solid dot
            drawCircle(
                color = nodeColor,
                radius = if (isHovered) 4.5.dp.toPx() else 3.dp.toPx(),
                center = point
            )

            // White core for hovered node
            if (isHovered) {
                drawCircle(
                    color = Slate100,
                    radius = 2.dp.toPx(),
                    center = point
                )
            }
        }

        // 4. Draw Scrubber Guideline (Recharts Tooltip Cursor)
        if (hoveredIndex in points.indices) {
            val activePoint = points[hoveredIndex]
            drawLine(
                color = Cyan300.copy(alpha = 0.75f),
                start = Offset(activePoint.x, paddingTop),
                end = Offset(activePoint.x, paddingTop + plotHeight),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )

            // Bottom X-Axis match indicator tick
            val matchLabel = "#${hoveredIndex + 1}"
            val tickLayout = textMeasurer.measure(
                text = matchLabel,
                style = TextStyle(
                    color = Cyan300,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = tickLayout,
                topLeft = Offset(
                    (activePoint.x - tickLayout.size.width / 2f).coerceIn(paddingLeft, width - paddingRight - tickLayout.size.width),
                    paddingTop + plotHeight + 4.dp.toPx()
                )
            )
        }
    }
}

/**
 * Recharts-style Grouped Bar Chart:
 * Displays dual vertical bars comparing Player Roll (Cyan) vs Enemy Roll (Orange/Red)
 * for each match with tactical styling.
 */
@Composable
private fun RechartsGroupedRollsBarChart(
    matches: List<SoloRankMatchEntity>,
    hoveredIndex: Int,
    onHoverIndexChange: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(matches) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(matches) {
                detectTapGestures { offset ->
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val groupWidth = plotWidth / matches.size
                        val index = ((offset.x - paddingLeft) / groupWidth).toInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
            .pointerInput(matches) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val paddingLeft = 32.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val groupWidth = plotWidth / matches.size
                        val index = ((change.position.x - paddingLeft) / groupWidth).toInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 32.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 20.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        if (plotWidth <= 0f || plotHeight <= 0f || matches.isEmpty()) return@Canvas

        val progress = animatedProgress.value
        val maxRoll = 50f // Maximum roll in solo rank war

        // Cartesian horizontal grid lines at 10, 25, 50
        val gridLines = listOf(10, 25, 50)
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

        gridLines.forEach { gridValue ->
            val ratio = gridValue / maxRoll
            val y = paddingTop + plotHeight * (1f - ratio)

            drawLine(
                color = Slate800,
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )

            val tickLayout = textMeasurer.measure(
                text = "$gridValue",
                style = TextStyle(
                    color = Slate500,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
            drawText(
                textLayoutResult = tickLayout,
                topLeft = Offset(paddingLeft - tickLayout.size.width - 4.dp.toPx(), y - tickLayout.size.height / 2f)
            )
        }

        val groupWidth = plotWidth / matches.size
        val barWidth = (groupWidth * 0.32f).coerceAtMost(10.dp.toPx()).coerceAtLeast(3.dp.toPx())
        val spacingBetweenBars = 2.dp.toPx()

        matches.forEachIndexed { index, match ->
            val groupStartX = paddingLeft + index * groupWidth
            val groupCenterX = groupStartX + groupWidth / 2f
            val isHovered = index == hoveredIndex

            // Highlight background if hovered
            if (isHovered) {
                drawRoundRect(
                    color = Slate800.copy(alpha = 0.5f),
                    topLeft = Offset(groupStartX, paddingTop),
                    size = Size(groupWidth, plotHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Player Bar (Cyan400)
            val playerBarHeight = (match.playerRoll / maxRoll) * plotHeight * progress
            val playerBarTop = paddingTop + plotHeight - playerBarHeight
            val playerBarLeft = groupCenterX - barWidth - spacingBetweenBars / 2f

            drawRoundRect(
                color = if (isHovered) Cyan300 else Cyan400,
                topLeft = Offset(playerBarLeft, playerBarTop),
                size = Size(barWidth, playerBarHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Enemy Bar (Orange/Red)
            val enemyBarHeight = (match.enemyRoll / maxRoll) * plotHeight * progress
            val enemyBarTop = paddingTop + plotHeight - enemyBarHeight
            val enemyBarLeft = groupCenterX + spacingBetweenBars / 2f
            val enemyColor = if (match.won) OrangeWarning else RankSRank

            drawRoundRect(
                color = if (isHovered) enemyColor.copy(alpha = 0.9f) else enemyColor.copy(alpha = 0.75f),
                topLeft = Offset(enemyBarLeft, enemyBarTop),
                size = Size(barWidth, enemyBarHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Bottom index label for every few matches
            if (matches.size <= 10 || index % 2 == 0 || isHovered) {
                val matchLabel = "#${index + 1}"
                val tickLayout = textMeasurer.measure(
                    text = matchLabel,
                    style = TextStyle(
                        color = if (isHovered) Cyan300 else Slate500,
                        fontSize = 7.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isHovered) FontWeight.Bold else FontWeight.Normal
                    )
                )
                drawText(
                    textLayoutResult = tickLayout,
                    topLeft = Offset(
                        groupCenterX - tickLayout.size.width / 2f,
                        paddingTop + plotHeight + 3.dp.toPx()
                    )
                )
            }
        }
    }
}

/**
 * Recharts Zero-Centered Diverging Bar Chart:
 * Shows net points gain (+PTS in Green) or net points loss (-PTS in Red) per match
 * from a zero-baseline axis.
 */
@Composable
private fun RechartsDeltaPointsBarChart(
    matches: List<SoloRankMatchEntity>,
    hoveredIndex: Int,
    onHoverIndexChange: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(matches) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650)
        )
    }

    val maxAbsDelta = remember(matches) {
        val max = matches.maxOfOrNull { it.pointsDiff } ?: 35
        (max + 5).coerceAtLeast(20)
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(matches) {
                detectTapGestures { offset ->
                    val paddingLeft = 36.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val barSlotWidth = plotWidth / matches.size
                        val index = ((offset.x - paddingLeft) / barSlotWidth).toInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
            .pointerInput(matches) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val paddingLeft = 36.dp.toPx()
                    val paddingRight = 16.dp.toPx()
                    val plotWidth = size.width - paddingLeft - paddingRight
                    if (plotWidth > 0 && matches.isNotEmpty()) {
                        val barSlotWidth = plotWidth / matches.size
                        val index = ((change.position.x - paddingLeft) / barSlotWidth).toInt().coerceIn(0, matches.lastIndex)
                        onHoverIndexChange(index)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 36.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 14.dp.toPx()
        val paddingBottom = 16.dp.toPx()

        val plotWidth = width - paddingLeft - paddingRight
        val plotHeight = height - paddingTop - paddingBottom

        if (plotWidth <= 0f || plotHeight <= 0f || matches.isEmpty()) return@Canvas

        val progress = animatedProgress.value
        val zeroY = paddingTop + plotHeight / 2f
        val halfHeight = plotHeight / 2f

        // Draw Zero Baseline (Recharts <ReferenceLine y={0} />)
        drawLine(
            color = Slate600,
            start = Offset(paddingLeft, zeroY),
            end = Offset(width - paddingRight, zeroY),
            strokeWidth = 1.5.dp.toPx()
        )

        // Draw +Max and -Max reference lines
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
        drawLine(
            color = Slate800,
            start = Offset(paddingLeft, paddingTop),
            end = Offset(width - paddingRight, paddingTop),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )
        drawLine(
            color = Slate800,
            start = Offset(paddingLeft, paddingTop + plotHeight),
            end = Offset(width - paddingRight, paddingTop + plotHeight),
            strokeWidth = 1f,
            pathEffect = dashEffect
        )

        // Reference labels (+Max, 0, -Max)
        listOf(
            Triple("+$maxAbsDelta", paddingTop, GreenSuccess),
            Triple("0", zeroY, Slate500),
            Triple("-$maxAbsDelta", paddingTop + plotHeight, RankSRank)
        ).forEach { (label, yPos, color) ->
            val tickLayout = textMeasurer.measure(
                text = label,
                style = TextStyle(
                    color = color,
                    fontSize = 7.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            )
            drawText(
                textLayoutResult = tickLayout,
                topLeft = Offset(paddingLeft - tickLayout.size.width - 4.dp.toPx(), yPos - tickLayout.size.height / 2f)
            )
        }

        val barSlotWidth = plotWidth / matches.size
        val barWidth = (barSlotWidth * 0.6f).coerceAtMost(14.dp.toPx()).coerceAtLeast(4.dp.toPx())

        matches.forEachIndexed { index, match ->
            val isWin = match.won
            val delta = match.pointsDiff.toFloat()
            val barCenterX = paddingLeft + index * barSlotWidth + barSlotWidth / 2f
            val isHovered = index == hoveredIndex

            val barHeight = (delta / maxAbsDelta) * halfHeight * progress
            val barTop = if (isWin) zeroY - barHeight else zeroY
            val barColor = if (isWin) GreenSuccess else RankSRank

            // Hover indicator column
            if (isHovered) {
                drawRoundRect(
                    color = Slate800.copy(alpha = 0.5f),
                    topLeft = Offset(paddingLeft + index * barSlotWidth, paddingTop),
                    size = Size(barSlotWidth, plotHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Delta Bar
            drawRoundRect(
                color = if (isHovered) barColor else barColor.copy(alpha = 0.85f),
                topLeft = Offset(barCenterX - barWidth / 2f, barTop),
                size = Size(barWidth, max(2.dp.toPx(), barHeight)),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )
        }
    }
}

/**
 * Inspection HUD Tooltip Card:
 * Appears dynamically when the user touches or scrubs across the chart,
 * displaying tactical metadata for that specific match.
 */
@Composable
private fun InspectedMatchTooltipCard(
    match: SoloRankMatchEntity,
    matchNumber: Int,
    totalInView: Int,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { SimpleDateFormat("HH:mm • dd/MM/yyyy", Locale.getDefault()) }
    val isWin = match.won

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isWin) GreenSuccess.copy(alpha = 0.4f) else RankSRank.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .testTag("chart_tooltip_inspection_card"),
        colors = CardDefaults.cardColors(containerColor = Slate950),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Header Row: Match index, Result Badge, Points Diff
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isWin) GreenSuccessDark.copy(alpha = 0.5f) else RedFailDark.copy(alpha = 0.5f))
                            .border(1.dp, if (isWin) GreenSuccess else RankSRank, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (isWin) "VICTORY" else "DEFEAT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isWin) GreenSuccess else RankSRank
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "TRẬN #$matchNumber / $totalInView",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200
                    )
                }

                Text(
                    text = "${if (isWin) "+" else "-"}${match.pointsDiff} PTS → ${match.finalPoints} PTS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isWin) Cyan300 else RankSRank
                )
            }

            // Match details: Opponent & Rolls showdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsKabaddi,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Đối thủ: ${match.opponentName} [${match.opponentRank}]",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "Xúc xắc: ${match.playerRoll} vs ${match.enemyRoll} (/50)",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = if (match.playerRoll >= match.enemyRoll) Cyan400 else OrangeWarning
                )
            }

            // Timestamp and tactic note if available
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(Date(match.timestamp)),
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate500
                )

                if (match.tacticNote.isNotBlank()) {
                    Text(
                        text = match.tacticNote,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Empty visualization prompt when no match data has been recorded in the database yet.
 */
@Composable
private fun EmptyVisualizationPrompt(
    onNavigateToArena: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Slate950)
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .testTag("chart_empty_state_prompt"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ShowChart,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "CHƯA CÓ DỮ LIỆU ĐỂ TRỰC QUAN HÓA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate300
            )
            Text(
                text = "Mỗi trận đấu Solo Rank War sẽ tự động vẽ biểu đồ xu hướng điểm, hiệu suất đổ xúc xắc và biến động +/-.",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            Button(
                onClick = onNavigateToArena,
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .height(30.dp)
                    .testTag("btn_chart_empty_start_arena")
            ) {
                Text(
                    text = "BẮT ĐẦU ĐẤU RANK NGAY",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Slate950
                )
            }
        }
    }
}
