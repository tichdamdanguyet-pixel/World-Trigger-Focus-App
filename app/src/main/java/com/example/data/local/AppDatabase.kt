package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.DailySoloRankMissionDao
import com.example.data.local.dao.SoloRankMatchDao
import com.example.data.local.dao.SourceFileDao
import com.example.data.local.dao.StudyTaskDao
import com.example.data.local.entity.DailySoloRankMissionEntity
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.data.local.entity.SourceFileEntity
import com.example.data.local.entity.StudyTaskEntity

@Database(
    entities = [
        SoloRankMatchEntity::class,
        StudyTaskEntity::class,
        DailySoloRankMissionEntity::class,
        SourceFileEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun soloRankMatchDao(): SoloRankMatchDao
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun dailySoloRankMissionDao(): DailySoloRankMissionDao
    abstract fun sourceFileDao(): SourceFileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE daily_solo_rank_missions ADD COLUMN customNotes TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE daily_solo_rank_missions ADD COLUMN customTags TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "border_agent_db"
                )
                    .addMigrations(MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
