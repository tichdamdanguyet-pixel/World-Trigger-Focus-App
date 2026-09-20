package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a study/training task that users can create, track,
 * and complete within their focus and Pomodoro sessions.
 * Supports custom tags and categories (e.g. 'Coding', 'Reading', etc.).
 */
@Entity(tableName = "study_tasks")
data class StudyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "Coding", // e.g. "Coding", "Reading", "Ngoại ngữ", "Chiến thuật" or custom
    val tags: String = "",           // Comma-separated custom tags e.g. "Coding, Android" or "Reading, Docs"
    val targetPomodoros: Int = 1,    // Estimated Pomodoro cycles (e.g. 1-4)
    val completedPomodoros: Int = 0, // Pomodoro cycles spent on this task
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    /**
     * Returns a combined distinct list of tags and category for filtering and display.
     */
    val tagList: List<String>
        get() {
            val list = mutableListOf<String>()
            if (category.isNotBlank()) {
                list.add(category.trim())
            }
            if (tags.isNotBlank()) {
                tags.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !list.contains(it) }
                    .forEach { list.add(it) }
            }
            return list
        }
}
