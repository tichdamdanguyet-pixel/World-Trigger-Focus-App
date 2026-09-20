package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrainingMission
import com.example.ui.AgentUiState
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun TrainingScreen(
    uiState: AgentUiState,
    onCompleteMission: (id: Int, top15: Boolean) -> Unit,
    onStartPomodoroForMission: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Info Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                    .testTag("language_training_header"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "NHIỆM VỤ HUẤN LUYỆN NGÔN NGỮ SNIPER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Các bài tập ngôn ngữ chuyên sâu (5 bài/tuần) dành riêng cho Sniper. Bạn phải nỗ lực vượt trội và đạt xếp hạng Top 15% mới chính thức nhận được 10 điểm thưởng cho mỗi bài hoàn thành!",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val completedCount = uiState.trainingMissions.count { it.top15Achieved }
                    Text(
                        text = "Tiến độ: $completedCount / ${uiState.trainingMissions.size} bài đạt Top 15%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }
            }
        }

        // Mission Items
        items(uiState.trainingMissions, key = { it.id }) { mission ->
            val isAchieved = mission.top15Achieved
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isAchieved) Cyan500.copy(alpha = 0.6f) else Slate800,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .testTag("mission_item_${mission.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAchieved) Cyan950.copy(alpha = 0.2f) else Slate900
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mission.week,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400
                        )

                        if (isAchieved) {
                            Box(
                                modifier = Modifier
                                    .background(Cyan500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Cyan500.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+${mission.reward} PTS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300
                                )
                            }
                        }
                    }

                    Text(
                        text = mission.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200,
                        lineHeight = 18.sp
                    )

                    Text(
                        text = "Yêu cầu: Đạt Top 15% để nhận +${mission.reward} PTS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )

                    // Pomodoro study launcher button
                    OutlinedButton(
                        onClick = { onStartPomodoroForMission(mission.title) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mission_pomodoro_btn_${mission.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "HỌC POMODORO CHO BÀI NÀY (25M)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Toggle Button
                    if (isAchieved) {
                        Button(
                            onClick = { onCompleteMission(mission.id, false) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cyan500,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mission_toggle_btn_${mission.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ĐÃ ĐẠT TOP 15% (+10 PTS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onCompleteMission(mission.id, true) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Slate700, Slate700))),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mission_toggle_btn_${mission.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "XÁC NHẬN ĐẠT TOP 15%",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
