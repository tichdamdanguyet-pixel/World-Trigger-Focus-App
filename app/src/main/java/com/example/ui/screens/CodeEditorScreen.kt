package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AgentUiState
import com.example.ui.AgentViewModel
import com.example.ui.components.codeeditor.CodeEditorAreaPane
import com.example.ui.components.codeeditor.CommandPaletteAction
import com.example.ui.components.codeeditor.CommandPaletteModal
import com.example.ui.components.codeeditor.EditorOutputPane
import com.example.ui.components.codeeditor.FileExplorerPane
import com.example.ui.theme.*

/**
 * Multi-Pane Code Editor Screen
 * Provides an IDE workspace with Project File Hierarchy, Text-Based Code Editor,
 * and Output Diagnostics/Build Drawer.
 */
@Composable
fun CodeEditorScreen(
    viewModel: AgentViewModel,
    uiState: AgentUiState,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
            .testTag("screen_code_editor")
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown &&
                    keyEvent.key == Key.P &&
                    (keyEvent.isCtrlPressed || keyEvent.isMetaPressed) &&
                    keyEvent.isShiftPressed
                ) {
                    viewModel.showCommandPalette()
                    true
                } else {
                    false
                }
            }
    ) {
        val isWideScreen = maxWidth >= 700.dp
        var mobileViewMode by remember { mutableStateOf(0) } // 0: Editor, 1: File Explorer

        Column(modifier = Modifier.fillMaxSize()) {
            // IDE Top Application Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .border(1.dp, Slate800)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Brand & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Cyan500.copy(alpha = 0.2f))
                            .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "IDE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Cyan300
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "BORDER SOURCE ARCHITECTURE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Slate100
                        )
                        Text(
                            text = "Hệ thống biên tập mã nguồn & cấu hình tác chiến",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Slate400
                        )
                    }
                }

                // Action controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Command Palette Quick Launcher (Ctrl+Shift+P)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Slate950)
                            .border(1.dp, Cyan400.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { viewModel.showCommandPalette() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("btn_open_command_palette")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Command Palette",
                                tint = Cyan400,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LỆNH",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Slate200
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Cyan500.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "Ctrl+Shift+P",
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan300
                                )
                            }
                        }
                    }

                    if (!isWideScreen) {
                        // Phone Segment Switcher: Explorer vs Editor
                        FilterChip(
                            selected = mobileViewMode == 1,
                            onClick = { mobileViewMode = if (mobileViewMode == 1) 0 else 1 },
                            label = {
                                Text(
                                    text = if (mobileViewMode == 1) "MÃ NGUỒN" else "TẬP TIN",
                                    fontSize = 9.5.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (mobileViewMode == 1) Icons.Default.Code else Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                            },
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("btn_toggle_mobile_view")
                        )
                    } else {
                        // Tablet/Wide screen: Toggle Explorer Pane
                        IconButton(
                            onClick = { viewModel.toggleExplorerPane() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_toggle_explorer_pane")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Ẩn/hiện cây thư mục",
                                tint = if (uiState.isExplorerPaneVisible) Cyan400 else Slate500,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                    // Toggle Output Drawer
                    IconButton(
                        onClick = { viewModel.toggleOutputPane() },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_toggle_output_pane")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Bảng điều khiển & Build",
                            tint = if (uiState.isOutputPaneVisible) Cyan300 else Slate400,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // Workspace Layout Body
            if (isWideScreen) {
                // Multi-Pane side-by-side (Tablets / Wide Screens)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Left Pane: File Explorer
                    AnimatedVisibility(
                        visible = uiState.isExplorerPaneVisible,
                        enter = slideInHorizontally(initialOffsetX = { -it }),
                        exit = slideOutHorizontally(targetOffsetX = { -it })
                    ) {
                        FileExplorerPane(
                            files = uiState.sourceFiles,
                            activeFileId = uiState.activeSourceFileId,
                            onSelectFile = { viewModel.selectSourceFile(it) },
                            onCreateNewFile = { name, dir, template ->
                                viewModel.createNewSourceFile(name, dir, template)
                            },
                            onDeleteFile = { viewModel.deleteSourceFile(it) },
                            onResetDefaults = { viewModel.resetAllSourceFilesToDefault() },
                            modifier = Modifier.width(270.dp)
                        )
                    }

                    // Right Pane: Editor Area + Output Drawer
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CodeEditorAreaPane(
                            activeFile = uiState.activeSourceFile,
                            openFiles = uiState.openSourceFiles,
                            editorContent = uiState.activeFileEditorContent,
                            fontSizeSp = uiState.editorFontSizeSp,
                            isLineWrapEnabled = uiState.isLineWrapEnabled,
                            codeDiagnostics = uiState.codeDiagnostics,
                            onContentChange = { viewModel.updateActiveEditorContent(it) },
                            onSelectFile = { viewModel.selectSourceFile(it) },
                            onCloseTab = { viewModel.closeFileTab(it) },
                            onSaveFile = { viewModel.saveActiveFile() },
                            onRevertFile = { viewModel.revertActiveFile() },
                            onInsertSnippet = { viewModel.insertCodeSnippet(it) },
                            onFontSizeChange = { viewModel.setEditorFontSize(it) },
                            onToggleLineWrap = { viewModel.toggleLineWrap() },
                            onCheckSyntax = { viewModel.checkSyntax() },
                            onRunBuild = { viewModel.runSimulatedBuild() },
                            onFormatCode = { viewModel.formatActiveCode() },
                            onOpenCommandPalette = { viewModel.showCommandPalette() },
                            onSaveProject = { viewModel.saveProject() },
                            modifier = Modifier.weight(1f)
                        )

                        // Collapsible Bottom Output Pane
                        AnimatedVisibility(visible = uiState.isOutputPaneVisible) {
                            EditorOutputPane(
                                activeFile = uiState.activeSourceFile,
                                currentEditorContent = uiState.activeFileEditorContent,
                                activeTab = uiState.activeOutputTab,
                                buildLogs = uiState.simulatedBuildLogs,
                                syntaxErrors = uiState.syntaxErrors,
                                isBuilding = uiState.isBuildingCode,
                                codeDiagnostics = uiState.codeDiagnostics,
                                onSelectTab = { viewModel.setActiveOutputTab(it) },
                                onRunBuild = { viewModel.runSimulatedBuild() },
                                onCheckSyntax = { viewModel.checkSyntax() },
                                onClosePane = { viewModel.toggleOutputPane() }
                            )
                        }
                    }
                }
            } else {
                // Single/Split Mobile Layout (Phone)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (mobileViewMode == 1) {
                        // File Explorer View
                        FileExplorerPane(
                            files = uiState.sourceFiles,
                            activeFileId = uiState.activeSourceFileId,
                            onSelectFile = {
                                viewModel.selectSourceFile(it)
                                mobileViewMode = 0 // Automatically jump to editor on file select!
                            },
                            onCreateNewFile = { name, dir, template ->
                                viewModel.createNewSourceFile(name, dir, template)
                                mobileViewMode = 0
                            },
                            onDeleteFile = { viewModel.deleteSourceFile(it) },
                            onResetDefaults = { viewModel.resetAllSourceFilesToDefault() },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Editor View
                        CodeEditorAreaPane(
                            activeFile = uiState.activeSourceFile,
                            openFiles = uiState.openSourceFiles,
                            editorContent = uiState.activeFileEditorContent,
                            fontSizeSp = uiState.editorFontSizeSp,
                            isLineWrapEnabled = uiState.isLineWrapEnabled,
                            codeDiagnostics = uiState.codeDiagnostics,
                            onContentChange = { viewModel.updateActiveEditorContent(it) },
                            onSelectFile = { viewModel.selectSourceFile(it) },
                            onCloseTab = { viewModel.closeFileTab(it) },
                            onSaveFile = { viewModel.saveActiveFile() },
                            onRevertFile = { viewModel.revertActiveFile() },
                            onInsertSnippet = { viewModel.insertCodeSnippet(it) },
                            onFontSizeChange = { viewModel.setEditorFontSize(it) },
                            onToggleLineWrap = { viewModel.toggleLineWrap() },
                            onCheckSyntax = { viewModel.checkSyntax() },
                            onRunBuild = { viewModel.runSimulatedBuild() },
                            onFormatCode = { viewModel.formatActiveCode() },
                            onOpenCommandPalette = { viewModel.showCommandPalette() },
                            onSaveProject = { viewModel.saveProject() },
                            modifier = Modifier.weight(1f)
                        )

                        // Bottom Output Pane
                        AnimatedVisibility(visible = uiState.isOutputPaneVisible) {
                            EditorOutputPane(
                                activeFile = uiState.activeSourceFile,
                                currentEditorContent = uiState.activeFileEditorContent,
                                activeTab = uiState.activeOutputTab,
                                buildLogs = uiState.simulatedBuildLogs,
                                syntaxErrors = uiState.syntaxErrors,
                                isBuilding = uiState.isBuildingCode,
                                codeDiagnostics = uiState.codeDiagnostics,
                                onSelectTab = { viewModel.setActiveOutputTab(it) },
                                onRunBuild = { viewModel.runSimulatedBuild() },
                                onCheckSyntax = { viewModel.checkSyntax() },
                                onClosePane = { viewModel.toggleOutputPane() }
                            )
                        }
                    }
                }
            }
        }

        // Command Palette Modal Dialog (Ctrl+Shift+P)
        val commandPaletteActions = remember(uiState) {
            listOf(
                CommandPaletteAction(
                    id = "format_code",
                    title = "Format Code",
                    description = "Tự động căn lề thụt đầu dòng, chuẩn hóa dấu ngoặc & loại bỏ khoảng trắng thừa",
                    category = "EDITOR",
                    shortcut = "Shift+Alt+F",
                    icon = Icons.Default.FormatAlignLeft,
                    keywords = listOf("format", "beautify", "indent", "clean", "prettify", "dinh dang"),
                    onExecute = { viewModel.formatActiveCode() }
                ),
                CommandPaletteAction(
                    id = "deploy",
                    title = "Deploy to Border HQ",
                    description = "Đóng gói trigger package, kiểm tra AST & nạp lên máy chủ Border HQ",
                    category = "BUILD & RUN",
                    shortcut = "Ctrl+Shift+D",
                    icon = Icons.Default.CloudUpload,
                    keywords = listOf("deploy", "publish", "release", "trien khai", "upload", "server"),
                    onExecute = { viewModel.deployProject() }
                ),
                CommandPaletteAction(
                    id = "save_project",
                    title = "Save Project",
                    description = "Lưu và đồng bộ toàn bộ dự án & tập tin tác chiến vào Room DB",
                    category = "PROJECT",
                    shortcut = "Ctrl+Shift+S",
                    icon = Icons.Default.Save,
                    keywords = listOf("save", "save all", "project", "luu", "commit", "sync"),
                    onExecute = { viewModel.saveProject() }
                ),
                CommandPaletteAction(
                    id = "save_file",
                    title = "Save Active File",
                    description = "Lưu nội dung của tập tin đang mở vào cơ sở dữ liệu",
                    category = "EDITOR",
                    shortcut = "Ctrl+S",
                    icon = Icons.Default.SaveAs,
                    keywords = listOf("save", "luu file", "file"),
                    onExecute = { viewModel.saveActiveFile() }
                ),
                CommandPaletteAction(
                    id = "run_build",
                    title = "Run Build & Compile",
                    description = "Chạy tiến trình Gradle build, kiểm tra tài nguyên & biên dịch JVM",
                    category = "BUILD & RUN",
                    shortcut = "Ctrl+B",
                    icon = Icons.Default.PlayArrow,
                    keywords = listOf("build", "compile", "run", "gradle", "bien dich"),
                    onExecute = { viewModel.runSimulatedBuild() }
                ),
                CommandPaletteAction(
                    id = "check_syntax",
                    title = "Check Syntax & Diagnostics",
                    description = "Quét kiểm tra lỗi cú pháp, cân bằng ngoặc & cảnh báo logic",
                    category = "DIAGNOSTICS",
                    shortcut = "Ctrl+K",
                    icon = Icons.Default.Rule,
                    keywords = listOf("syntax", "lint", "diagnostics", "check", "error", "kiem tra", "cu phap"),
                    onExecute = { viewModel.checkSyntax() }
                ),
                CommandPaletteAction(
                    id = "toggle_line_wrap",
                    title = "Toggle Line Wrap",
                    description = "Bật hoặc tắt chế độ tự động ngắt dòng",
                    category = "VIEW",
                    shortcut = "Alt+Z",
                    icon = Icons.Default.WrapText,
                    keywords = listOf("wrap", "line wrap", "ngat dong", "scroll"),
                    onExecute = { viewModel.toggleLineWrap() }
                ),
                CommandPaletteAction(
                    id = "toggle_explorer",
                    title = "Toggle File Explorer",
                    description = "Ẩn hoặc hiện cây thư mục tập tin dự án",
                    category = "VIEW",
                    shortcut = "Ctrl+Shift+E",
                    icon = Icons.Default.FolderOpen,
                    keywords = listOf("explorer", "files", "tree", "thu muc", "sidebar"),
                    onExecute = { viewModel.toggleExplorerPane() }
                ),
                CommandPaletteAction(
                    id = "toggle_output",
                    title = "Toggle Output & Terminal Drawer",
                    description = "Mở hoặc thu gọn ngăn Terminal, kết quả Build và Linter",
                    category = "VIEW",
                    shortcut = "Ctrl+`",
                    icon = Icons.Default.Terminal,
                    keywords = listOf("terminal", "output", "drawer", "console", "logs"),
                    onExecute = { viewModel.toggleOutputPane() }
                ),
                CommandPaletteAction(
                    id = "zoom_in",
                    title = "Increase Font Size",
                    description = "Phóng to kích thước chữ trong khung soạn thảo (+1.5sp)",
                    category = "EDITOR",
                    shortcut = "Ctrl++",
                    icon = Icons.Default.ZoomIn,
                    keywords = listOf("zoom", "font", "size", "tang chu", "bigger"),
                    onExecute = { viewModel.setEditorFontSize(uiState.editorFontSizeSp + 1.5f) }
                ),
                CommandPaletteAction(
                    id = "zoom_out",
                    title = "Decrease Font Size",
                    description = "Thu nhỏ kích thước chữ trong khung soạn thảo (-1.5sp)",
                    category = "EDITOR",
                    shortcut = "Ctrl+-",
                    icon = Icons.Default.ZoomOut,
                    keywords = listOf("zoom", "font", "size", "giam chu", "smaller"),
                    onExecute = { viewModel.setEditorFontSize(uiState.editorFontSizeSp - 1.5f) }
                ),
                CommandPaletteAction(
                    id = "reset_zoom",
                    title = "Reset Font Size",
                    description = "Khôi phục kích thước chữ về mặc định (13.5sp)",
                    category = "EDITOR",
                    shortcut = "Ctrl+0",
                    icon = Icons.Default.RestartAlt,
                    keywords = listOf("reset", "font", "default", "mac dinh"),
                    onExecute = { viewModel.setEditorFontSize(13.5f) }
                ),
                CommandPaletteAction(
                    id = "insert_snippet_trigger",
                    title = "Insert Snippet: Trigger Function",
                    description = "Chèn hàm executeTrigger() xử lý điểm trion và logic kích hoạt",
                    category = "EDITOR",
                    icon = Icons.Default.Bolt,
                    keywords = listOf("snippet", "trigger", "function", "chen ma"),
                    onExecute = {
                        viewModel.insertCodeSnippet(
                            "fun executeTrigger(agentId: String, power: Int = 100): Boolean {\n" +
                            "    // Border Tactical Execution\n" +
                            "    val trionCost = power * 2\n" +
                            "    return trionCost > 0\n" +
                            "}"
                        )
                    }
                ),
                CommandPaletteAction(
                    id = "insert_snippet_radar",
                    title = "Insert Snippet: Radar Scanning",
                    description = "Chèn đoạn mã quét lân cận scanNeighbors() phát hiện mục tiêu lân cận",
                    category = "EDITOR",
                    icon = Icons.Default.Radar,
                    keywords = listOf("snippet", "radar", "scan", "quet radar"),
                    onExecute = {
                        viewModel.insertCodeSnippet(
                            "fun scanRadarGrid(rangeKm: Double = 5.0): List<String> {\n" +
                            "    // Border HQ Radar Scanning Matrix\n" +
                            "    return listOf(\"Target_Neighbor_Alpha\", \"Target_Neighbor_Beta\")\n" +
                            "}"
                        )
                    }
                ),
                CommandPaletteAction(
                    id = "revert_file",
                    title = "Revert Current File",
                    description = "Hoàn tác tập tin hiện tại về nội dung gốc ban đầu",
                    category = "PROJECT",
                    icon = Icons.Default.Undo,
                    keywords = listOf("revert", "discard", "undo all", "khoi phuc"),
                    onExecute = { viewModel.revertActiveFile() }
                ),
                CommandPaletteAction(
                    id = "reset_all_files",
                    title = "Reset All Files to Default",
                    description = "Đặt lại toàn bộ cây thư mục và tập tin về cấu hình mẫu ban đầu",
                    category = "PROJECT",
                    icon = Icons.Default.Restore,
                    keywords = listOf("reset all", "restore", "defaults", "khoi phuc tat ca"),
                    onExecute = { viewModel.resetAllSourceFilesToDefault() }
                )
            )
        }

        CommandPaletteModal(
            isOpen = uiState.isCommandPaletteVisible,
            actions = commandPaletteActions,
            onDismiss = { viewModel.hideCommandPalette() }
        )
    }
}

