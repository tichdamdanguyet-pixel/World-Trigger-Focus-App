package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DailySoloRankMissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySoloRankMissionDao {
    @Query("SELECT * FROM daily_solo_rank_missions ORDER BY isCompleted ASC, id ASC")
    fun getAllMissions(): Flow<List<DailySoloRankMissionEntity>>

    @Query("SELECT * FROM daily_solo_rank_missions WHERE isCompleted = 1 ORDER BY (CASE WHEN completedAt IS NOT NULL THEN completedAt ELSE createdAt END) DESC, id DESC")
    fun getCompletedMissions(): Flow<List<DailySoloRankMissionEntity>>

    @Query("SELECT * FROM daily_solo_rank_missions WHERE dateString = :date ORDER BY isCompleted ASC, id ASC")
    fun getMissionsByDate(date: String): Flow<List<DailySoloRankMissionEntity>>

    @Query("SELECT * FROM daily_solo_rank_missions WHERE dateString = :date ORDER BY isCompleted ASC, id ASC")
    suspend fun getMissionsByDateSync(date: String): List<DailySoloRankMissionEntity>

    @Query("SELECT * FROM daily_solo_rank_missions WHERE id = :id LIMIT 1")
    suspend fun getMissionById(id: Long): DailySoloRankMissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: DailySoloRankMissionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<DailySoloRankMissionEntity>)

    @Update
    suspend fun updateMission(mission: DailySoloRankMissionEntity)

    @Query("UPDATE daily_solo_rank_missions SET customNotes = :notes, customTags = :tags WHERE id = :id")
    suspend fun updateCustomNotesAndTags(id: Long, notes: String, tags: String)

    @Query("UPDATE daily_solo_rank_missions SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean, completedAt: Long?)

    @Query("UPDATE daily_solo_rank_missions SET currentProgress = :progress, isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :id")
    suspend fun updateProgressAndCompletion(id: Long, progress: Int, isCompleted: Boolean, completedAt: Long?)

    @Query("DELETE FROM daily_solo_rank_missions WHERE id = :id")
    suspend fun deleteMissionById(id: Long)

    @Query("DELETE FROM daily_solo_rank_missions WHERE dateString = :date")
    suspend fun deleteMissionsForDate(date: String)

    @Query("DELETE FROM daily_solo_rank_missions")
    suspend fun clearAllMissions()
}
