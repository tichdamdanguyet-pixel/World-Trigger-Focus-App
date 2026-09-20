package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "solo_rank_matches")
data class SoloRankMatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val agentName: String,
    val opponentName: String,
    val opponentRank: String,
    val playerRoll: Int,
    val enemyRoll: Int,
    val won: Boolean,
    val pointsDiff: Int,
    val finalPoints: Int,
    val tacticNote: String,
    val timestamp: Long = System.currentTimeMillis()
)
