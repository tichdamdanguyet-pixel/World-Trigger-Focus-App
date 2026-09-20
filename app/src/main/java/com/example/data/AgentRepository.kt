package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.AppDatabase
import com.example.data.local.entity.DailySoloRankMissionEntity
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.model.AgentProfile
import com.example.model.AgentTriggerSet
import com.example.model.DEFAULT_MISSIONS
import com.example.model.SoloMatchResult
import com.example.model.TrainingMission
import com.example.model.TriggerSlot
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class AgentRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("border_agent_prefs", Context.MODE_PRIVATE)
    private val database: AppDatabase = AppDatabase.getInstance(context)
    private val matchDao = database.soloRankMatchDao()
    private val studyTaskDao = database.studyTaskDao()
    private val dailyMissionDao = database.dailySoloRankMissionDao()
    private val sourceFileDao = database.sourceFileDao()

    val allMatches: Flow<List<SoloRankMatchEntity>> = matchDao.getAllMatches()
    val allStudyTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getAllTasks()
    val activeStudyTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getActiveTasks()
    val allDailyMissions: Flow<List<DailySoloRankMissionEntity>> = dailyMissionDao.getAllMissions()
    val completedDailyMissions: Flow<List<DailySoloRankMissionEntity>> = dailyMissionDao.getCompletedMissions()
    val allSourceFiles: Flow<List<com.example.data.local.entity.SourceFileEntity>> = sourceFileDao.getAllFiles()

    fun getDailyMissions(date: String): Flow<List<DailySoloRankMissionEntity>> =
        dailyMissionDao.getMissionsByDate(date)

    suspend fun getDailyMissionsSync(date: String): List<DailySoloRankMissionEntity> =
        dailyMissionDao.getMissionsByDateSync(date)

    suspend fun insertDailyMission(mission: DailySoloRankMissionEntity): Long =
        dailyMissionDao.insertMission(mission)

    suspend fun insertDailyMissions(missions: List<DailySoloRankMissionEntity>) =
        dailyMissionDao.insertMissions(missions)

    suspend fun updateDailyMission(mission: DailySoloRankMissionEntity) =
        dailyMissionDao.updateMission(mission)

    suspend fun updateDailyMissionNotesAndTags(id: Long, notes: String, tags: String) =
        dailyMissionDao.updateCustomNotesAndTags(id, notes.trim(), tags.trim())

    suspend fun toggleDailyMissionCompletion(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        val existing = dailyMissionDao.getMissionById(id)
        if (existing != null) {
            val progress = if (completed) existing.targetCount else 0
            dailyMissionDao.updateProgressAndCompletion(id, progress, completed, completedAt)
        } else {
            dailyMissionDao.updateCompletionStatus(id, completed, completedAt)
        }
    }

    suspend fun incrementDailyMissionProgress(id: Long, amount: Int = 1): Boolean {
        val existing = dailyMissionDao.getMissionById(id) ?: return false
        val newProgress = (existing.currentProgress + amount).coerceAtMost(existing.targetCount)
        val isNowCompleted = newProgress >= existing.targetCount
        val completedAt = if (isNowCompleted) System.currentTimeMillis() else existing.completedAt
        dailyMissionDao.updateProgressAndCompletion(id, newProgress, isNowCompleted, completedAt)
        return isNowCompleted && !existing.isCompleted
    }

    suspend fun deleteDailyMission(id: Long) =
        dailyMissionDao.deleteMissionById(id)

    suspend fun seedDefaultDailyMissionsIfEmpty(date: String) {
        val existing = dailyMissionDao.getMissionsByDateSync(date)
        if (existing.isEmpty()) {
            val defaultList = listOf(
                DailySoloRankMissionEntity(
                    title = "Tham chiến 2 trận Solo Rank War",
                    description = "Ghi danh và thi đấu ít nhất 2 trận trong đấu trường Solo Rank để rèn dũa phản xạ chiến thuật.",
                    targetCount = 2,
                    currentProgress = 0,
                    rewardPoints = 150,
                    isCompleted = false,
                    dateString = date,
                    missionCategory = "SOLO_WAR",
                    customNotes = "Ưu tiên phản công tầm trung, kiểm soát nhịp độ",
                    customTags = "Rank War, Tác chiến"
                ),
                DailySoloRankMissionEntity(
                    title = "Hạ gục đối thủ & Giành 1 trận thắng",
                    description = "Đạt điểm xúc xắc áp đảo đối thủ Border để mang về 1 chiến thắng Solo Rank vinh quang.",
                    targetCount = 1,
                    currentProgress = 0,
                    rewardPoints = 200,
                    isCompleted = false,
                    dateString = date,
                    missionCategory = "WIN_MATCH",
                    customNotes = "Tận dụng đòn bộc phá Kogetsu",
                    customTags = "Chiến thắng, Solo War"
                ),
                DailySoloRankMissionEntity(
                    title = "Huấn luyện tập trung Pomodoro 30 phút",
                    description = "Duy trì trạng thái tập trung sâu trong phiên học tập/nghiên cứu Trion không xao nhãng.",
                    targetCount = 30,
                    currentProgress = 0,
                    rewardPoints = 150,
                    isCompleted = false,
                    dateString = date,
                    missionCategory = "FOCUS_TRAINING",
                    customNotes = "Không xao nhãng, ngắt mọi thông báo ngoại vi",
                    customTags = "Pomodoro, Tập trung"
                ),
                DailySoloRankMissionEntity(
                    title = "Nghiên cứu tài liệu chiến thuật Border",
                    description = "Đọc và ôn tập kiến thức chuyên môn, tối ưu hóa thiết lập Trigger Set cho đặc vụ.",
                    targetCount = 1,
                    currentProgress = 0,
                    rewardPoints = 100,
                    isCompleted = false,
                    dateString = date,
                    missionCategory = "STUDY_TACTIC",
                    customNotes = "Xem lại sơ đồ di chuyển của Tamakoma-2",
                    customTags = "Chiến thuật, Trigger Set"
                ),
                DailySoloRankMissionEntity(
                    title = "Xác lập chuỗi thắng 2 trận liên tiếp",
                    description = "Chứng tỏ bản lĩnh tinh anh bằng cách thắng liền 2 đối thủ trong ngày mà không bị hạ.",
                    targetCount = 2,
                    currentProgress = 0,
                    rewardPoints = 300,
                    isCompleted = false,
                    dateString = date,
                    missionCategory = "COMBAT_STREAK",
                    customNotes = "Duy trì bình tĩnh khi gặp đối thủ Rank A",
                    customTags = "Chuỗi thắng, Đột phá"
                )
            )
            dailyMissionDao.insertMissions(defaultList)
        }
    }

    suspend fun insertMatchRecord(match: SoloRankMatchEntity): Long {
        return matchDao.insertMatch(match)
    }

    suspend fun clearMatchHistory() {
        matchDao.clearAllMatches()
    }

    // --- Study Tasks Operations ---
    suspend fun insertStudyTask(task: StudyTaskEntity): Long {
        return studyTaskDao.insertTask(task)
    }

    suspend fun updateStudyTask(task: StudyTaskEntity) {
        studyTaskDao.updateTask(task)
    }

    suspend fun deleteStudyTask(task: StudyTaskEntity) {
        studyTaskDao.deleteTask(task)
    }

    suspend fun deleteStudyTaskById(id: Long) {
        studyTaskDao.deleteTaskById(id)
    }

    suspend fun toggleStudyTaskCompletion(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        studyTaskDao.updateTaskCompletion(id, completed, completedAt)
    }

    suspend fun incrementTaskPomodoroCount(id: Long) {
        studyTaskDao.incrementTaskPomodoro(id)
    }

    fun getTasksByCategory(category: String): Flow<List<StudyTaskEntity>> {
        return studyTaskDao.getTasksByCategory(category)
    }

    fun getTasksByTag(tag: String): Flow<List<StudyTaskEntity>> {
        return studyTaskDao.getTasksByTag(tag)
    }

    fun getFilteredTasks(filter: String): Flow<List<StudyTaskEntity>> {
        return studyTaskDao.getFilteredTasks(filter)
    }

    fun hasSeededStudyTasks(): Boolean = prefs.getBoolean(KEY_SEEDED_STUDY_TASKS, false)

    fun markSeededStudyTasks() {
        prefs.edit().putBoolean(KEY_SEEDED_STUDY_TASKS, true).apply()
    }

    companion object {
        private const val KEY_SEEDED_STUDY_TASKS = "has_seeded_study_tasks"
        private const val KEY_TRION_POINTS = "trion_points"
        private const val KEY_AGENT_NAME = "agent_name"
        private const val KEY_AGENT_AGE = "agent_age"
        private const val KEY_AGENT_GENDER = "agent_gender"
        private const val KEY_AGENT_POSITION = "agent_position"
        private const val KEY_AGENT_PICTURE_URI = "agent_picture_uri"
        private const val KEY_PROFILE_REGISTERED = "agent_profile_registered"
        private const val KEY_TRIGGER_TYPE = "trigger_type"
        private const val KEY_FOCUS_TIME_RANK = "focus_time_rank"
        private const val KEY_MISSIONS = "training_missions"
        private const val KEY_LAST_MATCH = "last_match_result"
        private const val KEY_CUSTOM_TRIGGER_SET = "custom_trigger_set_json"
        private const val KEY_POMODORO_TOTAL = "pomodoro_total_count"
        private const val KEY_POMODORO_LOGS = "pomodoro_session_logs_json"
        private const val KEY_TIMER_SOUND_ENABLED = "timer_sound_enabled"
        private const val KEY_REMINDER_ENABLED = "daily_mission_reminder_enabled"
        private const val KEY_REMINDER_HOUR = "daily_mission_reminder_hour"
        private const val KEY_REMINDER_MINUTE = "daily_mission_reminder_minute"
    }

    fun isTimerSoundEnabled(): Boolean = prefs.getBoolean(KEY_TIMER_SOUND_ENABLED, true)

    fun saveTimerSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TIMER_SOUND_ENABLED, enabled).apply()
    }

    fun getPomodoroTotalCount(): Int = prefs.getInt(KEY_POMODORO_TOTAL, 0)

    fun savePomodoroTotalCount(count: Int) {
        prefs.edit().putInt(KEY_POMODORO_TOTAL, count.coerceAtLeast(0)).apply()
    }

    fun getPomodoroLogs(): List<com.example.model.PomodoroSessionLog> {
        val jsonStr = prefs.getString(KEY_POMODORO_LOGS, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<com.example.model.PomodoroSessionLog>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    com.example.model.PomodoroSessionLog(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        subject = obj.optString("subject", "Học tập"),
                        durationMinutes = obj.optInt("durationMinutes", 25),
                        cycleNumber = obj.optInt("cycleNumber", 1),
                        earnedPoints = obj.optInt("earnedPoints", 2)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePomodoroLogs(logs: List<com.example.model.PomodoroSessionLog>) {
        try {
            val array = JSONArray()
            for (log in logs.take(30)) {
                val obj = JSONObject().apply {
                    put("id", log.id)
                    put("timestamp", log.timestamp)
                    put("subject", log.subject)
                    put("durationMinutes", log.durationMinutes)
                    put("cycleNumber", log.cycleNumber)
                    put("earnedPoints", log.earnedPoints)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_POMODORO_LOGS, array.toString()).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun getAgentProfile(): AgentProfile {
        return AgentProfile(
            name = prefs.getString(KEY_AGENT_NAME, "Belén Salinas") ?: "Belén Salinas",
            age = prefs.getInt(KEY_AGENT_AGE, 15),
            gender = prefs.getString(KEY_AGENT_GENDER, "Nữ") ?: "Nữ",
            position = prefs.getString(KEY_AGENT_POSITION, "Sniper") ?: "Sniper",
            pictureUri = prefs.getString(KEY_AGENT_PICTURE_URI, null),
            isRegistered = prefs.getBoolean(KEY_PROFILE_REGISTERED, false)
        )
    }

    fun saveAgentProfile(profile: AgentProfile) {
        prefs.edit()
            .putString(KEY_AGENT_NAME, profile.name)
            .putInt(KEY_AGENT_AGE, profile.age)
            .putString(KEY_AGENT_GENDER, profile.gender)
            .putString(KEY_AGENT_POSITION, profile.position)
            .putString(KEY_AGENT_PICTURE_URI, profile.pictureUri)
            .putBoolean(KEY_PROFILE_REGISTERED, profile.isRegistered)
            .apply()
    }

    fun updatePictureUri(uri: String?) {
        prefs.edit().putString(KEY_AGENT_PICTURE_URI, uri).apply()
    }

    fun isProfileRegistered(): Boolean = prefs.getBoolean(KEY_PROFILE_REGISTERED, false)

    fun getTrionPoints(): Int = prefs.getInt(KEY_TRION_POINTS, 1000)

    fun saveTrionPoints(points: Int) {
        prefs.edit().putInt(KEY_TRION_POINTS, points.coerceAtLeast(0)).apply()
    }

    fun getAgentName(): String = prefs.getString(KEY_AGENT_NAME, "Belén Salinas") ?: "Belén Salinas"

    fun saveAgentName(name: String) {
        prefs.edit().putString(KEY_AGENT_NAME, name).apply()
    }

    fun getAgentAge(): Int = prefs.getInt(KEY_AGENT_AGE, 15)

    fun saveAgentAge(age: Int) {
        prefs.edit().putInt(KEY_AGENT_AGE, age).apply()
    }

    fun getTriggerType(): String = prefs.getString(KEY_TRIGGER_TYPE, "Ibis (Sniper)") ?: "Ibis (Sniper)"

    fun saveTriggerType(type: String) {
        prefs.edit().putString(KEY_TRIGGER_TYPE, type).apply()
    }

    fun getFocusTimeForRank(): Int = prefs.getInt(KEY_FOCUS_TIME_RANK, 0)

    fun saveFocusTimeForRank(seconds: Int) {
        prefs.edit().putInt(KEY_FOCUS_TIME_RANK, seconds.coerceAtLeast(0)).apply()
    }

    fun getTrainingMissions(): List<TrainingMission> {
        val jsonStr = prefs.getString(KEY_MISSIONS, null) ?: return DEFAULT_MISSIONS
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TrainingMission>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TrainingMission(
                        id = obj.getInt("id"),
                        week = obj.getString("week"),
                        title = obj.getString("title"),
                        completed = obj.optBoolean("completed", false),
                        top15Achieved = obj.optBoolean("top15Achieved", false),
                        reward = obj.optInt("reward", 10)
                    )
                )
            }
            if (list.isEmpty()) DEFAULT_MISSIONS else list
        } catch (e: Exception) {
            DEFAULT_MISSIONS
        }
    }

    fun saveTrainingMissions(missions: List<TrainingMission>) {
        try {
            val array = JSONArray()
            for (m in missions) {
                val obj = JSONObject().apply {
                    put("id", m.id)
                    put("week", m.week)
                    put("title", m.title)
                    put("completed", m.completed)
                    put("top15Achieved", m.top15Achieved)
                    put("reward", m.reward)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_MISSIONS, array.toString()).apply()
        } catch (e: Exception) {
            // fallback
        }
    }

    fun getLastMatchResult(): SoloMatchResult? {
        val jsonStr = prefs.getString(KEY_LAST_MATCH, null) ?: return null
        return try {
            val obj = JSONObject(jsonStr)
            SoloMatchResult(
                playerRoll = obj.getInt("playerRoll"),
                enemyRoll = obj.getInt("enemyRoll"),
                won = obj.getBoolean("won"),
                pointsDiff = obj.getInt("pointsDiff"),
                timestamp = obj.optLong("timestamp", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveLastMatchResult(result: SoloMatchResult?) {
        if (result == null) {
            prefs.edit().remove(KEY_LAST_MATCH).apply()
            return
        }
        try {
            val obj = JSONObject().apply {
                put("playerRoll", result.playerRoll)
                put("enemyRoll", result.enemyRoll)
                put("won", result.won)
                put("pointsDiff", result.pointsDiff)
                put("timestamp", result.timestamp)
            }
            prefs.edit().putString(KEY_LAST_MATCH, obj.toString()).apply()
        } catch (e: Exception) {
            // fallback
        }
    }

    fun getCustomTriggerSet(): AgentTriggerSet? {
        val jsonStr = prefs.getString(KEY_CUSTOM_TRIGGER_SET, null) ?: return null
        return try {
            val root = JSONObject(jsonStr)
            val title = root.getString("title")
            val role = root.getString("tacticalRole")
            val mainArray = root.getJSONArray("mainTriggers")
            val subArray = root.getJSONArray("subTriggers")

            val mainList = mutableListOf<TriggerSlot>()
            for (i in 0 until mainArray.length()) {
                val item = mainArray.getJSONObject(i)
                mainList.add(
                    TriggerSlot(
                        slotNumber = item.getInt("slotNumber"),
                        name = item.getString("name"),
                        type = item.getString("type"),
                        description = item.getString("description"),
                        tag = item.getString("tag")
                    )
                )
            }

            val subList = mutableListOf<TriggerSlot>()
            for (i in 0 until subArray.length()) {
                val item = subArray.getJSONObject(i)
                subList.add(
                    TriggerSlot(
                        slotNumber = item.getInt("slotNumber"),
                        name = item.getString("name"),
                        type = item.getString("type"),
                        description = item.getString("description"),
                        tag = item.getString("tag")
                    )
                )
            }

            AgentTriggerSet(
                title = title,
                mainTriggers = mainList,
                subTriggers = subList,
                tacticalRole = role
            )
        } catch (e: Exception) {
            null
        }
    }

    fun saveCustomTriggerSet(triggerSet: AgentTriggerSet?) {
        if (triggerSet == null) {
            prefs.edit().remove(KEY_CUSTOM_TRIGGER_SET).apply()
            return
        }
        try {
            val root = JSONObject().apply {
                put("title", triggerSet.title)
                put("tacticalRole", triggerSet.tacticalRole)

                val mainArray = JSONArray()
                for (s in triggerSet.mainTriggers) {
                    val item = JSONObject().apply {
                        put("slotNumber", s.slotNumber)
                        put("name", s.name)
                        put("type", s.type)
                        put("description", s.description)
                        put("tag", s.tag)
                    }
                    mainArray.put(item)
                }
                put("mainTriggers", mainArray)

                val subArray = JSONArray()
                for (s in triggerSet.subTriggers) {
                    val item = JSONObject().apply {
                        put("slotNumber", s.slotNumber)
                        put("name", s.name)
                        put("type", s.type)
                        put("description", s.description)
                        put("tag", s.tag)
                    }
                    subArray.put(item)
                }
                put("subTriggers", subArray)
            }
            prefs.edit().putString(KEY_CUSTOM_TRIGGER_SET, root.toString()).apply()
        } catch (e: Exception) {
            // fallback
        }
    }

    // --- Solo Rank War Daily Mission Reminder Settings ---
    fun isDailyMissionReminderEnabled(): Boolean {
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun setDailyMissionReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }

    fun getDailyMissionReminderTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_REMINDER_HOUR, 20) // Default 20:00 (8:00 PM)
        val minute = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setDailyMissionReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_REMINDER_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    // --- Multi-Pane Code Editor Source File Operations ---
    suspend fun ensureDefaultSourceFiles() {
        val count = sourceFileDao.getFilesCount()
        if (count == 0) {
            val defaults = DefaultSourceFiles.getInitialProjectFiles()
            sourceFileDao.insertAll(defaults)
        }
    }

    suspend fun getSourceFileById(id: Long): com.example.data.local.entity.SourceFileEntity? =
        sourceFileDao.getFileById(id)

    suspend fun saveSourceFile(file: com.example.data.local.entity.SourceFileEntity) {
        sourceFileDao.updateFile(file)
    }

    suspend fun updateSourceFileContent(id: Long, newContent: String) {
        val existing = sourceFileDao.getFileById(id) ?: return
        val isModified = newContent != existing.originalContent
        val updated = existing.copy(
            content = newContent,
            isModified = isModified,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
        sourceFileDao.updateFile(updated)
    }

    suspend fun createSourceFile(
        path: String,
        name: String,
        extension: String,
        parentDir: String,
        content: String
    ): com.example.data.local.entity.SourceFileEntity {
        val cleanPath = if (parentDir.isNotBlank()) "${parentDir.trimEnd('/')}/$name" else name
        val newFile = com.example.data.local.entity.SourceFileEntity(
            filePath = cleanPath,
            fileName = name,
            fileExtension = extension.removePrefix(".").lowercase(),
            parentDir = parentDir,
            content = content,
            originalContent = content,
            isModified = false,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
        val id = sourceFileDao.insertFile(newFile)
        return newFile.copy(id = id)
    }

    suspend fun deleteSourceFile(id: Long) {
        sourceFileDao.deleteFileById(id)
    }

    suspend fun revertSourceFile(id: Long) {
        val existing = sourceFileDao.getFileById(id) ?: return
        val reverted = existing.copy(
            content = existing.originalContent,
            isModified = false,
            lastModifiedTimestamp = System.currentTimeMillis()
        )
        sourceFileDao.updateFile(reverted)
    }

    suspend fun resetAllSourceFilesToDefault() {
        sourceFileDao.deleteAll()
        val defaults = DefaultSourceFiles.getInitialProjectFiles()
        sourceFileDao.insertAll(defaults)
    }
}
