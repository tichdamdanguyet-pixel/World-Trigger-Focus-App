package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_solo_rank_missions")
data class DailySoloRankMissionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val targetCount: Int = 1,
    val currentProgress: Int = 0,
    val rewardPoints: Int = 100,
    val isCompleted: Boolean = false,
    val dateString: String, // Format: YYYY-MM-DD
    val missionCategory: String = "SOLO_WAR", // SOLO_WAR, STUDY_TACTIC, FOCUS_TRAINING, COMBAT_STREAK
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val customNotes: String = "",
    val customTags: String = ""
)
