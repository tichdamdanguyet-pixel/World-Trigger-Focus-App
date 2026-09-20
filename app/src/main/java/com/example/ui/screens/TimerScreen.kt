package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PomodoroPhase
import com.example.ui.AgentUiState
import com.example.ui.components.PomodoroFocusTimer
import com.example.ui.components.StudyTaskManager
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

fun formatSeconds(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val mins = (totalSeconds % 3600) / 60
    val secs = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, mins, secs)
    } else {
        String.format("%02d:%02d", mins, secs)
    }
}

@Composable
fun TimerScreen(
    uiState: AgentUiState,
    onModeChange: (String) -> Unit,
    onToggleTimer: () -> Unit,
    onResetCountdown: () -> Unit,
    onFinishStopwatch: () -> Unit,
    onTogglePomodoro: () -> Unit,
    onResetPomodoro: () -> Unit,
    onSkipPomodoroPhase: () -> Unit,
    onSelectPomodoroPhase: (PomodoroPhase) -> Unit,
    onSelectPomodoroSubject: (String) -> Unit,
    onSelectActiveTask: (Long?) -> Unit = {},
    onAddStudyTask: (String, String, String, Int) -> Unit = { _, _, _, _ -> },
    onToggleStudyTask: (Long) -> Unit = {},
    onDeleteStudyTask: (Long) -> Unit = {},
    isSoundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    onTestSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode switch pills bar (Pomodoro, Countdown, Stopwatch)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate950, RoundedCornerShape(50))
                .border(1.dp, Slate800, RoundedCornerShape(50))
                .padding(4.dp)
                .testTag("timer_mode_selector"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPomodoro = uiState.timerMode == "pomodoro"
            val isCountdown = uiState.timerMode == "countdown"
            val isStopwatch = uiState.timerMode == "stopwatch"
            val isAnyRunning = uiState.isRunning || uiState.pomodoroState.isRunning

            // Pomodoro Mode Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isPomodoro) Cyan500 else Slate950)
                    .clickable(enabled = !isAnyRunning) { onModeChange("pomodoro") }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Pomodoro",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isPomodoro) Slate950 else Slate400
                )
            }

            // Countdown (1h) Mode Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isCountdown) Cyan500 else Slate950)
                    .clickable(enabled = !isAnyRunning) { onModeChange("countdown") }
                    .padding(vertical = 8.dp)
                    .testTag("mode_countdown_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "1 Giờ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isCountdown) Slate950 else Slate400
                )
            }

            // Stopwatch Mode Pill
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isStopwatch) Cyan500 else Slate950)
                    .clickable(enabled = !isAnyRunning) { onModeChange("stopwatch") }
                    .padding(vertical = 8.dp)
                    .testTag("mode_stopwatch_btn"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bấm giờ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isStopwatch) Slate950 else Slate400
                )
            }
        }

        // Active View based on selected timer mode
        if (uiState.timerMode == "pomodoro") {
            PomodoroFocusTimer(
                pomodoroState = uiState.pomodoroState,
                tasks = uiState.studyTasks,
                activeTaskId = uiState.activeStudyTaskId,
                isSoundEnabled = isSoundEnabled,
                onToggleSound = onToggleSound,
                onTestSound = onTestSound,
                onSelectActiveTask = onSelectActiveTask,
                onToggleTimer = onTogglePomodoro,
                onResetPomodoro = onResetPomodoro,
                onSkipPhase = onSkipPomodoroPhase,
                onSelectPhase = onSelectPomodoroPhase,
                onSelectSubject = onSelectPomodoroSubject
            )

            // Room Database Study Tasks Manager
            StudyTaskManager(
                tasks = uiState.studyTasks,
                activeTaskId = uiState.activeStudyTaskId,
                onSelectActiveTask = onSelectActiveTask,
                onAddTask = onAddStudyTask,
                onToggleTask = onToggleStudyTask,
                onDeleteTask = onDeleteStudyTask
            )
        } else {
            // Main Standard Countdown / Stopwatch Timer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Slate800, RoundedCornerShape(16.dp))
                    .testTag("timer_card"),
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top decorative glowing line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Slate900,
                                        Cyan500,
                                        Slate900
                                    )
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (uiState.timerMode == "countdown")
                            "TẬP TRUNG HUẤN LUYỆN (1H = +6 PTS)"
                        else
                            "HUẤN LUYỆN TỰ DO (10 PHÚT = +1 PTS)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Cyan400,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Digital Clock Display
                    Text(
                        text = formatSeconds(uiState.timerSeconds),
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400,
                        modifier = Modifier.testTag("timer_display")
                    )

                    if (uiState.timerMode == "stopwatch") {
                        val expectedPts = uiState.timerSeconds / 600
                        Text(
                            text = "Điểm dự kiến: +$expectedPts PTS",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Play/Pause button
                        Button(
                            onClick = onToggleTimer,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cyan500,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("timer_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isRunning) "TẠM DỪNG" else "BẮT ĐẦU",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (uiState.timerMode == "countdown") {
                            OutlinedButton(
                                onClick = onResetCountdown,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("timer_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RESET (1H)",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            val canFinish = uiState.timerSeconds > 0
                            Button(
                                onClick = onFinishStopwatch,
                                enabled = canFinish,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenSuccess,
                                    contentColor = Slate950,
                                    disabledContainerColor = Slate800,
                                    disabledContentColor = Slate500
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("timer_finish_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CHỐT SỔ",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Focus accumulation for Rank Wars
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                .testTag("rank_focus_progress_card"),
            colors = CardDefaults.cardColors(containerColor = Slate900),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "TÍCH LŨY MỞ KHÓA ĐẤU RANK:",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Slate400
                        )
                    }
                    Text(
                        text = "${uiState.focusTimeForRank / 60} / 30 phút",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan300
                    )
                }

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

                if (uiState.rankUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Cyan950.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .border(1.dp, Cyan500.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ĐÃ MỞ KHÓA LƯỢT ĐẤU RANK ĐƠN!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300
                        )
                    }
                } else {
                    Text(
                        text = "Tích lũy đủ 30 phút tập trung để tham gia Solo Rank Wars.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }
            }
        }
    }
}
