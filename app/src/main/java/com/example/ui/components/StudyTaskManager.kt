package com.example.ui.components

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Label
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.StudyTaskEntity
import com.example.ui.theme.Cyan300
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan500
import com.example.ui.theme.Cyan950
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.OrangeWarning
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

val STUDY_CATEGORIES = listOf(
    "Coding",
    "Reading",
    "Ngoại ngữ",
    "Chiến thuật",
    "Tài liệu",
    "Luyện phản xạ"
)

val TAG_SUGGESTIONS = listOf(
    "Coding",
    "Reading",
    "Android",
    "Room",
    "Architecture",
    "Docs",
    "Strategy"
)

@Composable
fun StudyTaskManager(
    tasks: List<StudyTaskEntity>,
    activeTaskId: Long?,
    onSelectActiveTask: (Long?) -> Unit,
    onAddTask: (title: String, category: String, tags: String, targetPomodoros: Int) -> Unit,
    onToggleTask: (Long) -> Unit,
    onDeleteTask: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddingTask by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(STUDY_CATEGORIES.first()) }
    var isCustomCategory by remember { mutableStateOf(false) }
    var customCategoryText by remember { mutableStateOf("") }
    var customTagsText by remember { mutableStateOf("") }
    var targetPomodoros by remember { mutableIntStateOf(2) }

    var selectedFilterTag by remember { mutableStateOf<String?>(null) }

    val allAvailableTags = remember(tasks) {
        tasks.flatMap { it.tagList }.distinct().sorted()
    }

    val displayedTasks = remember(tasks, selectedFilterTag) {
        if (selectedFilterTag.isNullOrBlank()) tasks
        else tasks.filter { it.tagList.any { tag -> tag.equals(selectedFilterTag, ignoreCase = true) } }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
            .testTag("study_task_manager_card"),
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MỤC TIÊU HỌC TẬP (STUDY TASKS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200,
                        letterSpacing = 1.sp
                    )
                }

                val completedCount = tasks.count { it.isCompleted }
                Text(
                    text = "$completedCount/${tasks.size} xong",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Cyan300
                )
            }

            // Quick Add Toggle
            if (!isAddingTask) {
                OutlinedButton(
                    onClick = { isAddingTask = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_study_task_toggle_btn"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "THÊM MỤC TIÊU HỌC TẬP MỚI",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Add Task Inline Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "NHẬP TÊN BÀI HỌC / NHIỆM VỤ:",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )

                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_study_task_title_input"),
                        placeholder = {
                            Text(
                                "Ví dụ: Lập trình Room Database hoặc Đọc tài liệu Sniping",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate200,
                            unfocusedTextColor = Slate200,
                            focusedBorderColor = Cyan500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        )
                    )

                    // Category Selector Pills
                    Text(
                        text = "DANH MỤC (CATEGORY):",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        STUDY_CATEGORIES.forEach { category ->
                            val isSelected = !isCustomCategory && category == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Cyan500 else Slate900)
                                    .clickable {
                                        isCustomCategory = false
                                        selectedCategory = category
                                    }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("category_chip_$category"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Slate950 else Slate400
                                )
                            }
                        }

                        // "+ Khác" chip for custom category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCustomCategory) Cyan500 else Slate900)
                                .clickable { isCustomCategory = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("category_chip_custom"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ Khác",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isCustomCategory) Slate950 else Slate400
                            )
                        }
                    }

                    if (isCustomCategory) {
                        OutlinedTextField(
                            value = customCategoryText,
                            onValueChange = { customCategoryText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_category_input"),
                            placeholder = {
                                Text(
                                    "Nhập danh mục tự chọn (VD: Coding, Reading, Math...)",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Slate200,
                                unfocusedTextColor = Slate200,
                                focusedBorderColor = Cyan500,
                                unfocusedBorderColor = Slate700,
                                focusedContainerColor = Slate900,
                                unfocusedContainerColor = Slate900
                            )
                        )
                    }

                    // Custom Tags field
                    Text(
                        text = "TAGS / THẺ TÙY CHỈNH (phân cách bằng dấu phẩy):",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Slate400
                    )

                    OutlinedTextField(
                        value = customTagsText,
                        onValueChange = { customTagsText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_tags_input"),
                        placeholder = {
                            Text(
                                "Ví dụ: Coding, Room, Android hoặc Reading, Docs",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Slate200,
                            unfocusedTextColor = Slate200,
                            focusedBorderColor = Cyan500,
                            unfocusedBorderColor = Slate700,
                            focusedContainerColor = Slate900,
                            unfocusedContainerColor = Slate900
                        )
                    )

                    // Quick Tag Suggestions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gợi ý:",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate500
                        )
                        TAG_SUGGESTIONS.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate900)
                                    .border(1.dp, Slate700, RoundedCornerShape(4.dp))
                                    .clickable {
                                        val current = customTagsText.trim()
                                        customTagsText = if (current.isEmpty()) {
                                            suggestion
                                        } else if (!current.contains(suggestion, ignoreCase = true)) {
                                            "$current, $suggestion"
                                        } else {
                                            current
                                        }
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("tag_suggestion_$suggestion")
                            ) {
                                Text(
                                    text = "+$suggestion",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Cyan400
                                )
                            }
                        }
                    }

                    // Target Pomodoros Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dự kiến số Pomodoro (25m/phiên):",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { count ->
                                val isSelected = count == targetPomodoros
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Cyan400 else Slate900)
                                        .border(1.dp, if (isSelected) Cyan400 else Slate700, CircleShape)
                                    .clickable { targetPomodoros = count },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Slate950 else Slate300
                                    )
                                }
                            }
                        }
                    }

                    // Form Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                isAddingTask = false
                                newTaskTitle = ""
                                customCategoryText = ""
                                customTagsText = ""
                                isCustomCategory = false
                            },
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                "HỦY",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                        }

                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    val finalCategory = if (isCustomCategory && customCategoryText.isNotBlank()) {
                                        customCategoryText.trim()
                                    } else {
                                        selectedCategory
                                    }
                                    onAddTask(newTaskTitle, finalCategory, customTagsText, targetPomodoros)
                                    newTaskTitle = ""
                                    customCategoryText = ""
                                    customTagsText = ""
                                    isCustomCategory = false
                                    isAddingTask = false
                                }
                            },
                            enabled = newTaskTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Cyan500,
                                contentColor = Slate950
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.testTag("save_study_task_btn")
                        ) {
                            Text(
                                "LƯU MỤC TIÊU",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Tag Filter Chips Bar
            if (allAvailableTags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 2.dp)
                        .testTag("study_task_tag_filter_bar"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "LỌC:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400
                    )

                    // "TẤT CẢ" Chip
                    val isAllSelected = selectedFilterTag == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isAllSelected) Cyan500 else Slate900)
                            .border(1.dp, if (isAllSelected) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                            .clickable { selectedFilterTag = null }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("filter_tag_all"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TẤT CẢ (${tasks.size})",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isAllSelected) Slate950 else Slate300
                        )
                    }

                    // Individual Tag Chips
                    allAvailableTags.forEach { tag ->
                        val isTagSelected = selectedFilterTag.equals(tag, ignoreCase = true)
                        val count = tasks.count { it.tagList.any { t -> t.equals(tag, ignoreCase = true) } }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isTagSelected) Cyan500 else Slate900)
                                .border(1.dp, if (isTagSelected) Cyan400 else Slate800, RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedFilterTag = if (isTagSelected) null else tag
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("filter_tag_$tag"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "#$tag ($count)",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isTagSelected) Slate950 else Cyan300
                            )
                        }
                    }
                }
            }

            // Task List
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Chưa có mục tiêu học tập nào trong cơ sở dữ liệu.\nThêm bài tập để theo dõi tiến độ Pomodoro của bạn!",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            } else if (displayedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy nhiệm vụ nào có tag '#$selectedFilterTag'.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate400,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    displayedTasks.forEach { task ->
                        val isActive = task.id == activeTaskId
                        val isDone = task.isCompleted

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isActive) Cyan950.copy(alpha = 0.5f) else Slate950
                                )
                                .border(
                                    1.dp,
                                    if (isActive) Cyan400 else Slate800,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    // Clicking row selects/unselects as active focus target for Pomodoro
                                    if (!isDone) {
                                        onSelectActiveTask(if (isActive) null else task.id)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("study_task_item_${task.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Checkbox and Title
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { onToggleTask(task.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("study_task_toggle_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isDone) GreenSuccess else (if (isActive) Cyan400 else Slate500),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = task.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (isDone) Slate500 else (if (isActive) Cyan300 else Slate200),
                                            textDecoration = if (isDone) TextDecoration.LineThrough else null
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(Cyan500, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "ĐANG HỌC",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Slate950
                                                )
                                            }
                                        }
                                    }

                                    // Category and Tags chips row
                                    Row(
                                        modifier = Modifier.padding(top = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        // Category badge
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Slate900)
                                                .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
                                                .clickable { selectedFilterTag = task.category }
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = task.category,
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                color = Cyan300
                                            )
                                        }

                                        // Additional custom tags
                                        task.tagList.filter { !it.equals(task.category, ignoreCase = true) }.forEach { tag ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(Slate900)
                                                    .border(1.dp, Slate700, RoundedCornerShape(3.dp))
                                                    .clickable { selectedFilterTag = tag }
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "#$tag",
                                                    fontSize = 9.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = Slate400
                                                )
                                            }
                                        }

                                        Text(
                                            text = "•",
                                            fontSize = 9.sp,
                                            color = Slate600
                                        )

                                        Text(
                                            text = "${task.completedPomodoros}/${task.targetPomodoros} 🍅",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (task.completedPomodoros >= task.targetPomodoros) GreenSuccess else Cyan400
                                        )
                                    }
                                }
                            }

                            // Track / Active Selection Button
                            if (!isDone) {
                                if (isActive) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Cyan500)
                                            .clickable { onSelectActiveTask(null) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("study_task_active_badge_${task.id}")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.TrackChanges,
                                                contentDescription = null,
                                                tint = Slate950,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "TRACKING",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate950
                                            )
                                        }
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onSelectActiveTask(task.id) },
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("study_task_select_${task.id}"),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Cyan400),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                                    ) {
                                        Text(
                                            text = "TRACK",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // Delete Action
                            IconButton(
                                onClick = { onDeleteTask(task.id) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("study_task_delete_${task.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Slate600,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
