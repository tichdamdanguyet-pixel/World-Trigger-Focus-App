package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.SoloRankMatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SoloRankMatchDao {
    @Query("SELECT * FROM solo_rank_matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<SoloRankMatchEntity>>

    @Query("SELECT * FROM solo_rank_matches ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMatches(limit: Int): Flow<List<SoloRankMatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(match: SoloRankMatchEntity): Long

    @Query("SELECT COUNT(*) FROM solo_rank_matches")
    fun getMatchCount(): Flow<Int>

    @Query("DELETE FROM solo_rank_matches")
    suspend fun clearAllMatches()

    @Query("DELETE FROM solo_rank_matches WHERE id = :id")
    suspend fun deleteMatchById(id: Long)
}
