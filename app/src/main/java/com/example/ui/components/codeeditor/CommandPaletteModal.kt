package com.example.ui.components.codeeditor

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Data structure representing an executable action inside the Command Palette.
 */
data class CommandPaletteAction(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val shortcut: String? = null,
    val icon: ImageVector,
    val keywords: List<String> = emptyList(),
    val onExecute: () -> Unit
)

/**
 * Modal Command Palette dialog for rapid action search and execution (Ctrl+Shift+P).
 */
@Composable
fun CommandPaletteModal(
    isOpen: Boolean,
    actions: List<CommandPaletteAction>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()

    val categories = listOf("ALL", "EDITOR", "BUILD & RUN", "PROJECT", "VIEW", "DIAGNOSTICS")

    // Filter actions based on search query and category filter
    val filteredActions = remember(searchQuery, selectedCategory, actions) {
        actions.filter { action ->
            val matchesCategory = selectedCategory == "ALL" || action.category.equals(selectedCategory, ignoreCase = true)
            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                action.title.lowercase().contains(query) ||
                action.description.lowercase().contains(query) ||
                action.id.lowercase().contains(query) ||
                action.category.lowercase().contains(query) ||
                action.shortcut?.lowercase()?.contains(query) == true ||
                action.keywords.any { it.lowercase().contains(query) }
            matchesCategory && matchesSearch
        }
    }

    // Keep selectedIndex within bounds
    LaunchedEffect(filteredActions.size) {
        if (selectedIndex >= filteredActions.size) {
            selectedIndex = (filteredActions.size - 1).coerceAtLeast(0)
        }
    }

    // Auto-focus the search field when opening
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Scroll to selected item if changed via arrow keys
    LaunchedEffect(selectedIndex) {
        if (filteredActions.isNotEmpty() && selectedIndex in filteredActions.indices) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(onClick = onDismiss)
                .testTag("dialog_command_palette"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = modifier
                    .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
                    .widthIn(max = 660.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate950)
                    .border(1.5.dp, Brush.horizontalGradient(listOf(Cyan400, Cyan500, Cyan900)), RoundedCornerShape(12.dp))
                    .clickable(enabled = false) {} // Prevent dismiss on modal click
            ) {
                // Header Bar with Sci-Fi Accent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Cyan400)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "COMMAND PALETTE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp,
                            color = Slate100
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Cyan500.copy(alpha = 0.18f))
                                .border(0.5.dp, Cyan400.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ctrl+Shift+P",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan300
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("btn_close_command_palette")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Slate400,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Search Input Field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ">",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Cyan400
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            selectedIndex = 0
                        },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .testTag("input_command_palette_search")
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionDown -> {
                                            if (filteredActions.isNotEmpty()) {
                                                selectedIndex = (selectedIndex + 1) % filteredActions.size
                                            }
                                            true
                                        }
                                        Key.DirectionUp -> {
                                            if (filteredActions.isNotEmpty()) {
                                                selectedIndex = if (selectedIndex <= 0) filteredActions.size - 1 else selectedIndex - 1
                                            }
                                            true
                                        }
                                        Key.Enter -> {
                                            if (filteredActions.isNotEmpty() && selectedIndex in filteredActions.indices) {
                                                val target = filteredActions[selectedIndex]
                                                onDismiss()
                                                target.onExecute()
                                            }
                                            true
                                        }
                                        Key.Escape -> {
                                            onDismiss()
                                            true
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            },
                        textStyle = TextStyle(
                            color = Slate100,
                            fontSize = 13.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(Cyan400),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Nhập lệnh tác chiến (ví dụ: Format Code, Deploy, Save)...",
                                        color = Slate500,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Xóa tìm kiếm",
                                tint = Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate800, thickness = 1.dp)

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate950)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) Cyan500.copy(alpha = 0.2f) else Slate900)
                                .border(1.dp, if (isSelected) Cyan400 else Slate800, RoundedCornerShape(4.dp))
                                .clickable {
                                    selectedCategory = cat
                                    selectedIndex = 0
                                }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                .testTag("btn_filter_cat_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontFamily = FontFamily.Monospace,
                                color = if (isSelected) Cyan300 else Slate400
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate800.copy(alpha = 0.6f), thickness = 0.5.dp)

                // Action Items List
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .background(Slate950)
                ) {
                    if (filteredActions.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Slate600,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Không tìm thấy lệnh tác chiến nào khớp với \"$searchQuery\"",
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Slate400
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            itemsIndexed(filteredActions, key = { _, act -> act.id }) { idx, action ->
                                val isSelected = idx == selectedIndex

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (isSelected) Cyan500.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            width = if (isSelected) 1.dp else 0.dp,
                                            color = if (isSelected) Cyan400.copy(alpha = 0.7f) else Color.Transparent
                                        )
                                        .clickable {
                                            onDismiss()
                                            action.onExecute()
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                        .testTag("action_item_${action.id}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Category color indicator icon
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    when (action.category) {
                                                        "EDITOR" -> Cyan500.copy(alpha = 0.2f)
                                                        "BUILD & RUN" -> GreenSuccess.copy(alpha = 0.2f)
                                                        "PROJECT" -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                                        "VIEW" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                                        "DIAGNOSTICS" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                                        else -> Slate800
                                                    }
                                                )
                                                .border(
                                                    0.5.dp,
                                                    when (action.category) {
                                                        "EDITOR" -> Cyan400
                                                        "BUILD & RUN" -> GreenSuccess
                                                        "PROJECT" -> Color(0xFFA78BFA)
                                                        "VIEW" -> Color(0xFFFBBF24)
                                                        "DIAGNOSTICS" -> Color(0xFFF87171)
                                                        else -> Slate700
                                                    },
                                                    RoundedCornerShape(6.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = null,
                                                tint = when (action.category) {
                                                    "EDITOR" -> Cyan300
                                                    "BUILD & RUN" -> GreenSuccess
                                                    "PROJECT" -> Color(0xFFA78BFA)
                                                    "VIEW" -> Color(0xFFFBBF24)
                                                    "DIAGNOSTICS" -> Color(0xFFF87171)
                                                    else -> Slate300
                                                },
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = action.title,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) Cyan300 else Slate100
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(Slate800)
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = action.category,
                                                        fontSize = 7.5.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = Slate400
                                                    )
                                                }
                                            }
                                            Text(
                                                text = action.description,
                                                fontSize = 9.5.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate400,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (action.shortcut != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Slate900)
                                                .border(1.dp, if (isSelected) Cyan400.copy(alpha = 0.5f) else Slate800, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = action.shortcut,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                color = if (isSelected) Cyan300 else Slate400
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Slate800, thickness = 1.dp)

                // Footer Bar with Keyboard Shortcuts Guide
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900)
                        .padding(horizontal = 14.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShortcutLegendItem(keyLabel = "↑↓", desc = "Di chuyển")
                        ShortcutLegendItem(keyLabel = "↵", desc = "Kích hoạt")
                        ShortcutLegendItem(keyLabel = "ESC", desc = "Đóng")
                    }

                    Text(
                        text = "${filteredActions.size} lệnh có sẵn",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutLegendItem(keyLabel: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(Slate800)
                .border(0.5.dp, Slate700, RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
            Text(
                text = keyLabel,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Cyan300
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = desc,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = Slate400
        )
    }
}
