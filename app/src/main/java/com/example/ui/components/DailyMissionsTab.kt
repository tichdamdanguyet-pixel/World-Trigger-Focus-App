package com.example.ui.components

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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.local.entity.DailySoloRankMissionEntity
import com.example.ui.AgentUiState
import com.example.util.MissionCsvExporter
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.RankSRank
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

@Composable
fun DailyMissionsTab(
    uiState: AgentUiState,
    onToggleMission: (DailySoloRankMissionEntity) -> Unit,
    onAddMission: (title: String, description: String, targetCount: Int, rewardPoints: Int, category: String, notes: String, tags: String) -> Unit,
    onUpdateMissionNotesAndTags: (missionId: Long, notes: String, tags: String) -> Unit = { _, _, _ -> },
    onDeleteMission: (Long) -> Unit,
    onToggleReminder: (Boolean) -> Unit = {},
    onSetReminderTime: (hour: Int, minute: Int) -> Unit = { _, _ -> },
    onTestReminderNotification: () -> Unit = {},
    onTestBailOut: () -> Unit = {},
    onToggleBailOutHaptics: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val missions = uiState.dailySoloRankMissions
    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0: Nhiệm vụ hôm nay, 1: Lịch sử hoàn thành (Room DB)
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var pendingCsvContent by remember { mutableStateOf("") }
    var missionToEditNotesAndTags by remember { mutableStateOf<DailySoloRankMissionEntity?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null && pendingCsvContent.isNotEmpty()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(MissionCsvExporter.UTF8_BOM)
                    outputStream.write(pendingCsvContent.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                }
                Toast.makeText(
                    context,
                    "Đã lưu lịch sử nhiệm vụ thành công vào tệp CSV!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Lỗi khi lưu tệp CSV: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val completedCount = missions.count { it.isCompleted }
    val totalCount = missions.size
    val totalRewardAvailable = missions.filter { it.isCompleted }.sumOf { it.rewardPoints }
    val progressFraction = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    val filteredMissions = remember(missions, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            missions
        } else {
            missions.filter { it.missionCategory == selectedCategoryFilter }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Daily Mission Stats Dashboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Cyan500.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .testTag("daily_missions_summary_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Cyan500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MilitaryTech,
                                contentDescription = "Daily Rank War Missions",
                                tint = Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NHIỆM VỤ TÁC CHIẾN HÔM NAY",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                color = Slate200
                            )
                            Text(
                                text = "Local Room DB • Solo Rank War Daily Tracking",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = { activeSubTab = 1 },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GreenSuccess
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_view_completed_history")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Lịch sử hoàn thành",
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LỊCH SỬ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (uiState.completedMissionHistory.isEmpty() && missions.none { it.isCompleted }) {
                                    Toast.makeText(context, "Chưa có dữ liệu lịch sử nhiệm vụ để xuất CSV!", Toast.LENGTH_SHORT).show()
                                } else {
                                    showExportDialog = true
                                }
                            },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cyan300
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_top_export_csv")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Xuất CSV",
                                modifier = Modifier.size(13.dp),
                                tint = Cyan300
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "XUẤT CSV",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Cyan400
                            ),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_add_daily_mission")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Thêm nhiệm vụ",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "THÊM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Progress row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIẾN ĐỘ HOÀN THÀNH",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                    Text(
                        text = "$completedCount/$totalCount HOÀN TẤT (${(progressFraction * 100).toInt()}%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (completedCount == totalCount && totalCount > 0) GreenSuccess else Cyan300
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (completedCount == totalCount && totalCount > 0) GreenSuccess else Cyan400,
                    trackColor = Slate800
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Điểm thưởng đã tích lũy từ nhiệm vụ:",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "+$totalRewardAvailable PTS Trion",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = GreenSuccess
                    )
                }
            }
        }

        // Tactical Sub-Tab Switcher: Today's Missions vs Completed Missions History (Room DB)
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
                    .background(if (activeSubTab == 0) Cyan500.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (activeSubTab == 0) Cyan400 else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeSubTab = 0 }
                    .padding(vertical = 7.dp)
                    .testTag("subtab_today_missions"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = if (activeSubTab == 0) Cyan300 else Slate400,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "HÔM NAY (${missions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (activeSubTab == 0) Cyan300 else Slate400
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (activeSubTab == 1) GreenSuccess.copy(alpha = 0.22f) else Color.Transparent)
                    .border(
                        1.dp,
                        if (activeSubTab == 1) GreenSuccess else Color.Transparent,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { activeSubTab = 1 }
                    .padding(vertical = 7.dp)
                    .testTag("subtab_completed_history"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (activeSubTab == 1) GreenSuccess else Slate400,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LỊCH SỬ (${uiState.completedMissionHistory.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (activeSubTab == 1) GreenSuccess else Slate400
                    )
                }
            }
        }

        if (activeSubTab == 0) {
        // Daily Mission Local Reminder Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (uiState.isDailyMissionReminderEnabled) Cyan500.copy(alpha = 0.5f) else Slate800,
                    shape = RoundedCornerShape(10.dp)
                )
                .testTag("card_daily_mission_reminder"),
            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .background(
                                    if (uiState.isDailyMissionReminderEnabled) Cyan500.copy(alpha = 0.2f) else Slate800
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isDailyMissionReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                contentDescription = "Alarm Notification",
                                tint = if (uiState.isDailyMissionReminderEnabled) Cyan400 else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "NHẮC NHỞ TÁC CHIẾN HẰNG NGÀY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (uiState.isDailyMissionReminderEnabled) Slate200 else Slate400
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (uiState.isDailyMissionReminderEnabled) Cyan500.copy(alpha = 0.2f) else Slate800)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = String.format(
                                            java.util.Locale.getDefault(),
                                            "%02d:%02d",
                                            uiState.dailyMissionReminderHour,
                                            uiState.dailyMissionReminderMinute
                                        ),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (uiState.isDailyMissionReminderEnabled) Cyan300 else Slate500
                                    )
                                }
                            }
                            Text(
                                text = if (uiState.isDailyMissionReminderEnabled) {
                                    "Báo động nếu chưa hoàn thành nhiệm vụ Solo Rank War trước giờ hẹn"
                                } else {
                                    "Tính năng nhắc nhở đang tắt"
                                },
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Switch(
                        checked = uiState.isDailyMissionReminderEnabled,
                        onCheckedChange = { onToggleReminder(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Slate950,
                            checkedTrackColor = Cyan400,
                            uncheckedThumbColor = Slate400,
                            uncheckedTrackColor = Slate800
                        ),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .testTag("switch_daily_mission_reminder")
                    )
                }

                // Quick Action Bar: Adjust Set Time & Test Notification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showTimePickerDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("btn_change_reminder_time"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Cyan400
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan500.copy(alpha = 0.4f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Change Time",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ĐỔI GIỜ NHẮC",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    OutlinedButton(
                        onClick = { onTestReminderNotification() },
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("btn_test_reminder_notification"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Slate300
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Test Notification",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "THỬ THÔNG BÁO",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Bail Out Audio & Tactical Haptics Notification Trigger Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (uiState.isTimerSoundEnabled || uiState.isBailOutHapticsEnabled) Cyan500.copy(alpha = 0.45f) else Slate800,
                    shape = RoundedCornerShape(10.dp)
                )
                .testTag("card_bail_out_feedback"),
            colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                .background(Cyan500.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = "Bail Out Trigger",
                                tint = Cyan300,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TÍN HIỆU HOÀN THÀNH: BAIL OUT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate200
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Cyan500.copy(alpha = 0.2f))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SOUND & HAPTIC",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Cyan300
                                    )
                                }
                            }
                            Text(
                                text = "Phát âm thanh 'Bail Out' & rung phản hồi đa nhịp khi nhiệm vụ hoàn thành",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Test Bail Out Trigger Button
                    OutlinedButton(
                        onClick = { onTestBailOut() },
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_test_bail_out_trigger"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Cyan300
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Thử Bail Out",
                            modifier = Modifier.size(13.dp),
                            tint = Cyan300
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "THỬ BAIL OUT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Quick Toggle Controls for Sound and Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sound Toggle Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (uiState.isTimerSoundEnabled) Cyan500.copy(alpha = 0.15f) else Slate800)
                            .border(
                                1.dp,
                                if (uiState.isTimerSoundEnabled) Cyan400.copy(alpha = 0.5f) else Slate700,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleSound() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("toggle_bail_out_sound"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isTimerSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (uiState.isTimerSoundEnabled) Cyan300 else Slate500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isTimerSoundEnabled) "Âm thanh: BẬT" else "Âm thanh: TẮT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.isTimerSoundEnabled) Cyan300 else Slate400
                            )
                        }
                    }

                    // Haptics Toggle Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (uiState.isBailOutHapticsEnabled) Cyan500.copy(alpha = 0.15f) else Slate800)
                            .border(
                                1.dp,
                                if (uiState.isBailOutHapticsEnabled) Cyan400.copy(alpha = 0.5f) else Slate700,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleBailOutHaptics() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("toggle_bail_out_haptics"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (uiState.isBailOutHapticsEnabled) Cyan300 else Slate500,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (uiState.isBailOutHapticsEnabled) "Rung chiến thuật: BẬT" else "Rung: TẮT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (uiState.isBailOutHapticsEnabled) Cyan300 else Slate400
                            )
                        }
                    }
                }
            }
        }

        // Category Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                "ALL" to "TẤT CẢ",
                "SOLO_WAR" to "ĐẤU RANK",
                "WIN_MATCH" to "THẮNG",
                "FOCUS_TRAINING" to "POMODORO",
                "STUDY_TACTIC" to "CHIẾN THUẬT"
            )

            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategoryFilter == catKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Slate800 else Slate900)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Cyan400 else Slate800,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedCategoryFilter = catKey }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = catLabel,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) Cyan300 else Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Missions List
        if (filteredMissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Không có nhiệm vụ nào trong danh mục này",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Slate400
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("daily_missions_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMissions, key = { it.id }) { mission ->
                    DailyMissionItemCard(
                        mission = mission,
                        onToggle = { onToggleMission(mission) },
                        onDelete = { onDeleteMission(mission.id) },
                        onEditNotesAndTags = { missionToEditNotesAndTags = mission }
                    )
                }
            }
        }
        } else {
            CompletedMissionsHistoryListView(
                completedMissions = uiState.completedMissionHistory,
                onExportCsv = {
                    if (uiState.completedMissionHistory.isEmpty()) {
                        Toast.makeText(context, "Chưa có dữ liệu lịch sử nhiệm vụ để xuất CSV!", Toast.LENGTH_SHORT).show()
                    } else {
                        showExportDialog = true
                    }
                },
                onEditNotesAndTags = { missionToEditNotesAndTags = it },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Add Mission Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var customNotes by remember { mutableStateOf("") }
        var customTags by remember { mutableStateOf("") }
        var targetCountText by remember { mutableStateOf("1") }
        var rewardPointsText by remember { mutableStateOf("150") }
        var selectedCat by remember { mutableStateOf("SOLO_WAR") }

        val quickTagPresets = listOf("Rank War", "Solo War", "Chiến thuật", "Pomodoro", "Kogetsu", "Sniper", "Ưu tiên")

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "THÊM NHIỆM VỤ SOLO RANK WAR",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Cyan300
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Tiêu đề nhiệm vụ *", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mission_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Mô tả chiến thuật & chỉ tiêu", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mission_desc"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = Slate700
                        )
                    )

                    // Custom Notes input
                    OutlinedTextField(
                        value = customNotes,
                        onValueChange = { customNotes = it },
                        label = { Text("Ghi chú cá nhân (Custom Notes)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("vd: Sử dụng khiên chắn Kogetsu phản kích...", fontSize = 9.sp, color = Slate500) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mission_notes"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = Slate700
                        ),
                        maxLines = 2
                    )

                    // Custom Tags input
                    OutlinedTextField(
                        value = customTags,
                        onValueChange = { customTags = it },
                        label = { Text("Nhãn tag (Custom Tags, cách bằng dấu phẩy)", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                        placeholder = { Text("vd: Solo War, Rank A, Kogetsu", fontSize = 9.sp, color = Slate500) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_mission_tags"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan400,
                            unfocusedBorderColor = Slate700
                        ),
                        singleLine = true
                    )

                    // Quick Tag Suggestion Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        quickTagPresets.forEach { preset ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate800)
                                    .border(0.5.dp, Slate700, RoundedCornerShape(4.dp))
                                    .clickable {
                                        customTags = if (customTags.isBlank()) preset else "$customTags, $preset"
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+#$preset",
                                    fontSize = 8.5.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate300
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetCountText,
                            onValueChange = { targetCountText = it },
                            label = { Text("Chỉ tiêu (lần/phút)", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_mission_target"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = Slate700
                            )
                        )

                        OutlinedTextField(
                            value = rewardPointsText,
                            onValueChange = { rewardPointsText = it },
                            label = { Text("Thưởng Trion (PTS)", fontSize = 9.sp, fontFamily = FontFamily.Monospace) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_mission_reward"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Cyan400,
                                unfocusedBorderColor = Slate700
                            )
                        )
                    }

                    Text(
                        text = "Danh mục nhiệm vụ:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )

                    val catOptions = listOf(
                        "SOLO_WAR" to "Đấu Rank",
                        "WIN_MATCH" to "Thắng Trận",
                        "FOCUS_TRAINING" to "Pomodoro Trion",
                        "STUDY_TACTIC" to "Chiến thuật"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        catOptions.forEach { (catCode, catName) ->
                            val isSel = selectedCat == catCode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSel) Cyan500 else Slate800)
                                    .clickable { selectedCat = catCode }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = catName,
                                    fontSize = 8.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSel) Slate950 else Slate300
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val target = targetCountText.toIntOrNull() ?: 1
                            val reward = rewardPointsText.toIntOrNull() ?: 100
                            onAddMission(title, description, target, reward, selectedCat, customNotes, customTags)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("btn_save_daily_mission")
                ) {
                    Text(
                        text = "LƯU VÀO ROOM DB",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate950
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddDialog = false },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "HỦY",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }
            },
            containerColor = Slate900
        )
    }

    // Dialog: Cập nhật Ghi chú và Nhãn Tag cho Nhiệm vụ Room DB
    if (missionToEditNotesAndTags != null) {
        val mission = missionToEditNotesAndTags!!
        EditMissionNotesAndTagsDialog(
            mission = mission,
            onDismiss = { missionToEditNotesAndTags = null },
            onSave = { notes, tags ->
                onUpdateMissionNotesAndTags(mission.id, notes, tags)
            }
        )
    }

    // Dialog: Cấu hình giờ nhắc nhở nhiệm vụ tác chiến
    if (showTimePickerDialog) {
        var selectedHour by remember { mutableStateOf(uiState.dailyMissionReminderHour) }
        var selectedMinute by remember { mutableStateOf(uiState.dailyMissionReminderMinute) }

        val presetHours = listOf(18 to "18:00 (6 PM)", 20 to "20:00 (8 PM)", 21 to "21:00 (9 PM)", 22 to "22:00 (10 PM)")

        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ĐẶT GIỜ NHẮC NHỞ TÁC CHIẾN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hệ thống sẽ kiểm tra danh sách nhiệm vụ Solo Rank War hôm nay. Nếu còn nhiệm vụ chưa hoàn tất, một thông báo cục bộ kèm âm thanh sẽ được gửi đến thiết bị của bạn vào khung giờ đã chọn:",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300,
                        lineHeight = 16.sp
                    )

                    // Large Time Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Slate950)
                            .border(1.dp, Cyan500.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%02d : %02d", selectedHour, selectedMinute),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300
                        )
                    }

                    // Preset Quick-Picks
                    Text(
                        text = "Khung giờ gợi ý:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetHours.forEach { (h, label) ->
                            val isChosen = selectedHour == h && selectedMinute == 0
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isChosen) Cyan500.copy(alpha = 0.25f) else Slate950)
                                    .border(1.dp, if (isChosen) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                                    .clickable {
                                        selectedHour = h
                                        selectedMinute = 0
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d:00", h),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isChosen) Cyan300 else Slate400
                                )
                            }
                        }
                    }

                    // Stepper row for fine adjustments
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Giờ (0 - 23)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { selectedHour = (selectedHour - 1 + 24) % 24 },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text("-", fontSize = 14.sp, color = Cyan300)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", selectedHour),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate200,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                OutlinedButton(
                                    onClick = { selectedHour = (selectedHour + 1) % 24 },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 14.sp, color = Cyan300)
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Phút (0 - 59)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { selectedMinute = (selectedMinute - 15 + 60) % 60 },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text("-", fontSize = 14.sp, color = Cyan300)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%02d", selectedMinute),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate200,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                OutlinedButton(
                                    onClick = { selectedMinute = (selectedMinute + 15) % 60 },
                                    modifier = Modifier.size(32.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 14.sp, color = Cyan300)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetReminderTime(selectedHour, selectedMinute)
                        showTimePickerDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("btn_confirm_reminder_time")
                ) {
                    Text(
                        text = "LƯU GIỜ HẸN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate950
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showTimePickerDialog = false },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "ĐÓNG",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                }
            },
            containerColor = Slate900
        )
    }

    // Export Mission History to CSV Dialog
    if (showExportDialog) {
        val completedList = uiState.completedMissionHistory
        val todayMissions = missions
        var exportScope by remember { mutableStateOf(if (completedList.isNotEmpty()) 0 else 1) } // 0: Lịch sử hoàn tất (Room DB), 1: Tất cả nhiệm vụ hôm nay
        val selectedMissionsForExport = if (exportScope == 0) {
            completedList.ifEmpty { todayMissions.filter { it.isCompleted } }
        } else {
            todayMissions
        }

        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            containerColor = Slate900,
            shape = RoundedCornerShape(12.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "XUẤT DỮ LIỆU NHIỆM VỤ (CSV)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Trích xuất toàn bộ lịch sử nhiệm vụ thành tệp CSV tiêu chuẩn (hỗ trợ tiếng Việt có dấu trên Microsoft Excel và Google Sheets).",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300,
                        lineHeight = 15.sp
                    )

                    // Scope selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Slate950, RoundedCornerShape(8.dp))
                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (exportScope == 0) GreenSuccess.copy(alpha = 0.25f) else Color.Transparent)
                                .border(1.dp, if (exportScope == 0) GreenSuccess else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { exportScope = 0 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "LỊCH SỬ (${completedList.size})",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (exportScope == 0) GreenSuccess else Slate400
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (exportScope == 1) Cyan500.copy(alpha = 0.25f) else Color.Transparent)
                                .border(1.dp, if (exportScope == 1) Cyan400 else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { exportScope = 1 }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "HÔM NAY (${todayMissions.size})",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (exportScope == 1) Cyan300 else Slate400
                            )
                        }
                    }

                    // Metadata Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate950)
                            .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tên tệp dự kiến: ${MissionCsvExporter.getDefaultFileName()}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                            Text(
                                text = "Số lượng bản ghi: ${selectedMissionsForExport.size} nhiệm vụ",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                            Text(
                                text = "Mã hóa: UTF-8 with BOM (Hỗ trợ ký tự tiếng Việt)",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate500
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val csv = MissionCsvExporter.generateCsv(selectedMissionsForExport)
                        pendingCsvContent = csv
                        createDocumentLauncher.launch(MissionCsvExporter.getDefaultFileName())
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("btn_confirm_export_csv")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = Slate950,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LƯU TỆP CSV",
                        color = Slate950,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        val csv = MissionCsvExporter.generateCsv(selectedMissionsForExport)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(Intent.EXTRA_SUBJECT, "Lịch sử nhiệm vụ Solo Rank War")
                            putExtra(Intent.EXTRA_TEXT, csv)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Chia sẻ lịch sử nhiệm vụ (CSV)"))
                        showExportDialog = false
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700),
                    modifier = Modifier.testTag("btn_share_export_csv")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CHIA SẺ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp
                    )
                }
            }
        )
    }
}

@Composable
private fun EditMissionNotesAndTagsDialog(
    mission: DailySoloRankMissionEntity,
    onDismiss: () -> Unit,
    onSave: (notes: String, tags: String) -> Unit
) {
    var notes by remember { mutableStateOf(mission.customNotes) }
    var tags by remember { mutableStateOf(mission.customTags) }

    val presetTags = listOf(
        "Rank War",
        "Solo War",
        "Chiến thuật",
        "Pomodoro",
        "Kogetsu",
        "Sniper",
        "Rank A",
        "Ưu tiên cao"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Cyan500.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column {
                    Text(
                        text = "GHI CHÚ & NHÃN TAG CHIẾN THUẬT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Cyan300
                    )
                    Text(
                        text = mission.title,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.5.sp,
                        color = Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Notes Input Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = {
                        Text(
                            "Ghi chú chiến thuật (Custom Notes)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    placeholder = {
                        Text(
                            "Nhập ghi chú phản xạ, kinh nghiệm hoặc chiến thuật...",
                            fontSize = 9.sp,
                            color = Slate500
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp, max = 120.dp)
                        .testTag("input_edit_mission_notes"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = Slate700
                    ),
                    maxLines = 3
                )

                // Tags Input Field
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = {
                        Text(
                            "Nhãn tag (Custom Tags - phân cách bằng dấu phẩy)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    placeholder = {
                        Text(
                            "vd: Solo War, Kogetsu, Sniper, Rank A",
                            fontSize = 9.sp,
                            color = Slate500
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_edit_mission_tags"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = Slate700
                    ),
                    singleLine = true
                )

                // Quick presets
                Text(
                    text = "Gợi ý nhãn tag nhanh (chạm để thêm):",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Slate400
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetTags.forEach { preset ->
                        val currentTagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val isAlreadyPresent = currentTagList.any { it.equals(preset, ignoreCase = true) }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isAlreadyPresent) Cyan500.copy(alpha = 0.25f) else Slate800)
                                .border(0.5.dp, if (isAlreadyPresent) Cyan400 else Slate700, RoundedCornerShape(4.dp))
                                .clickable {
                                    if (isAlreadyPresent) {
                                        tags = currentTagList.filterNot { it.equals(preset, ignoreCase = true) }.joinToString(", ")
                                    } else {
                                        tags = if (tags.isBlank()) preset else "$tags, $preset"
                                    }
                                }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "#$preset",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyPresent) Cyan300 else Slate300
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(notes.trim(), tags.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("btn_save_notes_tags")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Slate950,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "LƯU VÀO ROOM DB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate950
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "HỦY",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
            }
        },
        containerColor = Slate900
    )
}

@Composable
private fun DailyMissionItemCard(
    mission: DailySoloRankMissionEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onEditNotesAndTags: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val categoryColor = when (mission.missionCategory) {
        "SOLO_WAR" -> Cyan400
        "WIN_MATCH" -> GreenSuccess
        "FOCUS_TRAINING" -> Color(0xFFFBBF24) // Amber
        "STUDY_TACTIC" -> Color(0xFFA78BFA) // Purple
        "COMBAT_STREAK" -> RankSRank
        else -> Slate400
    }

    val categoryLabel = when (mission.missionCategory) {
        "SOLO_WAR" -> "ĐẤU RANK"
        "WIN_MATCH" -> "CHIẾN THẮNG"
        "FOCUS_TRAINING" -> "POMODORO"
        "STUDY_TACTIC" -> "CHIẾN THUẬT"
        "COMBAT_STREAK" -> "CHUỖI THẮNG"
        else -> mission.missionCategory
    }

    val progressFraction = (mission.currentProgress.toFloat() / mission.targetCount.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (mission.isCompleted) GreenSuccess.copy(alpha = 0.5f) else Slate800,
                shape = RoundedCornerShape(10.dp)
            )
            .testTag("daily_mission_item_${mission.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (mission.isCompleted) Slate900.copy(alpha = 0.95f) else Slate900
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Category tag + Reward pill + Edit Notes/Tags + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = categoryColor
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GreenSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${mission.rewardPoints} PTS TRION",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditNotesAndTags,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("btn_edit_notes_tags_${mission.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Chỉnh sửa ghi chú và nhãn tag",
                            tint = if (mission.customNotes.isNotBlank() || mission.customTags.isNotBlank()) Cyan400 else Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Xóa nhiệm vụ",
                            tint = Slate600,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Title & Description
            Text(
                text = mission.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (mission.isCompleted) Slate300 else Slate100
            )

            if (mission.description.isNotBlank()) {
                Text(
                    text = mission.description,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400,
                    lineHeight = 14.sp
                )
            }

            // Custom Tags Badges (if present)
            if (mission.customTags.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Nhãn tags",
                        tint = Cyan400,
                        modifier = Modifier.size(12.dp)
                    )
                    mission.customTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate800)
                                .border(0.5.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .clickable { onEditNotesAndTags() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }
                    }
                }
            }

            // Custom Notes Container (if present)
            if (mission.customNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate950)
                        .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                        .clickable { onEditNotesAndTags() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Ghi chú chiến thuật",
                                tint = Cyan400,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mission.customNotes,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate300,
                                lineHeight = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa ghi chú",
                            tint = Slate500,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else if (mission.customTags.isBlank()) {
                // Subtle affordance when neither notes nor tags exist
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onEditNotesAndTags() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Thêm ghi chú & nhãn tag",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }
            }

            // Progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tiến độ: ${mission.currentProgress}/${mission.targetCount}",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}%",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (mission.isCompleted) GreenSuccess else Cyan400
                    )
                }

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.5.dp)),
                    color = if (mission.isCompleted) GreenSuccess else Cyan400,
                    trackColor = Slate800
                )
            }

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mission.isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Đã hoàn thành",
                            tint = GreenSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ĐÃ HOÀN THÀNH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }

                    OutlinedButton(
                        onClick = onToggle,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            text = "HOÀN TÁC",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Cyan400)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ĐANG TIẾN HÀNH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }

                    Button(
                        onClick = onToggle,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan500),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("btn_complete_mission_${mission.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Slate950,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "HOÀN THÀNH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Slate950
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedMissionsHistoryListView(
    completedMissions: List<DailySoloRankMissionEntity>,
    onExportCsv: () -> Unit = {},
    onEditNotesAndTags: (DailySoloRankMissionEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val filteredCompletedMissions = remember(completedMissions, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            completedMissions
        } else {
            completedMissions.filter { it.missionCategory == selectedCategoryFilter }
        }
    }

    val totalRewardEarned = remember(completedMissions) {
        completedMissions.sumOf { it.rewardPoints }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Summary Card for Room DB History
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GreenSuccess.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .testTag("completed_missions_history_summary_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(GreenSuccess.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed Missions History",
                            tint = GreenSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LỊCH SỬ TÁC CHIẾN ĐÃ HOÀN TẤT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Slate200
                        )
                        Text(
                            text = "${completedMissions.size} nhiệm vụ đã lưu trong Room Database",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "TỔNG THƯỞNG",
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                        Text(
                            text = "+$totalRewardEarned PTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Cyan300
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_export_missions_csv")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Xuất CSV",
                            modifier = Modifier.size(13.dp),
                            tint = Cyan300
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "XUẤT CSV",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Category filter chips for history
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val categories = listOf(
                "ALL" to "TẤT CẢ",
                "SOLO_WAR" to "ĐẤU RANK",
                "WIN_MATCH" to "THẮNG",
                "FOCUS_TRAINING" to "POMODORO",
                "STUDY_TACTIC" to "CHIẾN THUẬT"
            )

            categories.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategoryFilter == catKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Slate800 else Slate900)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) GreenSuccess else Slate800,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { selectedCategoryFilter = catKey }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = catLabel,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        color = if (isSelected) GreenSuccess else Slate400,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Empty state or List of completed missions
        if (filteredCompletedMissions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                    .testTag("completed_missions_empty_state"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "CHƯA CÓ LỊCH SỬ HOÀN THÀNH",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dữ liệu được lưu trữ tự động trong Room DB mỗi khi bạn hoàn thành nhiệm vụ Solo Rank War.",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Slate500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("completed_missions_history_list"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredCompletedMissions, key = { it.id }) { mission ->
                    CompletedMissionHistoryItemCard(
                        mission = mission,
                        onEditNotesAndTags = { onEditNotesAndTags(mission) }
                    )
                }
            }
        }
    }
}

@Composable
fun CompletedMissionHistoryItemCard(
    mission: DailySoloRankMissionEntity,
    onEditNotesAndTags: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val timestamp = mission.completedAt ?: mission.createdAt
    val timeFormatted = remember(timestamp) {
        SimpleDateFormat("HH:mm:ss • dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    val categoryLabel = when (mission.missionCategory) {
        "SOLO_WAR" -> "ĐẤU RANK"
        "WIN_MATCH" -> "THẮNG TRẬN"
        "FOCUS_TRAINING" -> "POMODORO"
        "STUDY_TACTIC" -> "CHIẾN THUẬT"
        "COMBAT_STREAK" -> "CHUỖI THẮNG"
        else -> mission.missionCategory
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GreenSuccess.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .testTag("completed_mission_item_${mission.id}"),
        colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Category tag, Title, Edit Button, Reward PTS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GreenSuccess.copy(alpha = 0.2f))
                            .border(1.dp, GreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = mission.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate100,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onEditNotesAndTags,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("btn_edit_completed_notes_tags_${mission.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Chỉnh sửa ghi chú và nhãn tag",
                            tint = if (mission.customNotes.isNotBlank() || mission.customTags.isNotBlank()) Cyan400 else Slate500,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate800)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${mission.rewardPoints} PTS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300
                        )
                    }
                }
            }

            if (mission.description.isNotBlank()) {
                Text(
                    text = mission.description,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400,
                    lineHeight = 14.sp
                )
            }

            // Custom Tags Badges (if present)
            if (mission.customTags.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Nhãn tags",
                        tint = Cyan400,
                        modifier = Modifier.size(12.dp)
                    )
                    mission.customTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate800)
                                .border(0.5.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .clickable { onEditNotesAndTags() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }
                    }
                }
            }

            // Custom Notes Container (if present)
            if (mission.customNotes.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate950)
                        .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                        .clickable { onEditNotesAndTags() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = "Ghi chú chiến thuật",
                                tint = Cyan400,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(top = 1.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mission.customNotes,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate300,
                                lineHeight = 13.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Sửa ghi chú",
                            tint = Slate500,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else if (mission.customTags.isBlank()) {
                // Subtle affordance when neither notes nor tags exist
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onEditNotesAndTags() }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = null,
                        tint = Slate500,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+ Thêm ghi chú & nhãn tag",
                        fontSize = 8.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }
            }

            // Room Database metadata: Date and Completed Timestamp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Slate950)
                    .border(0.5.dp, Slate800, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Thời gian hoàn tất",
                            tint = GreenSuccess,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Ngày ${mission.dateString} • $timeFormatted",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Slate300
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(GreenSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "${mission.currentProgress}/${mission.targetCount} HOÀN TẤT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = GreenSuccess
                        )
                    }
                }
            }
        }
    }
}
