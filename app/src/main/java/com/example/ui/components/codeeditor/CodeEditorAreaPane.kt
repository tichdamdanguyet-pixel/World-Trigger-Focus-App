package com.example.ui.components.codeeditor

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SourceFileEntity
import com.example.ui.theme.*

/**
 * Text-Based Code Editor Pane
 * Features tab strip, line numbers gutter, syntax highlights, toolbar actions and status bar.
 */
@Composable
fun CodeEditorAreaPane(
    activeFile: SourceFileEntity?,
    openFiles: List<SourceFileEntity>,
    editorContent: String,
    fontSizeSp: Float,
    isLineWrapEnabled: Boolean,
    codeDiagnostics: List<CodeDiagnostic> = emptyList(),
    onContentChange: (String) -> Unit,
    onSelectFile: (Long) -> Unit,
    onCloseTab: (Long) -> Unit,
    onSaveFile: () -> Unit,
    onRevertFile: () -> Unit,
    onInsertSnippet: (String) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleLineWrap: () -> Unit,
    onCheckSyntax: () -> Unit,
    onRunBuild: () -> Unit,
    onFormatCode: () -> Unit = {},
    onOpenCommandPalette: () -> Unit = {},
    onSaveProject: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showFindBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSnippetMenu by remember { mutableStateOf(false) }
    var isAutoIndentEnabled by remember { mutableStateOf(true) }
    var isSyntaxHighlightingEnabled by remember { mutableStateOf(true) }
    var isWavyUnderlineEnabled by remember { mutableStateOf(true) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Syntax highlighting visual transformation
    val syntaxTransformation = remember(activeFile?.fileExtension, searchQuery, isSyntaxHighlightingEnabled, showFindBar) {
        if (isSyntaxHighlightingEnabled) {
            CodeSyntaxVisualTransformation(
                fileExtension = activeFile?.fileExtension ?: "kt",
                searchQuery = if (showFindBar) searchQuery else ""
            )
        } else {
            VisualTransformation.None
        }
    }

    // Internal text state to track cursor position
    var textFieldValue by remember(activeFile?.id) {
        mutableStateOf(
            TextFieldValue(
                text = editorContent,
                selection = TextRange(editorContent.length.coerceAtMost(0))
            )
        )
    }

    // Keep internal textFieldValue updated when editorContent changes externally (e.g., file switch, revert, snippet)
    LaunchedEffect(editorContent) {
        if (textFieldValue.text != editorContent) {
            textFieldValue = textFieldValue.copy(
                text = editorContent,
                selection = TextRange(textFieldValue.selection.start.coerceIn(0, editorContent.length))
            )
        }
    }

    // Keep parent in sync when internal text changes, applying auto-indentation on 'Enter'
    fun handleTextChange(newVal: TextFieldValue) {
        val oldVal = textFieldValue
        val finalVal = if (isAutoIndentEnabled) {
            AutoIndentHelper.computeAutoIndent(oldVal, newVal)
        } else {
            newVal
        }
        textFieldValue = finalVal
        if (finalVal.text != editorContent) {
            onContentChange(finalVal.text)
        }
    }

    // Calculate lines and current cursor line & column
    val lines = remember(editorContent) {
        editorContent.split("\n")
    }

    val lineCount = lines.size.coerceAtLeast(1)

    val (currentLine, currentColumn) = remember(textFieldValue.selection.start, editorContent) {
        val cursorIndex = textFieldValue.selection.start.coerceIn(0, editorContent.length)
        val textBefore = editorContent.take(cursorIndex)
        val line = textBefore.count { it == '\n' } + 1
        val lastNewline = textBefore.lastIndexOf('\n')
        val col = if (lastNewline >= 0) cursorIndex - lastNewline else cursorIndex + 1
        Pair(line, col)
    }

    // Search match count
    val searchMatches = remember(editorContent, searchQuery) {
        if (searchQuery.isBlank() || !showFindBar) 0
        else {
            var count = 0
            var idx = 0
            while (idx >= 0 && idx < editorContent.length) {
                idx = editorContent.indexOf(searchQuery, idx, ignoreCase = true)
                if (idx >= 0) {
                    count++
                    idx += searchQuery.length
                }
            }
            count
        }
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("pane_code_editor_area")
    ) {
        // 1. Editor Tab Bar (Open Files Strip)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (openFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Chưa có tập tin nào được mở",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                }
            } else {
                openFiles.forEach { file ->
                    val isActive = file.id == activeFile?.id
                    val isDirty = file.id == activeFile?.id && editorContent != file.originalContent

                    Row(
                        modifier = Modifier
                            .background(if (isActive) Slate950 else Slate900)
                            .border(
                                width = 1.dp,
                                color = if (isActive) Cyan500.copy(alpha = 0.5f) else Slate800,
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                            .clickable { onSelectFile(file.id) }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                            .testTag("editor_tab_${file.fileName}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // File type indicator
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isDirty) Amber400 else if (isActive) Cyan400 else Slate600)
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = file.fileName,
                            fontSize = 11.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (isActive) Slate100 else Slate400,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Close tab button
                        IconButton(
                            onClick = { onCloseTab(file.id) },
                            modifier = Modifier.size(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Đóng tab",
                                tint = if (isActive) Slate400 else Slate600,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Editor Action Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Group: File actions (Save, Revert, Snippets, Find)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Save Button
                val isModified = activeFile != null && editorContent != activeFile.originalContent
                OutlinedButton(
                    onClick = onSaveFile,
                    enabled = activeFile != null,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("btn_save_code"),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isModified) Amber400 else Cyan300
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isModified) Amber400.copy(alpha = 0.8f) else Cyan500.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Lưu mã",
                        modifier = Modifier.size(13.dp),
                        tint = if (isModified) Amber400 else Cyan300
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isModified) "LƯU *" else "LƯU",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Revert Button
                IconButton(
                    onClick = onRevertFile,
                    enabled = activeFile != null && isModified,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_revert_code")
                ) {
                    Icon(
                        imageVector = Icons.Default.Undo,
                        contentDescription = "Hoàn tác thay đổi",
                        tint = if (isModified) Slate200 else Slate600,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Find in File toggle
                IconButton(
                    onClick = { showFindBar = !showFindBar },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_find_in_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Tìm kiếm trong file",
                        tint = if (showFindBar) Cyan300 else Slate400,
                        modifier = Modifier.size(15.dp)
                    )
                }

                // Snippet Menu Box
                Box {
                    IconButton(
                        onClick = { showSnippetMenu = true },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_insert_snippet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = "Chèn đoạn mã mẫu",
                            tint = Cyan400,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSnippetMenu,
                        onDismissRequest = { showSnippetMenu = false },
                        modifier = Modifier.background(Slate900)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Composable Screen", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200) },
                            onClick = {
                                showSnippetMenu = false
                                onInsertSnippet(
                                    """
@Composable
fun CustomTacticalScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Tactical Radar Active", style = MaterialTheme.typography.titleMedium)
    }
}
""".trimIndent()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Custom Trigger Slot", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200) },
                            onClick = {
                                showSnippetMenu = false
                                onInsertSnippet(
                                    """
val CustomSniperTrig = TriggerSlot(
    id = "w_custom_ibis",
    name = "Ibis High-Caliber Mod",
    category = TriggerCategory.MAIN_ATTACK,
    trionCost = 35,
    rangeMeters = 350f,
    description = "Long-range anti-shield penetrator projectile."
)
""".trimIndent()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Coroutine Flow Collector", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200) },
                            onClick = {
                                showSnippetMenu = false
                                onInsertSnippet(
                                    """
viewModelScope.launch {
    repository.allMatches.collect { matches ->
        // Handle tactical match stream
    }
}
""".trimIndent()
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Room Database DAO Query", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate200) },
                            onClick = {
                                showSnippetMenu = false
                                onInsertSnippet(
                                    """
@Query("SELECT * FROM project_source_files WHERE fileExtension = :ext")
fun getFilesByExtension(ext: String): Flow<List<SourceFileEntity>>
""".trimIndent()
                                )
                            }
                        )
                    }
                }
            }

            // Right Group: Syntax verify, Build, Font & Copy
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Syntax Check
                OutlinedButton(
                    onClick = onCheckSyntax,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("btn_check_syntax"),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate300),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate700)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircleOutline,
                        contentDescription = "Kiểm tra cú pháp",
                        modifier = Modifier.size(12.dp),
                        tint = GreenSuccess
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LINT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Build Button
                Button(
                    onClick = onRunBuild,
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("btn_build_project"),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Build dự án",
                        modifier = Modifier.size(13.dp),
                        tint = Slate950
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "BUILD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = Slate950
                    )
                }

                // Copy Code
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Source Code", editorContent)
                        clipboard?.setPrimaryClip(clip)
                    },
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("btn_copy_code")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Sao chép toàn bộ mã",
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Font Size Decrement
                IconButton(
                    onClick = { onFontSizeChange(fontSizeSp - 1f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("A-", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate400, fontFamily = FontFamily.Monospace)
                }

                // Font Size Increment
                IconButton(
                    onClick = { onFontSizeChange(fontSizeSp + 1f) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("A+", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Cyan400, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // 3. Collapsible Find & Replace Bar
        AnimatedVisibility(visible = showFindBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(1.dp, Slate800)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Tìm trong file...", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Slate500) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .testTag("input_editor_search"),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Slate100),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = Slate700,
                        focusedContainerColor = Slate950,
                        unfocusedContainerColor = Slate950
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (searchQuery.isBlank()) "0 kết quả" else "$searchMatches tìm thấy",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (searchMatches > 0) Cyan300 else Slate500
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { showFindBar = false },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Đóng tìm kiếm", tint = Slate400, modifier = Modifier.size(14.dp))
                }
            }
        }

        // 3.5 Quick Code Assistant & Indentation Bar
        if (activeFile != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(1.dp, Slate800)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Tab Button (Indent +4 spaces)
                AssistCodeButton(
                    label = "⇥ Tab",
                    testTag = "btn_code_tab"
                ) {
                    val updated = AutoIndentHelper.indent(textFieldValue)
                    handleTextChange(updated)
                }

                // Untab Button (Unindent -4 spaces)
                AssistCodeButton(
                    label = "⇤ Untab",
                    testTag = "btn_code_untab"
                ) {
                    val updated = AutoIndentHelper.unindent(textFieldValue)
                    handleTextChange(updated)
                }

                // Braces pair { }
                AssistCodeButton(
                    label = "{ }",
                    testTag = "btn_code_braces"
                ) {
                    val updated = AutoIndentHelper.insertPair(textFieldValue, "{\n    ", "\n}")
                    handleTextChange(updated)
                }

                // Parens pair ( )
                AssistCodeButton(
                    label = "( )",
                    testTag = "btn_code_parens"
                ) {
                    val updated = AutoIndentHelper.insertPair(textFieldValue, "(", ")")
                    handleTextChange(updated)
                }

                // Brackets pair [ ]
                AssistCodeButton(
                    label = "[ ]",
                    testTag = "btn_code_brackets"
                ) {
                    val updated = AutoIndentHelper.insertPair(textFieldValue, "[", "]")
                    handleTextChange(updated)
                }

                // Quotes pair " "
                AssistCodeButton(
                    label = "\" \"",
                    testTag = "btn_code_quotes"
                ) {
                    val updated = AutoIndentHelper.insertPair(textFieldValue, "\"", "\"")
                    handleTextChange(updated)
                }

                // Arrow ->
                AssistCodeButton(
                    label = "->",
                    testTag = "btn_code_arrow"
                ) {
                    val cur = textFieldValue.selection.start
                    val text = textFieldValue.text
                    val newText = text.substring(0, cur) + " -> " + text.substring(cur)
                    handleTextChange(TextFieldValue(newText, TextRange(cur + 4)))
                }

                // Equals =
                AssistCodeButton(
                    label = "=",
                    testTag = "btn_code_equals"
                ) {
                    val cur = textFieldValue.selection.start
                    val text = textFieldValue.text
                    val newText = text.substring(0, cur) + " = " + text.substring(cur)
                    handleTextChange(TextFieldValue(newText, TextRange(cur + 3)))
                }

                // Semicolon ;
                AssistCodeButton(
                    label = ";",
                    testTag = "btn_code_semicolon"
                ) {
                    val cur = textFieldValue.selection.start
                    val text = textFieldValue.text
                    val newText = text.substring(0, cur) + ";" + text.substring(cur)
                    handleTextChange(TextFieldValue(newText, TextRange(cur + 1)))
                }

                // Separator
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(Slate700)
                )

                // Auto-Indent Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isAutoIndentEnabled) Cyan500.copy(alpha = 0.2f) else Slate800)
                        .border(1.dp, if (isAutoIndentEnabled) Cyan500 else Slate700, RoundedCornerShape(4.dp))
                        .clickable { isAutoIndentEnabled = !isAutoIndentEnabled }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_toggle_auto_indent")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isAutoIndentEnabled) Icons.Default.FormatIndentIncrease else Icons.Default.FormatIndentDecrease,
                            contentDescription = "Tự động thụt lề khi bấm Enter",
                            tint = if (isAutoIndentEnabled) Cyan400 else Slate400,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAutoIndentEnabled) "AUTO-INDENT [BẬT]" else "AUTO-INDENT [TẮT]",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isAutoIndentEnabled) Cyan300 else Slate400
                        )
                    }
                }

                // Syntax Highlight Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSyntaxHighlightingEnabled) Color(0xFFC678DD).copy(alpha = 0.2f) else Slate800)
                        .border(1.dp, if (isSyntaxHighlightingEnabled) Color(0xFFC678DD) else Slate700, RoundedCornerShape(4.dp))
                        .clickable { isSyntaxHighlightingEnabled = !isSyntaxHighlightingEnabled }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_toggle_syntax_highlight")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Bật/Tắt tô màu cú pháp",
                            tint = if (isSyntaxHighlightingEnabled) Color(0xFFC678DD) else Slate400,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSyntaxHighlightingEnabled) "CÚ PHÁP [BẬT]" else "CÚ PHÁP [TẮT]",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isSyntaxHighlightingEnabled) Color(0xFFE879F9) else Slate400
                        )
                    }
                }

                // Wavy Underline Squiggly Toggle Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isWavyUnderlineEnabled) Color(0xFFEF4444).copy(alpha = 0.2f) else Slate800)
                        .border(1.dp, if (isWavyUnderlineEnabled) Color(0xFFEF4444) else Slate700, RoundedCornerShape(4.dp))
                        .clickable { isWavyUnderlineEnabled = !isWavyUnderlineEnabled }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_toggle_wavy_underline")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Gesture,
                            contentDescription = "Bật/Tắt gạch chân lượn sóng lỗi",
                            tint = if (isWavyUnderlineEnabled) Color(0xFFF87171) else Slate400,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWavyUnderlineEnabled) "GẠCH SÓNG [BẬT]" else "GẠCH SÓNG [TẮT]",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isWavyUnderlineEnabled) Color(0xFFFCA5A5) else Slate400
                        )
                    }
                }

                // Format Code Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Cyan500.copy(alpha = 0.15f))
                        .border(1.dp, Cyan400.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .clickable { onFormatCode() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_assistant_format_code")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignLeft,
                            contentDescription = "Format Code",
                            tint = Cyan300,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FORMAT CODE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300
                        )
                    }
                }

                // Command Palette Pill (Ctrl+Shift+P)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF8B5CF6).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFA78BFA), RoundedCornerShape(4.dp))
                        .clickable { onOpenCommandPalette() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("btn_assistant_command_palette")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Command Palette",
                            tint = Color(0xFFA78BFA),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Ctrl+Shift+P LỆNH",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFDDD6FE)
                        )
                    }
                }
            }
        }

        // 4. Editor Body with Line Numbers Gutter
        if (activeFile == null) {
            // Empty State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = Slate600,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "CHỌN TẬP TIN ĐỂ BẮT ĐẦU CHỈNH SỬA MÃ NGUỒN",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                    Text(
                        text = "Mở cây thư mục bên trái để duyệt các tập tin Kotlin, XML, JSON, Gradle.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate600
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Line Numbers Gutter (Left side of text editing area)
                val gutterWidth = remember(lineCount) {
                    when {
                        lineCount >= 10000 -> 56.dp
                        lineCount >= 1000 -> 48.dp
                        lineCount >= 100 -> 42.dp
                        else -> 38.dp
                    }
                }

                Row(
                    modifier = Modifier
                        .width(gutterWidth)
                        .fillMaxHeight()
                        .background(Slate900.copy(alpha = 0.85f))
                        .testTag("editor_line_number_gutter")
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(verticalScrollState)
                            .padding(top = 8.dp, bottom = 8.dp, end = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..lineCount) {
                            val isCurrent = i == currentLine
                            val lineDiagnostic = codeDiagnostics.firstOrNull { it.line == i }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((fontSizeSp * 1.5f).dp)
                                    .background(
                                        when {
                                            lineDiagnostic?.severity == DiagnosticSeverity.ERROR -> Color(0xFFEF4444).copy(alpha = 0.18f)
                                            lineDiagnostic?.severity == DiagnosticSeverity.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                            isCurrent -> Cyan500.copy(alpha = 0.15f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        // Jump cursor to start of clicked line
                                        val lineStartIndex = lines.take(i - 1).sumOf { it.length + 1 }
                                        val targetPos = lineStartIndex.coerceIn(0, editorContent.length)
                                        textFieldValue = textFieldValue.copy(
                                            selection = TextRange(targetPos)
                                        )
                                    },
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (lineDiagnostic != null) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (lineDiagnostic.severity) {
                                                        DiagnosticSeverity.ERROR -> Color(0xFFEF4444)
                                                        DiagnosticSeverity.WARNING -> Color(0xFFF59E0B)
                                                        DiagnosticSeverity.INFO -> Color(0xFF06B6D4)
                                                    }
                                                )
                                                .testTag(
                                                    when (lineDiagnostic.severity) {
                                                        DiagnosticSeverity.ERROR -> "gutter_error_$i"
                                                        DiagnosticSeverity.WARNING -> "gutter_warning_$i"
                                                        DiagnosticSeverity.INFO -> "gutter_info_$i"
                                                    }
                                                )
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Text(
                                        text = "$i",
                                        fontSize = (fontSizeSp * 0.9f).sp,
                                        lineHeight = (fontSizeSp * 1.5f).sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isCurrent || lineDiagnostic != null) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            lineDiagnostic?.severity == DiagnosticSeverity.ERROR -> Color(0xFFEF4444)
                                            lineDiagnostic?.severity == DiagnosticSeverity.WARNING -> Color(0xFFF59E0B)
                                            isCurrent -> Cyan400
                                            else -> Slate500
                                        },
                                        textAlign = TextAlign.End
                                    )
                                }
                            }
                        }
                    }

                    // Vertical Gutter Separator Border
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(Slate800)
                    )
                }

                // Code Input Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Slate950)
                        .verticalScroll(verticalScrollState)
                        .then(if (!isLineWrapEnabled) Modifier.horizontalScroll(horizontalScrollState) else Modifier)
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { handleTextChange(it) },
                        visualTransformation = syntaxTransformation,
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_text_input")
                            .drawWithContent {
                                drawContent()
                                if (isWavyUnderlineEnabled && textLayoutResult != null) {
                                    WavyUnderlineDrawer.drawWavyUnderlines(
                                        drawScope = this,
                                        textLayoutResult = textLayoutResult!!,
                                        diagnostics = codeDiagnostics,
                                        textLength = textFieldValue.text.length
                                    )
                                }
                            }
                            .onPreviewKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    if (keyEvent.key == Key.P && (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) && keyEvent.isShiftPressed) {
                                        onOpenCommandPalette()
                                        true
                                    } else if (keyEvent.key == Key.S && (keyEvent.isCtrlPressed || keyEvent.isMetaPressed)) {
                                        if (keyEvent.isShiftPressed) {
                                            onSaveProject()
                                        } else {
                                            onSaveFile()
                                        }
                                        true
                                    } else if (keyEvent.key == Key.F && keyEvent.isShiftPressed && keyEvent.isAltPressed) {
                                        onFormatCode()
                                        true
                                    } else if (keyEvent.key == Key.Tab) {
                                        val updated = if (keyEvent.isShiftPressed) {
                                            AutoIndentHelper.unindent(textFieldValue)
                                        } else {
                                            AutoIndentHelper.indent(textFieldValue)
                                        }
                                        handleTextChange(updated)
                                        true
                                    } else {
                                        false
                                    }
                                } else {
                                    false
                                }
                            },
                        textStyle = LocalTextStyle.current.copy(
                            color = Slate100,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.5f).sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        cursorBrush = SolidColor(Cyan400),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    for (i in 1..lineCount) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height((fontSizeSp * 1.5f).dp)
                                                .background(
                                                    if (i == currentLine) Cyan500.copy(alpha = 0.08f)
                                                    else Color.Transparent
                                                )
                                        )
                                    }
                                }
                                if (editorContent.isEmpty()) {
                                    Text(
                                        text = "// Nhập mã nguồn tại đây...",
                                        color = Slate600,
                                        fontSize = fontSizeSp.sp,
                                        lineHeight = (fontSizeSp * 1.5f).sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }
        }

        // Diagnostic Floating Banner for current line
        val cursorOffset = textFieldValue.selection.start
        val activeDiagnosticAtCursor = remember(cursorOffset, currentLine, codeDiagnostics) {
            codeDiagnostics.find { cursorOffset in it.startOffset..it.endOffset }
                ?: codeDiagnostics.find { it.line == currentLine }
        }

        AnimatedVisibility(visible = activeDiagnosticAtCursor != null) {
            activeDiagnosticAtCursor?.let { diag ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when (diag.severity) {
                                DiagnosticSeverity.ERROR -> Color(0xFF450A0A)
                                DiagnosticSeverity.WARNING -> Color(0xFF451A03)
                                DiagnosticSeverity.INFO -> Color(0xFF082F49)
                            }
                        )
                        .border(
                            1.dp,
                            when (diag.severity) {
                                DiagnosticSeverity.ERROR -> Color(0xFFEF4444).copy(alpha = 0.6f)
                                DiagnosticSeverity.WARNING -> Color(0xFFF59E0B).copy(alpha = 0.6f)
                                DiagnosticSeverity.INFO -> Color(0xFF06B6D4).copy(alpha = 0.6f)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("editor_diagnostic_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (diag.severity) {
                                DiagnosticSeverity.ERROR -> Icons.Default.Cancel
                                DiagnosticSeverity.WARNING -> Icons.Default.Warning
                                DiagnosticSeverity.INFO -> Icons.Default.Info
                            },
                            contentDescription = null,
                            tint = when (diag.severity) {
                                DiagnosticSeverity.ERROR -> Color(0xFFEF4444)
                                DiagnosticSeverity.WARNING -> Color(0xFFF59E0B)
                                DiagnosticSeverity.INFO -> Color(0xFF06B6D4)
                            },
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "[${diag.severity}] Dòng ${diag.line}, Cột ${diag.column}: ${diag.message}",
                            fontSize = 9.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = Slate100,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "CHI TIẾT →",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Cyan300,
                        modifier = Modifier
                            .clickable { onCheckSyntax() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("btn_diagnostic_banner_details")
                    )
                }
            }
        }

        // 5. Editor Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate900)
                .border(1.dp, Slate800)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Cursor position & Line count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "DÒNG $currentLine, CỘT $currentColumn",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Cyan400
                )
                Text(
                    text = "•",
                    fontSize = 9.sp,
                    color = Slate600
                )
                Text(
                    text = "$lineCount DÒNG",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
                Text(
                    text = "•",
                    fontSize = 9.sp,
                    color = Slate600
                )
                Text(
                    text = "${editorContent.length} KÝ TỰ",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
                Text(
                    text = "•",
                    fontSize = 9.sp,
                    color = Slate600
                )
                Text(
                    text = if (isAutoIndentEnabled) "INDENT: 4SP (TỰ ĐỘNG)" else "INDENT: TẮT",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAutoIndentEnabled) Cyan400 else Slate500
                )
                Text(
                    text = "•",
                    fontSize = 9.sp,
                    color = Slate600
                )
                Text(
                    text = if (isSyntaxHighlightingEnabled) "CÚ PHÁP: ${activeFile?.fileExtension?.uppercase() ?: "KT"}" else "CÚ PHÁP: TẮT",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSyntaxHighlightingEnabled) Color(0xFFE879F9) else Slate500
                )
            }

            // Right: File encoding & Save state
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Diagnostics count badge
                val errorCount = codeDiagnostics.count { it.severity == DiagnosticSeverity.ERROR }
                val warnCount = codeDiagnostics.count { it.severity == DiagnosticSeverity.WARNING }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            when {
                                errorCount > 0 -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                warnCount > 0 -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> GreenSuccessDark.copy(alpha = 0.3f)
                            }
                        )
                        .border(
                            1.dp,
                            when {
                                errorCount > 0 -> Color(0xFFEF4444)
                                warnCount > 0 -> Color(0xFFF59E0B)
                                else -> GreenSuccess
                            },
                            RoundedCornerShape(3.dp)
                        )
                        .clickable { onCheckSyntax() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .testTag("btn_statusbar_diagnostics")
                ) {
                    Text(
                        text = when {
                            errorCount > 0 && warnCount > 0 -> "$errorCount LỖI • $warnCount CẢNH BÁO"
                            errorCount > 0 -> "$errorCount LỖI CÚ PHÁP"
                            warnCount > 0 -> "$warnCount VẤN ĐỀ LOGIC"
                            else -> "0 LỖI (CHUẨN)"
                        },
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = when {
                            errorCount > 0 -> Color(0xFFF87171)
                            warnCount > 0 -> Color(0xFFFBBF24)
                            else -> GreenSuccess
                        }
                    )
                }

                val isModified = activeFile != null && editorContent != activeFile.originalContent
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isModified) Amber400.copy(alpha = 0.2f) else GreenSuccessDark.copy(alpha = 0.4f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = if (isModified) "CHƯA LƯU" else "ĐÃ LƯU ROOM",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isModified) Amber400 else GreenSuccess
                    )
                }

                Text(
                    text = "UTF-8",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )

                Text(
                    text = activeFile?.fileExtension?.uppercase() ?: "NONE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
            }
        }
    }
}

@Composable
private fun AssistCodeButton(
    label: String,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Slate200
        )
    }
}
