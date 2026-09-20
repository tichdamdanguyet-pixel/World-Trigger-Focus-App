package com.example.ui.components.codeeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.SourceFileEntity
import com.example.ui.theme.*

/**
 * Multi-Pane File Explorer Pane
 * Displays project hierarchical folder structure, search filter, and file actions.
 */
@Composable
fun FileExplorerPane(
    files: List<SourceFileEntity>,
    activeFileId: Long?,
    onSelectFile: (Long) -> Unit,
    onCreateNewFile: (name: String, parentDir: String, templateType: String) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var fileToDelete by remember { mutableStateOf<SourceFileEntity?>(null) }

    // Map of expanded folders (path -> isExpanded)
    var expandedFolders by remember {
        mutableStateOf(
            mapOf(
                "app" to true,
                "app/src" to true,
                "app/src/main" to true,
                "app/src/main/java" to true,
                "app/src/main/java/com/example" to true,
                "app/src/main/java/com/example/ui" to true,
                "app/src/main/java/com/example/model" to true,
                "app/src/main/java/com/example/util" to true,
                "app/src/main/res" to true,
                "app/src/main/res/values" to true,
                "config" to true
            )
        )
    }

    // Filtered files based on search
    val filteredFiles = remember(files, searchQuery) {
        if (searchQuery.isBlank()) files
        else files.filter {
            it.fileName.contains(searchQuery, ignoreCase = true) ||
            it.filePath.contains(searchQuery, ignoreCase = true)
        }
    }

    // Extract unique directories
    val directories = remember(files) {
        val set = sortedSetOf<String>()
        files.forEach { file ->
            if (file.parentDir.isNotBlank()) {
                val parts = file.parentDir.split("/")
                var currentPath = ""
                parts.forEach { part ->
                    currentPath = if (currentPath.isEmpty()) part else "$currentPath/$part"
                    set.add(currentPath)
                }
            }
        }
        set.toList()
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(Slate900)
            .border(1.dp, Slate800)
            .testTag("pane_file_explorer")
    ) {
        // Explorer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate950)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = Cyan400,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DỰ ÁN: BORDER-AGENT",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Slate200
                )
            }

            // Quick Actions: New File & Reset
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = { showNewFileDialog = true },
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("btn_new_file")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm tập tin mới",
                        tint = Cyan300,
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { showResetConfirmDialog = true },
                    modifier = Modifier
                        .size(26.dp)
                        .testTag("btn_reset_files")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Khôi phục mẫu gốc",
                        tint = Slate400,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Search Filter Input
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("input_explorer_search"),
                placeholder = {
                    Text(
                        text = "Tìm kiếm tập tin...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Slate500
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(15.dp)
                    )
                },
                trailingIcon = {
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
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate100
                ),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan400,
                    unfocusedBorderColor = Slate700,
                    focusedContainerColor = Slate950,
                    unfocusedContainerColor = Slate950
                )
            )
        }

        // Quick File count and Project Branch info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filteredFiles.size} TẬP TIN",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Slate400
            )
            Text(
                text = "BRANCH: MAIN",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Cyan400
            )
        }

        Divider(color = Slate800, thickness = 1.dp)

        // File List Tree
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            if (searchQuery.isNotBlank()) {
                // Flat Search Results
                items(filteredFiles, key = { it.id }) { file ->
                    FileTreeItem(
                        file = file,
                        isActive = file.id == activeFileId,
                        depth = 0,
                        onSelect = { onSelectFile(file.id) },
                        onDelete = { fileToDelete = file }
                    )
                }
            } else {
                // Grouped by parent directory or flat hierarchy
                // Root files
                val rootFiles = files.filter { it.parentDir.isBlank() }
                items(rootFiles, key = { it.id }) { file ->
                    FileTreeItem(
                        file = file,
                        isActive = file.id == activeFileId,
                        depth = 0,
                        onSelect = { onSelectFile(file.id) },
                        onDelete = { fileToDelete = file }
                    )
                }

                // Directories and their files
                directories.forEach { dirPath ->
                    val dirFiles = files.filter { it.parentDir == dirPath }
                    if (dirFiles.isNotEmpty()) {
                        val isExpanded = expandedFolders[dirPath] ?: true
                        val depth = dirPath.count { it == '/' }
                        val dirName = dirPath.substringAfterLast('/')

                        item(key = "dir_$dirPath") {
                            DirectoryHeaderItem(
                                name = dirName,
                                path = dirPath,
                                depth = depth,
                                isExpanded = isExpanded,
                                onToggle = {
                                    expandedFolders = expandedFolders.toMutableMap().apply {
                                        put(dirPath, !isExpanded)
                                    }
                                }
                            )
                        }

                        if (isExpanded) {
                            items(dirFiles, key = { it.id }) { file ->
                                FileTreeItem(
                                    file = file,
                                    isActive = file.id == activeFileId,
                                    depth = depth + 1,
                                    onSelect = { onSelectFile(file.id) },
                                    onDelete = { fileToDelete = file }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        NewFileDialog(
            directories = directories,
            onDismiss = { showNewFileDialog = false },
            onCreate = { name, dir, template ->
                onCreateNewFile(name, dir, template)
                showNewFileDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = {
                Text(
                    text = "XÓA TẬP TIN?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Slate100
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn xóa tập tin '${file.fileName}' khỏi dự án không? Thao tác này không thể hoàn tác.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate300
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteFile(file.id)
                        fileToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedFailDark)
                ) {
                    Text("XÓA", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("HỦY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }

    // Reset Defaults Confirmation Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = {
                Text(
                    text = "KHÔI PHỤC CÂY THƯ MỤC GỐC?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Slate100
                )
            },
            text = {
                Text(
                    text = "Hệ thống sẽ xóa toàn bộ các tập tin tùy chỉnh hiện tại và tái tạo lại bộ mã nguồn gốc của Border Agent OS. Bạn có muốn tiếp tục?",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Slate300
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
                ) {
                    Text("KHÔI PHỤC", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("HỦY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Slate400)
                }
            },
            containerColor = Slate900
        )
    }
}

@Composable
fun DirectoryHeaderItem(
    name: String,
    path: String,
    depth: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(start = (depth * 12 + 6).dp, top = 4.dp, bottom = 4.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Slate400,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = null,
            tint = Cyan400,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = name,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = Slate300,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FileTreeItem(
    file: SourceFileEntity,
    isActive: Boolean,
    depth: Int,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val (badgeColor, badgeText, fileIcon) = when (file.fileExtension.lowercase()) {
        "kt" -> Triple(RankARank, "KT", Icons.Default.Code)
        "kts" -> Triple(OrangeWarning, "KTS", Icons.Default.Build)
        "xml" -> Triple(Amber400, "XML", Icons.Default.DataObject)
        "json" -> Triple(Cyan400, "JSON", Icons.Default.Code)
        "md" -> Triple(RankBRank, "MD", Icons.Default.Description)
        else -> Triple(Slate400, file.fileExtension.uppercase().take(3), Icons.Default.InsertDriveFile)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isActive) Cyan500.copy(alpha = 0.15f) else Color.Transparent)
            .border(
                width = if (isActive) 1.dp else 0.dp,
                color = if (isActive) Cyan400.copy(alpha = 0.4f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onSelect() }
            .padding(start = (depth * 12 + 18).dp, top = 5.dp, bottom = 5.dp, end = 6.dp)
            .testTag("file_item_${file.fileName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Extension badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 4.dp, vertical = 1.5.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = badgeColor
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // File Name
            Text(
                text = file.fileName,
                fontSize = 11.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = if (isActive) Cyan200(badgeColor) else Slate200,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Dirty / Unsaved indicator
            if (file.isModified) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Amber400)
                )
            }
        }

        // Options icon button
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Tùy chọn tập tin",
                    tint = Slate500,
                    modifier = Modifier.size(13.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(Slate900)
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Xóa tập tin",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = RedFailDark.copy(alpha = 0.9f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = RedFailDark,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

// Helper to tint active text cleanly
private fun Cyan200(fallback: Color): Color = Cyan300

@Composable
fun NewFileDialog(
    directories: List<String>,
    onDismiss: () -> Unit,
    onCreate: (name: String, parentDir: String, template: String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var selectedDir by remember { mutableStateOf(directories.firstOrNull() ?: "") }
    var selectedTemplate by remember { mutableStateOf("KOTLIN") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "TẠO TẬP TIN MÃ NGUỒN MỚI",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = Slate100
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Tên tập tin (ví dụ: CustomTrigger.kt)", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_file_name"),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                )

                // Template selection
                Text(
                    text = "ĐỊNH DẠNG MẪU:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Slate400
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("KOTLIN" to ".kt", "JSON" to ".json", "XML" to ".xml", "MD" to ".md").forEach { (type, ext) ->
                        val selected = selectedTemplate == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (selected) Cyan500 else Slate800)
                                .clickable {
                                    selectedTemplate = type
                                    if (!fileName.contains(".")) {
                                        fileName = "$fileName$ext"
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = type,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = if (selected) Slate950 else Slate300
                            )
                        }
                    }
                }

                // Target directory
                Text(
                    text = "THƯ MỤC ĐÍCH: ${if (selectedDir.isEmpty()) "(Gốc dự án)" else selectedDir}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Cyan300
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onCreate(fileName.trim(), selectedDir, selectedTemplate)
                    }
                },
                enabled = fileName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan500)
            ) {
                Text("TẠO MỚI", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate950)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("HỦY", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Slate400)
            }
        },
        containerColor = Slate900
    )
}
