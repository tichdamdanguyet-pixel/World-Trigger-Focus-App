package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a source code file or directory in the project structure.
 */
@Entity(tableName = "project_source_files")
data class SourceFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,             // e.g. "app/src/main/java/com/example/AgentViewModel.kt"
    val fileName: String,             // e.g. "AgentViewModel.kt"
    val fileExtension: String,        // "kt", "kts", "json", "xml", "md", "txt"
    val parentDir: String,            // e.g. "app/src/main/java/com/example"
    val content: String,              // Full text source code content
    val originalContent: String = "", // Used for diff checking and revert
    val isDirectory: Boolean = false,
    val isModified: Boolean = false,
    val lastModifiedTimestamp: Long = System.currentTimeMillis()
)
