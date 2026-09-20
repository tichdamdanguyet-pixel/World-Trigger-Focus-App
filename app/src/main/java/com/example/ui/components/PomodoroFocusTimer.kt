package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.data.local.entity.StudyTaskEntity
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PomodoroPhase
import com.example.model.PomodoroSessionLog
import com.example.model.PomodoroState
import com.example.ui.screens.formatSeconds
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.RankBRank
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

val STUDY_SUBJECTS = listOf(
    "Ngoại ngữ Sniper",
    "Giải mã tín hiệu Aftokrator",
    "Phân tích tài liệu quân sự",
    "Chiến thuật ngắm bắn Ibis",
    "Tự học tự do"
)

@Composable
fun PomodoroFocusTimer(
    pomodoroState: PomodoroState,
    tasks: List<StudyTaskEntity> = emptyList(),
    activeTaskId: Long? = null,
    isSoundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    onTestSound: () -> Unit = {},
    onSelectActiveTask: (Long?) -> Unit = {},
    onToggleTimer: () -> Unit,
    onResetPomodoro: () -> Unit,
    onSkipPhase: () -> Unit,
    onSelectPhase: (PomodoroPhase) -> Unit,
    onSelectSubject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSubjectMenu by remember { mutableStateOf(false) }
    var selectorTagFilter by remember { mutableStateOf<String?>(null) }
    val activeTask = tasks.find { it.id == activeTaskId }

    val phaseColor by animateColorAsState(
        targetValue = when (pomodoroState.phase) {
            PomodoroPhase.FOCUS -> Cyan400
            PomodoroPhase.SHORT_BREAK -> GreenSuccess
            PomodoroPhase.LONG_BREAK -> Amber400
        },
        label = "phaseColor"
    )

    val phaseBgGlow by animateColorAsState(
        targetValue = when (pomodoroState.phase) {
            PomodoroPhase.FOCUS -> Cyan950.copy(alpha = 0.4f)
            PomodoroPhase.SHORT_BREAK -> Color(0xFF052e16).copy(alpha = 0.4f)
            PomodoroPhase.LONG_BREAK -> Color(0xFF451a03).copy(alpha = 0.4f)
        },
        label = "phaseBgGlow"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(16.dp))
            .testTag("pomodoro_timer_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glow top accent line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Slate900, phaseColor, Slate900)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Active Room Task Tracking Display
            if (activeTask != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .border(1.dp, if (pomodoroState.isRunning) phaseColor else Slate700, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                        .testTag("active_task_banner")
                ) {
                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = phaseColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MỤC TIÊU FOCUS (ROOM DB)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = phaseColor,
                                letterSpacing = 0.8.sp
                            )
                        }

                        // Category and custom tags chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Slate900, RoundedCornerShape(4.dp))
                                    .border(1.dp, Slate800, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = activeTask.category,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300
                                )
                            }

                            activeTask.tagList.filter { !it.equals(activeTask.category, ignoreCase = true) }.take(2).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(Slate900, RoundedCornerShape(4.dp))
                                        .border(1.dp, Slate800, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Slate400
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = activeTask.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate100
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val progress = (activeTask.completedPomodoros.toFloat() / activeTask.targetPomodoros.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
                    val percent = (progress * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tiến độ: ${activeTask.completedPomodoros}/${activeTask.targetPomodoros} Pomodoro",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (activeTask.completedPomodoros >= activeTask.targetPomodoros) GreenSuccess else Slate300
                        )
                        Text(
                            text = "$percent%",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTask.completedPomodoros >= activeTask.targetPomodoros) GreenSuccess else Cyan300
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (activeTask.completedPomodoros >= activeTask.targetPomodoros) GreenSuccess else Cyan400,
                        trackColor = Slate800
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bỏ theo dõi",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500,
                            modifier = Modifier
                                .clickable { onSelectActiveTask(null) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("deselect_active_task_btn")
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Slate900)
                                .border(1.dp, Slate700, RoundedCornerShape(4.dp))
                                .clickable { showSubjectMenu = !showSubjectMenu }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("change_active_task_btn")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ĐỔI NHIỆM VỤ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = if (showSubjectMenu) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Cyan300,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // When no active task is selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                        .clickable { showSubjectMenu = !showSubjectMenu }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("pomodoro_subject_selector"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Mục tiêu: ${pomodoroState.selectedSubject}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Slate200
                            )
                            Text(
                                text = "Bấm để chọn nhiệm vụ từ Room DB",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate500
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Cyan500)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "CHỌN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate950
                        )
                    }
                }
            }

            // Task / Subject selector modal / dropdown
            if (showSubjectMenu) {
                val selectorAvailableTags = remember(tasks) {
                    tasks.flatMap { it.tagList }.distinct().sorted()
                }
                val filteredTasks = remember(tasks, selectorTagFilter) {
                    if (selectorTagFilter.isNullOrBlank()) tasks
                    else tasks.filter { it.tagList.any { t -> t.equals(selectorTagFilter, ignoreCase = true) } }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(10.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                        .testTag("pomodoro_task_selection_panel"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NHIỆM VỤ HỌC TẬP TỪ ROOM DATABASE:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan400,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        if (selectorTagFilter != null) {
                            Text(
                                text = "Xóa lọc",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400,
                                modifier = Modifier
                                    .clickable { selectorTagFilter = null }
                                    .padding(4.dp)
                            )
                        }
                    }

                    // Tag Filter Bar in selector
                    if (selectorAvailableTags.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 2.dp)
                                .testTag("task_selector_filter_bar"),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Cyan400,
                                modifier = Modifier.size(12.dp)
                            )

                            // "TẤT CẢ" Chip
                            val isAllSelected = selectorTagFilter == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isAllSelected) Cyan500 else Slate900)
                                    .border(1.dp, if (isAllSelected) Cyan400 else Slate800, RoundedCornerShape(4.dp))
                                    .clickable { selectorTagFilter = null }
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                    .testTag("selector_filter_tag_all"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "TẤT CẢ",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAllSelected) Slate950 else Slate300
                                )
                            }

                            selectorAvailableTags.forEach { tag ->
                                val isTagSelected = selectorTagFilter.equals(tag, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isTagSelected) Cyan500 else Slate900)
                                        .border(1.dp, if (isTagSelected) Cyan400 else Slate800, RoundedCornerShape(4.dp))
                                        .clickable {
                                            selectorTagFilter = if (isTagSelected) null else tag
                                        }
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                        .testTag("selector_filter_tag_$tag"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTagSelected) Slate950 else Cyan300
                                    )
                                }
                            }
                        }
                    }

                    if (filteredTasks.isNotEmpty()) {
                        filteredTasks.forEach { task ->
                            val isSelected = task.id == activeTaskId
                            val isDone = task.isCompleted
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Cyan950 else Slate900)
                                    .border(
                                        1.dp,
                                        if (isSelected) Cyan400 else Slate800,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable {
                                        onSelectActiveTask(task.id)
                                        onSelectSubject(task.title)
                                        showSubjectMenu = false
                                    }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                                    .testTag("select_room_task_${task.id}"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) Cyan400 else Slate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = task.title,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Cyan300 else Slate200
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Slate950, RoundedCornerShape(3.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = task.category,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Cyan400
                                                )
                                            }
                                            task.tagList.filter { !it.equals(task.category, ignoreCase = true) }.take(2).forEach { tag ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(Slate950, RoundedCornerShape(3.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = "#$tag",
                                                        fontSize = 8.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Slate400
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "• ${task.completedPomodoros}/${task.targetPomodoros} 🍅",
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (task.completedPomodoros >= task.targetPomodoros) GreenSuccess else Cyan400
                                            )
                                        }
                                    }
                                }

                                if (isDone) {
                                    Box(
                                        modifier = Modifier
                                            .background(GreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "HOÀN THÀNH",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = GreenSuccess
                                        )
                                    }
                                }
                            }
                        }
                    } else if (tasks.isEmpty()) {
                        Text(
                            text = "Chưa có nhiệm vụ trong cơ sở dữ liệu. Tạo ở danh sách bên dưới.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = "Không tìm thấy nhiệm vụ nào có tag '#$selectorTagFilter'.",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "HOẶC CHỌN CHỦ ĐỀ TỰ DO:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    STUDY_SUBJECTS.forEach { subject ->
                        val isSelected = activeTaskId == null && subject == pomodoroState.selectedSubject
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Cyan950 else Slate900)
                                .border(
                                    1.dp,
                                    if (isSelected) Cyan400 else Slate800,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    onSelectActiveTask(null)
                                    onSelectSubject(subject)
                                    showSubjectMenu = false
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("subject_option_$subject")
                        ) {
                            Text(
                                text = subject,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Cyan300 else Slate400
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pomodoro Mode Switch Tabs (Focus / Short Break / Long Break)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate950, RoundedCornerShape(50))
                    .border(1.dp, Slate800, RoundedCornerShape(50))
                    .padding(4.dp)
                    .testTag("pomodoro_phase_selector"),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PomodoroPhasePill(
                    label = "Học tập (25m)",
                    isSelected = pomodoroState.phase == PomodoroPhase.FOCUS,
                    activeColor = Cyan500,
                    enabled = !pomodoroState.isRunning,
                    tag = "phase_focus_btn",
                    onClick = { onSelectPhase(PomodoroPhase.FOCUS) }
                )
                PomodoroPhasePill(
                    label = "Nghỉ ngắn (5m)",
                    isSelected = pomodoroState.phase == PomodoroPhase.SHORT_BREAK,
                    activeColor = GreenSuccess,
                    enabled = !pomodoroState.isRunning,
                    tag = "phase_short_break_btn",
                    onClick = { onSelectPhase(PomodoroPhase.SHORT_BREAK) }
                )
                PomodoroPhasePill(
                    label = "Nghỉ dài (15m)",
                    isSelected = pomodoroState.phase == PomodoroPhase.LONG_BREAK,
                    activeColor = Amber400,
                    enabled = !pomodoroState.isRunning,
                    tag = "phase_long_break_btn",
                    onClick = { onSelectPhase(PomodoroPhase.LONG_BREAK) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tactical Pomodoro Cycle Indicators (4 cycles)
            Row(
                modifier = Modifier
                    .background(Slate950, RoundedCornerShape(20.dp))
                    .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("pomodoro_cycles_indicator"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CHU KỲ ${pomodoroState.currentCycle}/4:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
                for (i in 1..4) {
                    val isDone = i < pomodoroState.currentCycle
                    val isCurrent = i == pomodoroState.currentCycle
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone -> GreenSuccess
                                    isCurrent -> phaseColor
                                    else -> Slate800
                                }
                            )
                            .border(
                                1.dp,
                                if (isCurrent) Color.White else Color.Transparent,
                                CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Circular Visual Progress with Dial & Digital Clock
            val animatedProgress by animateFloatAsState(
                targetValue = pomodoroState.progressFraction.coerceIn(0f, 1f),
                label = "animatedProgress"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(230.dp)
            ) {
                // Background Circle Glow
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(phaseBgGlow)
                )

                // Outer progress ring
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    // Track background
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = (size.minDimension - strokeWidth) / 2,
                        style = Stroke(width = strokeWidth)
                    )

                    // Active progress sweep
                    drawArc(
                        color = phaseColor,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner content
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Phase Icon
                    Icon(
                        imageVector = when (pomodoroState.phase) {
                            PomodoroPhase.FOCUS -> Icons.Default.FitnessCenter
                            PomodoroPhase.SHORT_BREAK -> Icons.Default.Coffee
                            PomodoroPhase.LONG_BREAK -> Icons.Default.LocalCafe
                        },
                        contentDescription = null,
                        tint = phaseColor,
                        modifier = Modifier.size(26.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pomodoroState.phase.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = phaseColor,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Digital countdown timer
                    Text(
                        text = formatSeconds(pomodoroState.remainingSeconds),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Slate100,
                        modifier = Modifier.testTag("pomodoro_timer_display")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (pomodoroState.phase == PomodoroPhase.FOCUS) "+2 PTS TRION" else "+1 PTS NGHỈ",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = phaseColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = pomodoroState.phase.subtitle,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate400,
                textAlign = TextAlign.Center
            )

            if (activeTask != null && pomodoroState.phase == PomodoroPhase.FOCUS) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Theo dõi: ${activeTask.title}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Cyan300
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = onToggleTimer,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = phaseColor,
                        contentColor = Slate950
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("pomodoro_toggle_button")
                ) {
                    Icon(
                        imageVector = if (pomodoroState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (pomodoroState.isRunning) "TẠM DỪNG" else "BẮT ĐẦU",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Reset Button
                OutlinedButton(
                    onClick = onResetPomodoro,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate200),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .testTag("pomodoro_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RESET",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Skip phase button
                OutlinedButton(
                    onClick = onSkipPhase,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("pomodoro_skip_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BỎ QUA",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sound Notification Bar (Android MediaPlayer chime on session end)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (isSoundEnabled) Cyan500.copy(alpha = 0.35f) else Slate800,
                        RoundedCornerShape(8.dp)
                    )
                    .testTag("pomodoro_sound_notification_bar"),
                color = if (isSoundEnabled) Slate950 else Slate950.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(onClick = onToggleSound)
                            .weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSoundEnabled) Cyan500.copy(alpha = 0.15f) else Slate800),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = if (isSoundEnabled) "Âm thanh chuông báo đang bật" else "Âm thanh chuông báo đã tắt",
                                tint = if (isSoundEnabled) Cyan400 else Slate500,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TÍN HIỆU BAIL OUT KHI HOÀN THÀNH FOCUS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (isSoundEnabled) Slate100 else Slate400
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isSoundEnabled) Cyan500.copy(alpha = 0.2f) else Slate800)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (isSoundEnabled) "BẬT" else "TẮT",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (isSoundEnabled) Cyan300 else Slate500
                                    )
                                }
                            }
                            Text(
                                text = if (isSoundEnabled) "Phát âm thanh 'Bail Out' & rung phản hồi chiến thuật" else "Đang tắt tín hiệu Bail Out khi kết thúc phiên",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate500
                            )
                        }
                    }

                    // Test Sound Button
                    OutlinedButton(
                        onClick = onTestSound,
                        enabled = isSoundEnabled,
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Cyan400,
                            disabledContentColor = Slate600
                        ),
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("test_sound_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Thử âm thanh",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "THỬ BAIL OUT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Pomodoro Stats & Session History Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .testTag("pomodoro_stats_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NHẬT KÝ PHIÊN HỌC POMODORO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate300,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "Tổng: ${pomodoroState.totalCompletedPomodoros} phiên",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
            }

            if (pomodoroState.recentLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có phiên học nào được ghi nhận.\nHoàn thành 1 chu kỳ Pomodoro 25 phút để tích lũy điểm và mở khóa Rank!",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault()) }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    pomodoroState.recentLogs.take(5).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Slate950, RoundedCornerShape(6.dp))
                                .border(1.dp, Slate800, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = log.subject,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate200
                                )
                                Text(
                                    text = "${dateFormat.format(Date(log.timestamp))} • ${log.durationMinutes} phút • Chu kỳ #${log.cycleNumber}",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate500
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Cyan400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "+${log.earnedPoints} PTS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan300
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PomodoroPhasePill(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (isSelected) activeColor else Slate950)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (isSelected) Slate950 else Slate400
        )
    }
}
