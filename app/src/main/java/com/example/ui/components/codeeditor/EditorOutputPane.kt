package com.example.ui.components.codeeditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SourceFileEntity
import com.example.ui.theme.*

/**
 * Diagnostics, Build Output & Diff Drawer Pane
 */
@Composable
fun EditorOutputPane(
    activeFile: SourceFileEntity?,
    currentEditorContent: String,
    activeTab: Int,
    buildLogs: List<String>,
    syntaxErrors: List<String>,
    isBuilding: Boolean,
    codeDiagnostics: List<CodeDiagnostic> = emptyList(),
    onSelectTab: (Int) -> Unit,
    onRunBuild: () -> Unit,
    onCheckSyntax: () -> Unit,
    onClosePane: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Triple("CHẨN ĐOÁN CÚ PHÁP", Icons.Default.CheckCircleOutline, 0),
        Triple("KẾT QUẢ BUILD", Icons.Default.Terminal, 1),
        Triple("SO SÁNH DIFF", Icons.Default.Difference, 2)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp, max = 280.dp)
            .background(Slate900)
            .border(1.dp, Slate800)
            .testTag("pane_editor_output")
    ) {
        // Output Pane Header Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate950)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (title, icon, idx) ->
                    val selected = activeTab == idx
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (selected) Cyan500.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) Cyan400.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onSelectTab(idx) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("output_tab_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) Cyan300 else Slate400,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = title,
                            fontSize = 9.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace,
                            color = if (selected) Cyan200(Cyan300) else Slate400
                        )

                        // Badge count for errors
                        if (idx == 0 && syntaxErrors.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(OrangeWarning)
                                    .padding(horizontal = 4.dp, vertical = 0.5.dp)
                            ) {
                                Text(
                                    text = "${syntaxErrors.size}",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate950
                                )
                            }
                        }
                    }
                }
            }

            // Close / Minimize Button
            IconButton(
                onClick = onClosePane,
                modifier = Modifier
                    .size(24.dp)
                    .testTag("btn_close_output_pane")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Thu nhỏ bảng điều khiển",
                    tint = Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Divider(color = Slate800, thickness = 1.dp)

        // Output Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            when (activeTab) {
                0 -> DiagnosticsTabContent(
                    syntaxErrors = syntaxErrors,
                    codeDiagnostics = codeDiagnostics,
                    onCheckSyntax = onCheckSyntax
                )
                1 -> BuildOutputTabContent(
                    buildLogs = buildLogs,
                    isBuilding = isBuilding,
                    onRunBuild = onRunBuild
                )
                2 -> DiffTabContent(
                    activeFile = activeFile,
                    currentContent = currentEditorContent
                )
            }
        }
    }
}

private fun Cyan200(fallback: Color): Color = Cyan300

@Composable
fun DiagnosticsTabContent(
    syntaxErrors: List<String>,
    codeDiagnostics: List<CodeDiagnostic> = emptyList(),
    onCheckSyntax: () -> Unit
) {
    if (codeDiagnostics.isEmpty() && syntaxErrors.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = GreenSuccess,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CÚ PHÁP HOÀN TOÀN HỢP LỆ",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate200
            )
            Text(
                text = "Không phát hiện lỗi cú pháp, mất cân bằng ngoặc hay vấn đề logic.",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCheckSyntax,
                modifier = Modifier.height(26.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("QUÉT LẠI CÚ PHÁP & LOGIC", fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
        }
    } else if (codeDiagnostics.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(codeDiagnostics) { diag ->
                val (bgColor, borderColor, textColor, badgeText) = when (diag.severity) {
                    DiagnosticSeverity.ERROR -> Quadruple(
                        Color(0xFF450A0A).copy(alpha = 0.4f),
                        Color(0xFFEF4444).copy(alpha = 0.6f),
                        Color(0xFFFCA5A5),
                        "LỖI CÚ PHÁP"
                    )
                    DiagnosticSeverity.WARNING -> Quadruple(
                        Color(0xFF451A03).copy(alpha = 0.4f),
                        Color(0xFFF59E0B).copy(alpha = 0.6f),
                        Color(0xFFFDE68A),
                        "CẢNH BÁO LOGIC"
                    )
                    DiagnosticSeverity.INFO -> Quadruple(
                        Color(0xFF082F49).copy(alpha = 0.4f),
                        Color(0xFF06B6D4).copy(alpha = 0.6f),
                        Color(0xFFBAE6FD),
                        "GỢI Ý"
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("diagnostic_card_${diag.line}_${diag.column}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(borderColor)
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Slate950
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DÒNG ${diag.line}, CỘT ${diag.column}",
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                        Text(
                            text = "[${diag.rule}]",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = diag.message,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200
                    )

                    if (diag.errorSnippet.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(2.dp))
                                .background(Slate950)
                                .border(0.5.dp, Slate800, RoundedCornerShape(2.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = diag.errorSnippet,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Cyan400
                            )
                        }
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(syntaxErrors) { error ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OrangeWarning.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, OrangeWarning.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = OrangeWarning,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        fontSize = 10.5.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate200
                    )
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun BuildOutputTabContent(
    buildLogs: List<String>,
    isBuilding: Boolean,
    onRunBuild: () -> Unit
) {
    if (isBuilding) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Cyan400,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ĐANG THỰC THI GRADLE TASKS...",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Cyan300
            )
        }
    } else if (buildLogs.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CHƯA CÓ LỊCH SỬ BUILD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate400
            )
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = onRunBuild,
                modifier = Modifier.height(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
            ) {
                Text("CHẠY BUILD NGAY", fontSize = 9.5.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Slate950)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(buildLogs) { log ->
                val color = when {
                    log.startsWith("e:") || log.contains("FAILURE") -> RedFailDark.copy(alpha = 0.9f)
                    log.startsWith("v:") || log.contains("SUCCESSFUL") -> GreenSuccess
                    log.startsWith("> Task") -> Cyan300
                    else -> Slate300
                }

                Text(
                    text = log,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
            }
        }
    }
}

@Composable
fun DiffTabContent(
    activeFile: SourceFileEntity?,
    currentContent: String
) {
    if (activeFile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không có tập tin nào đang mở.", fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, color = Slate500)
        }
        return
    }

    val origLines = remember(activeFile.originalContent) { activeFile.originalContent.split("\n") }
    val newLines = remember(currentContent) { currentContent.split("\n") }
    val isChanged = activeFile.originalContent != currentContent

    if (!isChanged) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = Cyan400, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "KHÔNG CÓ THAY ĐỔI SO VỚI BẢN GỐC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate300
            )
            Text(
                text = "Tập tin '${activeFile.fileName}' đang ở trạng thái nguyên bản.",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Slate500
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Bản gốc: ${origLines.size} dòng",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
                Text(
                    text = "Hiện tại: ${newLines.size} dòng",
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Amber400
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Slate950)
                    .border(1.dp, Slate800)
                    .padding(4.dp)
            ) {
                val maxCount = maxOf(origLines.size, newLines.size)
                items(maxCount) { index ->
                    val orig = origLines.getOrNull(index)
                    val new = newLines.getOrNull(index)

                    if (orig != new) {
                        if (orig != null) {
                            Text(
                                text = "- $orig",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFF87171),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF450A0A).copy(alpha = 0.4f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        if (new != null) {
                            Text(
                                text = "+ $new",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF4ADE80),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF14532D).copy(alpha = 0.4f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    } else if (orig != null) {
                        Text(
                            text = "  $orig",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate600,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
