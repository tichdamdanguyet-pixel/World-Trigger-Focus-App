package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DailySoloRankMissionEntity
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.GreenSuccess
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data structure holding mission analytics calculations:
 * - Average Missions Per Week
 * - Most Productive Day of the Week
 * - Day-of-week breakdown and span
 */
data class MissionAnalyticsResult(
    val totalMissionsAnalyzed: Int,
    val completedMissionsCount: Int,
    val averageMissionsPerWeek: Double,
    val formattedAvgPerWeek: String,
    val mostProductiveDayOfWeek: String,
    val mostProductiveDayOfWeekVietnamese: String,
    val mostProductiveDayCount: Int,
    val dayOfWeekCounts: Map<Int, Int>, // Calendar.SUNDAY..Calendar.SATURDAY -> count
    val totalWeeksSpan: Int
)

/**
 * Parses timestamp or dateString from DailySoloRankMissionEntity.
 */
fun getMissionTimestamp(mission: DailySoloRankMissionEntity): Long {
    if (mission.completedAt != null && mission.completedAt > 0) {
        return mission.completedAt
    }
    if (mission.dateString.isNotBlank()) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = sdf.parse(mission.dateString.trim())
            if (parsed != null) return parsed.time
        } catch (_: Exception) {
            // fallback
        }
    }
    return if (mission.createdAt > 0) mission.createdAt else System.currentTimeMillis()
}

/**
 * Resolves English and Vietnamese day-of-week names for Calendar.DAY_OF_WEEK.
 */
fun getDayOfWeekNames(dayOfWeek: Int): Pair<String, String> {
    return when (dayOfWeek) {
        Calendar.MONDAY -> Pair("Monday", "Thứ Hai")
        Calendar.TUESDAY -> Pair("Tuesday", "Thứ Ba")
        Calendar.WEDNESDAY -> Pair("Wednesday", "Thứ Tư")
        Calendar.THURSDAY -> Pair("Thursday", "Thứ Năm")
        Calendar.FRIDAY -> Pair("Friday", "Thứ Sáu")
        Calendar.SATURDAY -> Pair("Saturday", "Thứ Bảy")
        Calendar.SUNDAY -> Pair("Sunday", "Chủ Nhật")
        else -> Pair("Unknown", "Không rõ")
    }
}

/**
 * Pure function to calculate mission analytics from a list of DailySoloRankMissionEntity:
 * - Computes 'Average Missions Per Week' based on recorded weeks.
 * - Computes 'Most Productive Day of the Week' based on mission distribution.
 */
fun calculateMissionAnalytics(
    missions: List<DailySoloRankMissionEntity>,
    onlyCompleted: Boolean = true
): MissionAnalyticsResult {
    val targetMissions = if (onlyCompleted) {
        val completed = missions.filter { it.isCompleted }
        if (completed.isEmpty() && missions.isNotEmpty()) missions else completed
    } else {
        missions
    }

    val completedCount = missions.count { it.isCompleted }

    if (targetMissions.isEmpty()) {
        val emptyDays = (Calendar.SUNDAY..Calendar.SATURDAY).associateWith { 0 }
        return MissionAnalyticsResult(
            totalMissionsAnalyzed = 0,
            completedMissionsCount = 0,
            averageMissionsPerWeek = 0.0,
            formattedAvgPerWeek = "0.0",
            mostProductiveDayOfWeek = "N/A",
            mostProductiveDayOfWeekVietnamese = "Chưa có dữ liệu",
            mostProductiveDayCount = 0,
            dayOfWeekCounts = emptyDays,
            totalWeeksSpan = 0
        )
    }

    val dayCounts = mutableMapOf<Int, Int>()
    for (d in Calendar.SUNDAY..Calendar.SATURDAY) {
        dayCounts[d] = 0
    }

    val weekKeys = mutableSetOf<String>()
    val cal = Calendar.getInstance()

    for (m in targetMissions) {
        val time = getMissionTimestamp(m)
        cal.timeInMillis = time

        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        dayCounts[dayOfWeek] = (dayCounts[dayOfWeek] ?: 0) + 1

        val year = cal.get(Calendar.YEAR)
        val week = cal.get(Calendar.WEEK_OF_YEAR)
        weekKeys.add("$year-W$week")
    }

    val distinctWeeks = weekKeys.size.coerceAtLeast(1)
    val avgPerWeek = targetMissions.size.toDouble() / distinctWeeks.toDouble()
    val formattedAvg = String.format(Locale.US, "%.1f", avgPerWeek)

    val maxCount = dayCounts.values.maxOrNull() ?: 0
    val topDayEntry = dayCounts.entries.find { it.value == maxCount && maxCount > 0 }

    val (dayEn, dayVi) = if (topDayEntry != null && maxCount > 0) {
        getDayOfWeekNames(topDayEntry.key)
    } else {
        Pair("N/A", "Chưa có dữ liệu")
    }

    return MissionAnalyticsResult(
        totalMissionsAnalyzed = targetMissions.size,
        completedMissionsCount = completedCount,
        averageMissionsPerWeek = avgPerWeek,
        formattedAvgPerWeek = formattedAvg,
        mostProductiveDayOfWeek = dayEn,
        mostProductiveDayOfWeekVietnamese = dayVi,
        mostProductiveDayCount = maxCount,
        dayOfWeekCounts = dayCounts,
        totalWeeksSpan = distinctWeeks
    )
}

/**
 * MissionPerformanceAnalyticsSection:
 * Tactical analysis section located below the performance chart.
 * Analyzes the user's mission data to calculate and display:
 * - 'Average Missions Per Week'
 * - 'Most Productive Day of the Week'
 * - Interactive 7-day distribution visualizer
 */
@Composable
fun MissionPerformanceAnalyticsSection(
    missions: List<DailySoloRankMissionEntity>,
    modifier: Modifier = Modifier
) {
    var analyzeOnlyCompleted by remember { mutableStateOf(true) }

    val analytics = remember(missions, analyzeOnlyCompleted) {
        calculateMissionAnalytics(missions, onlyCompleted = analyzeOnlyCompleted)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Cyan500.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .testTag("mission_performance_analytics_section"),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Bar
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
                            imageVector = Icons.Default.Insights,
                            contentDescription = "Mission Analytics",
                            tint = Cyan300,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PHÂN TÍCH NHIỆM VỤ CHIẾN THUẬT",
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
                                    .background(GreenSuccess.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ROOM ANALYTICS",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = GreenSuccess
                                )
                            }
                        }
                        Text(
                            text = "Hiệu suất hoàn thành nhiệm vụ theo tuần & ngày trong tuần",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }
                }

                // Scope Filter Toggle
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .background(Slate950, RoundedCornerShape(6.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (analyzeOnlyCompleted) Cyan500 else Color.Transparent)
                            .clickable { analyzeOnlyCompleted = true }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("analytics_mode_completed"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐÃ ĐẠT",
                            fontSize = 8.5.sp,
                            fontWeight = if (analyzeOnlyCompleted) FontWeight.Black else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (analyzeOnlyCompleted) Slate950 else Slate400
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (!analyzeOnlyCompleted) Cyan500 else Color.Transparent)
                            .clickable { analyzeOnlyCompleted = false }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("analytics_mode_all"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TẤT CẢ",
                            fontSize = 8.5.sp,
                            fontWeight = if (!analyzeOnlyCompleted) FontWeight.Black else FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = if (!analyzeOnlyCompleted) Slate950 else Slate400
                        )
                    }
                }
            }

            // Two Main KPI Cards Grid: Average Missions Per Week & Most Productive Day of the Week
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Average Missions Per Week
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, Cyan400.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .testTag("avg_missions_per_week_card"),
                    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Average Missions Per Week",
                                    tint = Cyan300,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AVERAGE MISSIONS PER WEEK",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = "Trung bình nhiệm vụ mỗi tuần",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = analytics.formattedAvgPerWeek,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate100,
                                modifier = Modifier.testTag("avg_missions_per_week_value")
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "nhiệm vụ/tuần",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan400,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Text(
                            text = if (analytics.totalMissionsAnalyzed > 0)
                                "${analytics.totalMissionsAnalyzed} nhiệm vụ qua ${analytics.totalWeeksSpan} tuần"
                            else "Chưa có nhiệm vụ ghi nhận",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                    }
                }

                // Card 2: Most Productive Day of the Week
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, GreenSuccess.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .testTag("most_productive_day_card"),
                    colors = CardDefaults.cardColors(containerColor = Slate950.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Most Productive Day of the Week",
                                    tint = GreenSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MOST PRODUCTIVE DAY OF THE WEEK",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = GreenSuccess,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Text(
                            text = "Ngày năng suất cao nhất",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = analytics.mostProductiveDayOfWeek,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (analytics.mostProductiveDayCount > 0) GreenSuccess else Slate400,
                                modifier = Modifier.testTag("most_productive_day_value"),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (analytics.mostProductiveDayCount > 0)
                                "${analytics.mostProductiveDayOfWeekVietnamese} (${analytics.mostProductiveDayCount} nhiệm vụ)"
                            else "Chưa đủ dữ liệu",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Day of Week Distribution Visualizer (Monday -> Sunday)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate950)
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(10.dp)
                    .testTag("day_of_week_chart"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PHÂN BỔ TÁC CHIẾN THEO THỨ (DAY-OF-WEEK DISTRIBUTION)",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300
                    )

                    if (analytics.mostProductiveDayCount > 0) {
                        Text(
                            text = "ĐỈNH: ${analytics.mostProductiveDayOfWeek.uppercase()}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }
                }

                // Bar Chart Row: Monday to Sunday
                val daysOrdered = listOf(
                    Triple(Calendar.MONDAY, "T2", "Mon"),
                    Triple(Calendar.TUESDAY, "T3", "Tue"),
                    Triple(Calendar.WEDNESDAY, "T4", "Wed"),
                    Triple(Calendar.THURSDAY, "T5", "Thu"),
                    Triple(Calendar.FRIDAY, "T6", "Fri"),
                    Triple(Calendar.SATURDAY, "T7", "Sat"),
                    Triple(Calendar.SUNDAY, "CN", "Sun")
                )

                val maxCount = analytics.mostProductiveDayCount.coerceAtLeast(1)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    daysOrdered.forEach { (calDay, viLabel, enLabel) ->
                        val count = analytics.dayOfWeekCounts[calDay] ?: 0
                        val isPeak = count == analytics.mostProductiveDayCount && count > 0
                        val heightFraction = if (count > 0) (count.toFloat() / maxCount.toFloat()).coerceIn(0.15f, 1f) else 0.06f

                        val animatedFraction by animateFloatAsState(
                            targetValue = heightFraction,
                            animationSpec = tween(durationMillis = 400),
                            label = "bar_anim_$enLabel"
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("day_bar_${enLabel.lowercase()}"),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            // Count label on top of bar
                            Text(
                                text = if (count > 0) "$count" else "-",
                                fontSize = 8.sp,
                                fontWeight = if (isPeak) FontWeight.Black else FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (isPeak) GreenSuccess else if (count > 0) Cyan300 else Slate600
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            // Vertical Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height((50 * animatedFraction).dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(
                                        when {
                                            isPeak -> Brush.verticalGradient(
                                                listOf(GreenSuccess, Cyan400)
                                            )
                                            count > 0 -> Brush.verticalGradient(
                                                listOf(Cyan400, Cyan500.copy(alpha = 0.5f))
                                            )
                                            else -> Brush.verticalGradient(
                                                listOf(Slate700, Slate800)
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isPeak) 1.dp else 0.5.dp,
                                        color = if (isPeak) GreenSuccess else if (count > 0) Cyan400.copy(alpha = 0.4f) else Slate800,
                                        shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                    )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Day Label
                            Text(
                                text = viLabel,
                                fontSize = 8.5.sp,
                                fontWeight = if (isPeak) FontWeight.Black else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = if (isPeak) GreenSuccess else if (count > 0) Slate200 else Slate500
                            )
                        }
                    }
                }
            }

            // Summary Metrics Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "ĐÃ HOÀN TẤT: ${analytics.completedMissionsCount}/${missions.size}",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300
                    )
                }

                Text(
                    text = "GHI NHẬN: ${analytics.totalWeeksSpan} TUẦN",
                    fontSize = 8.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
            }
        }
    }
}
