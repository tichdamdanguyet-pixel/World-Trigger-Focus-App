package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.SourceFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceFileDao {
    @Query("SELECT * FROM project_source_files ORDER BY isDirectory DESC, fileName ASC")
    fun getAllFiles(): Flow<List<SourceFileEntity>>

    @Query("SELECT * FROM project_source_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): SourceFileEntity?

    @Query("SELECT * FROM project_source_files WHERE filePath = :path LIMIT 1")
    suspend fun getFileByPath(path: String): SourceFileEntity?

    @Query("SELECT COUNT(*) FROM project_source_files")
    suspend fun getFilesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: SourceFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<SourceFileEntity>)

    @Update
    suspend fun updateFile(file: SourceFileEntity)

    @Delete
    suspend fun deleteFile(file: SourceFileEntity)

    @Query("DELETE FROM project_source_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM project_source_files")
    suspend fun deleteAll()
}
