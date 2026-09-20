package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.model.LeaderboardAgent
import com.example.ui.AgentUiState
import com.example.ui.components.MissionPerformanceAnalyticsSection
import com.example.ui.components.SoloRankPerformanceVisualization
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.GreenSuccessDark
import com.example.ui.theme.RankARank
import com.example.ui.theme.RankBRank
import com.example.ui.theme.RankCRank
import com.example.ui.theme.RankSRank
import com.example.ui.theme.RedFailDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.components.DailyMissionsTab
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

@Composable
fun SoloRankScreen(
    uiState: AgentUiState,
    onStartMatch: () -> Unit,
    onSelectTab: (Int) -> Unit,
    onSelectHistoryFilter: (String) -> Unit,
    onClearHistory: () -> Unit,
    onSyncCloud: () -> Unit = {},
    onToggleDailyMission: (DailySoloRankMissionEntity) -> Unit = {},
    onAddDailyMission: (title: String, description: String, targetCount: Int, rewardPoints: Int, category: String, notes: String, tags: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onUpdateMissionNotesAndTags: (missionId: Long, notes: String, tags: String) -> Unit = { _, _, _ -> },
    onDeleteDailyMission: (Long) -> Unit = {},
    onToggleDailyMissionReminder: (Boolean) -> Unit = {},
    onSetDailyMissionReminderTime: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onTestDailyMissionReminder: () -> Unit = {},
    onTestBailOut: () -> Unit = {},
    onToggleBailOutHaptics: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var selectedTierFilter by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Tactical Segmented Switcher (Tabs)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900, RoundedCornerShape(10.dp))
                .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                Triple("ĐẤU RANK", Icons.Default.SportsKabaddi, 0),
                Triple("NHIỆM VỤ", Icons.Default.MilitaryTech, 1),
                Triple("XẾP HẠNG", Icons.Default.EmojiEvents, 2),
                Triple("NHẬT KÝ", Icons.Default.History, 3)
            )

            tabs.forEach { (title, icon, index) ->
                val selected = uiState.soloRankTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selected) Cyan500 else Color.Transparent)
                        .clickable { onSelectTab(index) }
                        .padding(vertical = 8.dp)
                        .testTag("solo_rank_tab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) Slate950 else Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (selected) Slate950 else Slate400
                        )
                    }
                }
            }
        }

        // Tactical Stats Overview Bar
        val stats = uiState.historyStats
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                .testTag("solo_rank_quick_stats"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Win Rate
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TỶ LỆ THẮNG",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "${stats.winRatePercent}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (stats.winRatePercent >= 50) GreenSuccess else Cyan400
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate800)
                )

                // Record (W - L)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "KẾT QUẢ",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "${stats.wins}W - ${stats.losses}L",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate800)
                )

                // Streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CHUỖI",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = if (stats.currentStreak > 0)
                            "${stats.currentStreak}x ${if (stats.isWinningStreak) "W" else "L"}"
                        else "-",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (stats.isWinningStreak) GreenSuccess else RankSRank
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp)
                        .background(Slate800)
                )

                // Highest roll
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ĐỔ CAO NHẤT",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "${stats.highestRoll}/50",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }
            }
        }

        // Firebase Cloud Rank Synchronization Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                .testTag("solo_rank_cloud_sync_bar"),
            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.85f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Cyan500.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = "Border Cloud Sync",
                            tint = Cyan400,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BORDER CLOUD (FIRESTORE)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Slate200
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(GreenSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "ONLINE",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = GreenSuccess
                                )
                            }
                        }
                        Text(
                            text = uiState.cloudSyncStatus ?: "Sẵn sàng đồng bộ điểm Rank & BXH lên Cloud",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onSyncCloud,
                    enabled = !uiState.isSyncingRankToCloud,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Cyan400,
                        disabledContentColor = Slate600
                    ),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("sync_rank_cloud_button")
                ) {
                    if (uiState.isSyncingRankToCloud) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Cyan400,
                            strokeWidth = 1.5.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ĐANG ĐỒNG BỘ...",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Đồng bộ Cloud",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ĐỒNG BỘ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Body Content based on active tab
        when (uiState.soloRankTab) {
            0 -> ArenaCombatTab(
                uiState = uiState,
                onStartMatch = onStartMatch
            )
            1 -> DailyMissionsTab(
                uiState = uiState,
                onToggleMission = onToggleDailyMission,
                onAddMission = onAddDailyMission,
                onUpdateMissionNotesAndTags = onUpdateMissionNotesAndTags,
                onDeleteMission = onDeleteDailyMission,
                onToggleReminder = onToggleDailyMissionReminder,
                onSetReminderTime = onSetDailyMissionReminderTime,
                onTestReminderNotification = onTestDailyMissionReminder,
                onTestBailOut = onTestBailOut,
                onToggleBailOutHaptics = onToggleBailOutHaptics,
                onToggleSound = onToggleSound
            )
            2 -> LeaderboardTab(
                leaderboard = uiState.leaderboard,
                selectedTier = selectedTierFilter,
                onSelectTier = { selectedTierFilter = it }
            )
            3 -> HistoryLoggingTab(
                matches = uiState.matchHistory,
                stats = uiState.historyStats,
                missions = uiState.completedMissionHistory.ifEmpty {
                    uiState.allMissionsHistory.ifEmpty { uiState.dailySoloRankMissions }
                },
                activeFilter = uiState.historyFilter,
                onFilterChange = onSelectHistoryFilter,
                onRequestClear = { showClearConfirmDialog = true },
                onNavigateToArena = { onSelectTab(0) }
            )
        }
    }

    // Confirmation dialog for clearing history
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "XÓA NHẬT KÝ TÁC CHIẾN?",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = RankSRank
                )
            },
            text = {
                Text(
                    text = "Hành động này sẽ xóa vĩnh viễn toàn bộ lịch sử các trận đấu Rank trong cơ sở dữ liệu Room cục bộ. Điểm Trion của đặc vụ không bị ảnh hưởng.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = Slate300,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RankSRank,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "XÓA DỮ LIỆU",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "HỦY",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            },
            containerColor = Slate900,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, RankSRank.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        )
    }
}

// -------------------------------------------------------------
// TAB 0: COMBAT ARENA
// -------------------------------------------------------------
@Composable
private fun ArenaCombatTab(
    uiState: AgentUiState,
    onStartMatch: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                    .testTag("solo_rank_arena_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Glow line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Slate900, Cyan500, Slate900)
                                )
                            )
                    )

                    Text(
                        text = "ĐẤU RANK ĐƠN - TRƯỜNG ĐẤU BORDER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Quy tắc: Tích lũy đủ 30 phút tập trung ở Trion Engine để kích hoạt 1 lượt Đấu Rank. Bạn và đối thủ (đặc vụ mô phỏng của Border) sẽ cùng đổ xúc xắc (1 đến 50). Người có điểm cao hơn sẽ giành chiến thắng, nhận điểm thưởng tương ứng và được ghi nhận vào Bảng Xếp Hạng & Nhật Ký Tác Chiến.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    // Accumulation Progress Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Slate800, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = Slate950),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TIẾN TRÌNH NĂNG LƯỢNG:",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate500
                                )
                                Text(
                                    text = if (uiState.rankUnlocked) "SẴN SÀNG" else "CẦN TÍCH LŨY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (uiState.rankUnlocked) GreenSuccess else Cyan400
                                )
                            }

                            Text(
                                text = "${uiState.focusTimeForRank / 60} phút / 30 phút",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )

                            val progressFraction = (uiState.focusTimeForRank / 1800f).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Cyan500,
                                trackColor = Slate800
                            )
                        }
                    }

                    // Unlock message or hint
                    if (uiState.rankUnlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Cyan950.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                .border(1.dp, Cyan500.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LƯỢT ĐẤU RANK ĐÃ MỞ KHÓA! SẴN SÀNG TRIỂN KHAI.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Text(
                            text = "Hãy vào tab 'Huấn luyện' và bật đồng hồ tập trung để tích lũy đủ 30 phút.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Dice Roll Button
                    Button(
                        onClick = onStartMatch,
                        enabled = uiState.rankUnlocked && !uiState.isRolling,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Cyan500,
                            contentColor = Slate950,
                            disabledContainerColor = Slate800,
                            disabledContentColor = Slate500
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("solo_rank_roll_button")
                    ) {
                        if (uiState.isRolling) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Slate950,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ĐANG ĐỔ XÚC XẮC ĐỐI KHÁNG...",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KHIÊU CHIẾN ĐẤU RANK (1-50)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Live Battle Result Card
                    uiState.matchResult?.let { result ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            val isWin = result.won
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        if (isWin) GreenSuccess.copy(alpha = 0.7f) else RankSRank.copy(alpha = 0.7f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .testTag("match_result_card"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isWin) GreenSuccessDark.copy(alpha = 0.25f) else RedFailDark.copy(alpha = 0.25f)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isWin) "CHIẾN THẮNG TRONG TRẬN ĐẤU RANK!" else "THẤT BẠI TRONG TRẬN ĐẤU RANK!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isWin) GreenSuccess else RankSRank
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceAround
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${uiState.agentName} (Bạn):",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate400
                                            )
                                            Text(
                                                text = "${result.playerRoll}",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isWin) GreenSuccess else Slate200
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Đối thủ Border:",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate400
                                            )
                                            Text(
                                                text = "${result.enemyRoll}",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (!isWin) RankSRank else Slate200
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isWin)
                                            "+${result.pointsDiff} PTS đã được cộng vào điểm Trion và lưu vào Nhật ký."
                                        else
                                            "-${result.pointsDiff} PTS đã bị trừ khỏi điểm Trion và lưu vào Nhật ký.",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isWin) GreenSuccess else RankSRank,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 1: BẢNG XẾP HẠNG (LEADERBOARD)
// -------------------------------------------------------------
@Composable
private fun LeaderboardTab(
    leaderboard: List<LeaderboardAgent>,
    selectedTier: String,
    onSelectTier: (String) -> Unit
) {
    val filteredList = remember(leaderboard, selectedTier) {
        if (selectedTier == "ALL") leaderboard
        else leaderboard.filter { it.rankTier == selectedTier }
    }

    val userEntry = remember(leaderboard) {
        leaderboard.firstOrNull { it.isUser }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // User's Standings Banner
        userEntry?.let { user ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Cyan400, RoundedCornerShape(12.dp))
                        .testTag("leaderboard_user_banner"),
                    colors = CardDefaults.cardColors(containerColor = Cyan950.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Cyan500, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${user.rankPosition}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate950
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate100
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Cyan500.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "BẠN",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Cyan300
                                        )
                                    }
                                }
                                Text(
                                    text = "${user.role} • ${user.triggerClass}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate400
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${user.points} PTS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                            Text(
                                text = user.rankTier,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = user.tierColor
                            )
                        }
                    }
                }
            }
        }

        // Tier Filter Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "S-RANK", "A-RANK", "B-RANK", "C-RANK").forEach { tier ->
                    val isSelected = selectedTier == tier
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Cyan500 else Slate900)
                            .border(1.dp, if (isSelected) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                            .clickable { onSelectTier(tier) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tier == "ALL") "TẤT CẢ" else tier,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSelected) Slate950 else Slate300
                        )
                    }
                }
            }
        }

        // Agents List
        items(filteredList, key = { "${it.name}_${it.rankPosition}" }) { agent ->
            val isUser = agent.isUser
            val isTop1 = agent.rankPosition == 1
            val isTop2 = agent.rankPosition == 2
            val isTop3 = agent.rankPosition == 3

            val badgeColor = when {
                isTop1 -> Color(0xFFFFD700) // Gold
                isTop2 -> Color(0xFFC0C0C0) // Silver
                isTop3 -> Color(0xFFCD7F32) // Bronze
                else -> Slate700
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isUser) 1.5.dp else 1.dp,
                        color = if (isUser) Cyan400 else if (isTop1) Color(0xFFFFD700).copy(alpha = 0.5f) else Slate800,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .testTag("leaderboard_agent_${agent.rankPosition}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) Cyan950.copy(alpha = 0.2f) else Slate900
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Rank Number Badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#${agent.rankPosition}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = if (isTop1 || isTop2 || isTop3) badgeColor else Slate300
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = agent.name,
                                    fontSize = 11.sp,
                                    fontWeight = if (isUser) FontWeight.Black else FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isUser) Cyan300 else Slate100,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (isUser) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Cyan500, RoundedCornerShape(3.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "BẠN",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Slate950
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${agent.squad} • ${agent.role}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "Trigger: ${agent.triggerClass} (WR: ${agent.winRate})",
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate500,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${agent.points} PTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isUser) Cyan300 else Slate200
                        )

                        Box(
                            modifier = Modifier
                                .background(agent.tierColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, agent.tierColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = agent.rankTier,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = agent.tierColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: NHẬT KÝ TÁC CHIẾN (HISTORICAL PERFORMANCE LOGGING & D3/RECHARTS VISUALIZATION)
// -------------------------------------------------------------
@Composable
private fun HistoryLoggingTab(
    matches: List<SoloRankMatchEntity>,
    stats: com.example.model.SoloRankHistoryStats,
    missions: List<DailySoloRankMissionEntity> = emptyList(),
    activeFilter: String,
    onFilterChange: (String) -> Unit,
    onRequestClear: () -> Unit,
    onNavigateToArena: () -> Unit = {}
) {
    var historySubTab by remember { mutableIntStateOf(0) } // 0: BIỂU ĐỒ (D3/RECHARTS), 1: DANH SÁCH CHI TIẾT
    val filteredMatches = remember(matches, activeFilter) {
        when (activeFilter) {
            "WIN" -> matches.filter { it.won }
            "LOSS" -> matches.filter { !it.won }
            else -> matches
        }
    }

    val dateFormatter = remember { SimpleDateFormat("HH:mm • dd/MM/yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Sub-Tab Switcher: [BIỂU ĐỒ (D3/RECHARTS)] vs [DANH SÁCH CHI TIẾT]
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900, RoundedCornerShape(8.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (historySubTab == 0) Cyan500.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (historySubTab == 0) Cyan400 else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { historySubTab = 0 }
                        .padding(vertical = 7.dp)
                        .testTag("history_subtab_chart"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = if (historySubTab == 0) Cyan300 else Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BIỂU ĐỒ (D3/RECHARTS)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (historySubTab == 0) Cyan300 else Slate400
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (historySubTab == 1) Cyan500.copy(alpha = 0.22f) else Color.Transparent)
                        .border(
                            1.dp,
                            if (historySubTab == 1) Cyan400 else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { historySubTab = 1 }
                        .padding(vertical = 7.dp)
                        .testTag("history_subtab_list"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (historySubTab == 1) Cyan300 else Slate400,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DANH SÁCH (${matches.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (historySubTab == 1) Cyan300 else Slate400
                        )
                    }
                }
            }
        }

        if (historySubTab == 0) {
            // Visualizer View (D3/Recharts Area, Bars, Delta + Inspection)
            item {
                SoloRankPerformanceVisualization(
                    matches = matches,
                    stats = stats,
                    missions = missions,
                    onNavigateToArena = onNavigateToArena
                )
            }

            // Mission Performance Analytics Section (Average Missions Per Week & Most Productive Day of the Week)
            item {
                MissionPerformanceAnalyticsSection(
                    missions = missions,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            // Quick Stats summary Card
            item {
                HistoryStatsOverviewCard(
                    matches = matches,
                    stats = stats,
                    onRequestClear = onRequestClear
                )
            }

            // Recent Match Highlights Header if matches exist
            if (matches.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRẬN ĐẤU GẦN ĐÂY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                        Text(
                            text = "Xem tất cả (${matches.size}) →",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400,
                            modifier = Modifier.clickable { historySubTab = 1 }
                        )
                    }
                }

                // Show top 3 recent matches
                items(matches.take(3), key = { "recent_${it.id}" }) { match ->
                    SoloRankMatchHistoryCard(match = match, dateFormatter = dateFormatter)
                }
            }
        } else {
            // Detailed List View
            item {
                HistoryStatsOverviewCard(
                    matches = matches,
                    stats = stats,
                    onRequestClear = onRequestClear
                )
            }

            // Filter Bar (All / Wins / Losses)
            item {
                HistoryFilterBar(
                    matchesCount = matches.size,
                    winsCount = stats.wins,
                    lossesCount = stats.losses,
                    activeFilter = activeFilter,
                    onFilterChange = onFilterChange
                )
            }

            // Empty state
            if (filteredMatches.isEmpty()) {
                item {
                    HistoryEmptyStateCard()
                }
            }

            // Matches List
            items(filteredMatches, key = { it.id }) { match ->
                SoloRankMatchHistoryCard(match = match, dateFormatter = dateFormatter)
            }
        }
    }
}

@Composable
private fun HistoryStatsOverviewCard(
    matches: List<SoloRankMatchEntity>,
    stats: com.example.model.SoloRankHistoryStats,
    onRequestClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .testTag("history_stats_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "THỐNG KÊ CHIẾN TÍCH (ROOM PERSISTENCE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400
                    )
                }

                if (matches.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa lịch sử",
                        tint = Slate400,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRequestClear() }
                            .testTag("btn_clear_history")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tổng điểm thu:",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "+${stats.totalPointsGained} PTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = GreenSuccess
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Tổng điểm mất:",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "-${stats.totalPointsLost} PTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = RankSRank
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val net = stats.totalPointsGained - stats.totalPointsLost
                    Text(
                        text = "Hiệu số ròng:",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "${if (net >= 0) "+" else ""}$net PTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (net >= 0) Cyan300 else RankSRank
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterBar(
    matchesCount: Int,
    winsCount: Int,
    lossesCount: Int,
    activeFilter: String,
    onFilterChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val filters = listOf(
            Pair("ALL", "TẤT CẢ ($matchesCount)"),
            Pair("WIN", "CHIẾN THẮNG ($winsCount)"),
            Pair("LOSS", "THẤT BẠI ($lossesCount)")
        )

        filters.forEach { (key, label) ->
            val selected = activeFilter == key
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selected) Cyan500 else Slate900)
                    .border(1.dp, if (selected) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                    .clickable { onFilterChange(key) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("filter_$key"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                    fontFamily = FontFamily.Monospace,
                    color = if (selected) Slate950 else Slate300
                )
            }
        }
    }
}

@Composable
private fun HistoryEmptyStateCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .padding(top = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = Slate500,
                modifier = Modifier.size(36.dp)
            )
            Text(
                text = "CHƯA CÓ DỮ LIỆU TÁC CHIẾN NÀO",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate300
            )
            Text(
                text = "Hãy tích lũy đủ 30 phút tập trung ở Trion Engine để kích hoạt lượt đấu và bắt đầu ghi nhận nhật ký trận đấu vào cơ sở dữ liệu Room.",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SoloRankMatchHistoryCard(
    match: SoloRankMatchEntity,
    dateFormatter: SimpleDateFormat
) {
    val isWin = match.won
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (isWin) GreenSuccess.copy(alpha = 0.5f) else RankSRank.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("match_history_item_${match.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isWin) GreenSuccessDark.copy(alpha = 0.15f) else RedFailDark.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Win/Loss badge, Timestamp & Points Diff
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (isWin) GreenSuccess.copy(alpha = 0.2f) else RankSRank.copy(alpha = 0.2f),
                                RoundedCornerShape(4.dp)
                            )
                            .border(
                                1.dp,
                                if (isWin) GreenSuccess else RankSRank,
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isWin) "VICTORY" else "DEFEAT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = if (isWin) GreenSuccess else RankSRank
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = dateFormatter.format(Date(match.timestamp)),
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }

                // Points delta
                Text(
                    text = if (isWin) "+${match.pointsDiff} PTS" else "-${match.pointsDiff} PTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isWin) GreenSuccess else RankSRank
                )
            }

            // Opponent & Roll showdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ĐỐI THỦ: ${match.opponentName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200
                    )
                    Text(
                        text = "Cấp bậc: ${match.opponentRank}",
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${match.agentName}: ",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                    Text(
                        text = "${match.playerRoll}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (isWin) GreenSuccess else Slate200
                    )
                    Text(
                        text = " vs ",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "${match.enemyRoll}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (!isWin) RankSRank else Slate200
                    )
                }
            }

            // Tactic Note & Final Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chiến thuật: ${match.tacticNote}",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Dư: ${match.finalPoints} PTS",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
            }
        }
    }
}
