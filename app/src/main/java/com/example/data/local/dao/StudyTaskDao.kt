package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.StudyTaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing study tasks in Room.
 */
@Dao
interface StudyTaskDao {

    @Query("SELECT * FROM study_tasks ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    fun getActiveTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): StudyTaskEntity?

    @Query("SELECT * FROM study_tasks WHERE category = :category ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE tags LIKE '%' || :tag || '%' ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksByTag(tag: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT * FROM study_tasks WHERE (:filter = '' OR category = :filter OR tags LIKE '%' || :filter || '%') ORDER BY isCompleted ASC, createdAt DESC")
    fun getFilteredTasks(filter: String): Flow<List<StudyTaskEntity>>

    @Query("SELECT DISTINCT category FROM study_tasks WHERE category != ''")
    fun getAllCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTaskEntity): Long

    @Update
    suspend fun updateTask(task: StudyTaskEntity)

    @Delete
    suspend fun deleteTask(task: StudyTaskEntity)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE study_tasks SET isCompleted = :completed, completedAt = :completedAt WHERE id = :id")
    suspend fun updateTaskCompletion(id: Long, completed: Boolean, completedAt: Long?)

    @Query("UPDATE study_tasks SET completedPomodoros = completedPomodoros + 1 WHERE id = :id")
    suspend fun incrementTaskPomodoro(id: Long)

    @Query("SELECT COUNT(*) FROM study_tasks WHERE isCompleted = 0")
    fun getActiveTaskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM study_tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>
}
