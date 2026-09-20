package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AgentRepository
import com.example.data.CloudAgentRank
import com.example.data.RankCloudSyncService
import com.example.data.local.entity.DailySoloRankMissionEntity
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.data.local.entity.SourceFileEntity
import com.example.data.local.entity.StudyTaskEntity
import com.example.util.DailyMissionNotificationManager
import com.example.util.MissionRewardSoundManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.model.AVAILABLE_POSITIONS
import com.example.model.AgentProfile
import com.example.model.AgentRank
import com.example.model.AgentTriggerSet
import com.example.model.DEFAULT_PARAMETERS
import com.example.model.LeaderboardAgent
import com.example.model.ParameterItem
import com.example.model.PomodoroPhase
import com.example.model.PomodoroSessionLog
import com.example.model.PomodoroState
import com.example.model.SoloMatchResult
import com.example.model.SoloRankHistoryStats
import com.example.model.TrainingMission
import com.example.model.TriggerDefinition
import com.example.model.TriggerSlot
import com.example.model.computeHistoryStats
import com.example.model.generateLeaderboardWithUser
import com.example.model.getParametersForPosition
import com.example.model.getRankForPoints
import com.example.model.getTriggerSetForPosition
import com.example.model.toTriggerSlot
import com.example.util.BailOutSoundManager
import com.example.util.TimerSoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ui.components.codeeditor.CodeDiagnostic
import com.example.ui.components.codeeditor.CodeDiagnosticsService
import com.example.ui.components.codeeditor.CodeFormattingService
import kotlin.random.Random

data class AgentUiState(
    val profile: AgentProfile = AgentProfile(),
    val isProfileRegistered: Boolean = false,
    val showEditProfileDialog: Boolean = false,
    val agentName: String = "Belén Salinas",
    val agentAge: Int = 15,
    val triggerType: String = "Ibis (Sniper)",
    val unitName: String = "BORDER DEFENSE AGENCY • SNIPER UNIT",
    val trionPoints: Int = 1000,
    val focusTimeForRank: Int = 0,
    val rankUnlocked: Boolean = false,
    val timerMode: String = "pomodoro", // "pomodoro", "countdown", "stopwatch"
    val timerSeconds: Int = 3600,
    val isRunning: Boolean = false,
    val isTimerSoundEnabled: Boolean = true,
    val isBailOutHapticsEnabled: Boolean = true,
    val pomodoroState: PomodoroState = PomodoroState(),
    val studyTasks: List<StudyTaskEntity> = emptyList(),
    val activeStudyTaskId: Long? = null,
    val taskTagFilter: String = "",
    val trainingMissions: List<TrainingMission> = emptyList(),
    val matchResult: SoloMatchResult? = null,
    val isRolling: Boolean = false,
    val parameters: List<ParameterItem> = DEFAULT_PARAMETERS,
    val triggerSet: AgentTriggerSet = getTriggerSetForPosition("Sniper", "Ibis (Sniper)"),
    val matchHistory: List<SoloRankMatchEntity> = emptyList(),
    val historyStats: SoloRankHistoryStats = SoloRankHistoryStats(),
    val leaderboard: List<LeaderboardAgent> = emptyList(),
    val soloRankTab: Int = 0, // 0: Đấu Rank, 1: Bảng Xếp Hạng, 2: Lịch Sử
    val historyFilter: String = "ALL", // "ALL", "WIN", "LOSS"
    val toastMessage: String? = null,
    val showTriggerCustomizerDialog: Boolean = false,
    val showCompletedDialog: Boolean = false,
    val completedDialogTitle: String = "",
    val completedDialogBody: String = "",
    val isSyncingRankToCloud: Boolean = false,
    val cloudSyncStatus: String? = null,
    val dailySoloRankMissions: List<DailySoloRankMissionEntity> = emptyList(),
    val completedMissionHistory: List<DailySoloRankMissionEntity> = emptyList(),
    val allMissionsHistory: List<DailySoloRankMissionEntity> = emptyList(),
    val isDailyMissionReminderEnabled: Boolean = true,
    val dailyMissionReminderHour: Int = 20,
    val dailyMissionReminderMinute: Int = 0,
    // Multi-Pane Code Editor State
    val sourceFiles: List<SourceFileEntity> = emptyList(),
    val openFileIds: List<Long> = emptyList(),
    val activeSourceFileId: Long? = null,
    val activeFileEditorContent: String = "",
    val isExplorerPaneVisible: Boolean = true,
    val isOutputPaneVisible: Boolean = false,
    val editorSearchQuery: String = "",
    val editorFontSizeSp: Float = 13.5f,
    val isLineWrapEnabled: Boolean = false,
    val simulatedBuildLogs: List<String> = emptyList(),
    val syntaxErrors: List<String> = emptyList(),
    val codeDiagnostics: List<CodeDiagnostic> = emptyList(),
    val isBuildingCode: Boolean = false,
    val activeOutputTab: Int = 0, // 0: Linter/Diagnostics, 1: Build Output, 2: Diff / History
    val isCommandPaletteVisible: Boolean = false
) {
    val currentRank: AgentRank
        get() = getRankForPoints(trionPoints)

    val canCustomizeTriggers: Boolean
        get() = trionPoints >= 4000 // Only Rank B and up (minPoints 4000) can customize

    val activeSourceFile: SourceFileEntity?
        get() = sourceFiles.find { it.id == activeSourceFileId } ?: sourceFiles.firstOrNull()

    val openSourceFiles: List<SourceFileEntity>
        get() = openFileIds.mapNotNull { id -> sourceFiles.find { it.id == id } }
}

class AgentViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AgentRepository(application.applicationContext)
    private val soundManager = TimerSoundManager(application.applicationContext)
    private val missionRewardSoundManager = MissionRewardSoundManager(application.applicationContext)
    private val bailOutSoundManager = BailOutSoundManager(application.applicationContext)
    private val rankCloudSyncService = RankCloudSyncService()

    private val _uiState = MutableStateFlow(AgentUiState())
    val uiState: StateFlow<AgentUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadData()
        observeMatchHistory()
        observeStudyTasks()
        observeDailyMissions()
        observeSourceFiles()
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun observeDailyMissions() {
        val today = getTodayDateString()
        viewModelScope.launch {
            repository.seedDefaultDailyMissionsIfEmpty(today)
            repository.getDailyMissions(today).collect { missions ->
                _uiState.update { it.copy(dailySoloRankMissions = missions) }
            }
        }
        viewModelScope.launch {
            repository.completedDailyMissions.collect { completedList ->
                _uiState.update { it.copy(completedMissionHistory = completedList) }
            }
        }
        viewModelScope.launch {
            repository.allDailyMissions.collect { allList ->
                _uiState.update { it.copy(allMissionsHistory = allList) }
            }
        }
    }

    private fun observeStudyTasks() {
        viewModelScope.launch {
            repository.allStudyTasks.collect { tasks ->
                if (tasks.isEmpty() && !repository.hasSeededStudyTasks()) {
                    repository.markSeededStudyTasks()
                    seedInitialStudyTasks()
                } else {
                    _uiState.update { current ->
                        val currentActiveId = current.activeStudyTaskId
                        val activeId = if (currentActiveId == null || tasks.none { it.id == currentActiveId }) {
                            tasks.firstOrNull { !it.isCompleted }?.id
                        } else {
                            currentActiveId
                        }
                        val activeTask = tasks.find { it.id == activeId }
                        val subjectName = activeTask?.title ?: current.pomodoroState.selectedSubject
                        current.copy(
                            studyTasks = tasks,
                            activeStudyTaskId = activeId,
                            pomodoroState = current.pomodoroState.copy(selectedSubject = subjectName)
                        )
                    }
                }
            }
        }
    }

    private fun seedInitialStudyTasks() {
        viewModelScope.launch {
            val task1 = StudyTaskEntity(
                title = "Lập trình Room DAO & Flows reactive",
                category = "Coding",
                tags = "Coding, Android, Architecture",
                targetPomodoros = 2,
                completedPomodoros = 1,
                priority = "HIGH",
                notes = "Xây dựng truy vấn Room có lọc theo tag và danh mục"
            )
            val task2 = StudyTaskEntity(
                title = "Đọc tài liệu cơ chế Trigger & Trion Horns",
                category = "Reading",
                tags = "Reading, Docs, Border Specs",
                targetPomodoros = 3,
                completedPomodoros = 0,
                priority = "MEDIUM",
                notes = "Nghiên cứu nguyên lý kích hoạt Trigger Tamakoma"
            )
            val task3 = StudyTaskEntity(
                title = "Học 30 từ vựng radar & trigger tiếng Anh",
                category = "Ngoại ngữ",
                tags = "Ngoại ngữ, Vocabulary, English",
                targetPomodoros = 2,
                completedPomodoros = 0,
                priority = "MEDIUM",
                notes = "Từ vựng chuyên ngành Border Defense"
            )
            val task4 = StudyTaskEntity(
                title = "Phân tích chiến thuật B-Rank Tamakoma-2",
                category = "Chiến thuật",
                tags = "Chiến thuật, Strategy, Combat",
                targetPomodoros = 3,
                completedPomodoros = 1,
                priority = "HIGH",
                notes = "Nghiên cứu phối hợp đội hình Sniper + Attacker"
            )
            val id1 = repository.insertStudyTask(task1)
            repository.insertStudyTask(task2)
            repository.insertStudyTask(task3)
            repository.insertStudyTask(task4)
            setActiveStudyTask(id1)
        }
    }

    private fun observeMatchHistory() {
        viewModelScope.launch {
            repository.allMatches.collect { matches ->
                val stats = computeHistoryStats(matches)
                val winRateStr = "${stats.winRatePercent}%"
                val board = generateLeaderboardWithUser(
                    userName = _uiState.value.agentName,
                    userPoints = _uiState.value.trionPoints,
                    userWinRate = winRateStr
                )
                _uiState.update {
                    it.copy(
                        matchHistory = matches,
                        historyStats = stats,
                        leaderboard = board
                    )
                }
            }
        }
    }

    private fun loadData() {
        val profile = repository.getAgentProfile()
        val isRegistered = repository.isProfileRegistered()
        val points = repository.getTrionPoints()
        val trigger = repository.getTriggerType()
        val focusTime = repository.getFocusTimeForRank()
        val missions = repository.getTrainingMissions()
        val lastMatch = repository.getLastMatchResult()
        val parameters = getParametersForPosition(profile.position)
        val savedCustomSet = repository.getCustomTriggerSet()
        val triggerSet = savedCustomSet ?: getTriggerSetForPosition(profile.position, trigger)
        val pomodoroTotal = repository.getPomodoroTotalCount()
        val pomodoroLogs = repository.getPomodoroLogs()
        val soundEnabled = repository.isTimerSoundEnabled()
        val reminderEnabled = repository.isDailyMissionReminderEnabled()
        val (reminderHour, reminderMinute) = repository.getDailyMissionReminderTime()

        // Ensure alarm is scheduled if enabled
        if (reminderEnabled) {
            DailyMissionNotificationManager.createNotificationChannel(getApplication())
            DailyMissionNotificationManager.scheduleDailyReminder(
                getApplication(),
                reminderHour,
                reminderMinute
            )
        }

        _uiState.update {
            it.copy(
                profile = profile,
                isProfileRegistered = isRegistered,
                trionPoints = points,
                agentName = profile.name,
                agentAge = profile.age,
                triggerType = trigger,
                unitName = "BORDER DEFENSE AGENCY • ${profile.position.uppercase()} UNIT",
                focusTimeForRank = focusTime,
                rankUnlocked = focusTime >= 1800,
                isTimerSoundEnabled = soundEnabled,
                isDailyMissionReminderEnabled = reminderEnabled,
                dailyMissionReminderHour = reminderHour,
                dailyMissionReminderMinute = reminderMinute,
                trainingMissions = missions,
                matchResult = lastMatch,
                parameters = parameters,
                triggerSet = triggerSet,
                pomodoroState = it.pomodoroState.copy(
                    totalCompletedPomodoros = pomodoroTotal,
                    recentLogs = pomodoroLogs
                )
            )
        }
    }

    fun registerProfile(profile: AgentProfile) {
        val updatedProfile = profile.copy(isRegistered = true)
        repository.saveAgentProfile(updatedProfile)

        val newTrigger = AVAILABLE_POSITIONS.firstOrNull {
            it.title.equals(profile.position, ignoreCase = true)
        }?.defaultTrigger ?: "Ibis (Sniper)"
        repository.saveTriggerType(newTrigger)
        val parameters = getParametersForPosition(profile.position)
        val savedCustomSet = repository.getCustomTriggerSet()
        val triggerSet = savedCustomSet ?: getTriggerSetForPosition(profile.position, newTrigger)

        _uiState.update {
            it.copy(
                profile = updatedProfile,
                isProfileRegistered = true,
                agentName = profile.name,
                agentAge = profile.age,
                triggerType = newTrigger,
                unitName = "BORDER DEFENSE AGENCY • ${profile.position.uppercase()} UNIT",
                parameters = parameters,
                triggerSet = triggerSet,
                showEditProfileDialog = false,
                toastMessage = "Hồ sơ đặc vụ ${profile.name} đã được cập nhật thành công!"
            )
        }

        val stats = _uiState.value.historyStats
        val board = generateLeaderboardWithUser(
            userName = profile.name,
            userPoints = _uiState.value.trionPoints,
            userWinRate = "${stats.winRatePercent}%"
        )
        _uiState.update { it.copy(leaderboard = board) }
    }

    fun updateProfilePicture(uriString: String?) {
        val updatedProfile = _uiState.value.profile.copy(pictureUri = uriString)
        repository.saveAgentProfile(updatedProfile)
        _uiState.update {
            it.copy(
                profile = updatedProfile,
                toastMessage = if (uriString != null) "Đã cập nhật ảnh đại diện đặc vụ!" else "Đã xóa ảnh đại diện!"
            )
        }
    }

    fun setShowEditProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showEditProfileDialog = show) }
    }

    fun setTimerMode(mode: String) {
        if (_uiState.value.isRunning) return
        _uiState.update {
            it.copy(
                timerMode = mode,
                timerSeconds = if (mode == "countdown") 3600 else 0
            )
        }
    }

    fun toggleTimer() {
        val currentlyRunning = _uiState.value.isRunning
        if (currentlyRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val state = _uiState.value
                if (!state.isRunning) break

                val isCountdown = state.timerMode == "countdown"
                val nextSeconds = if (isCountdown) state.timerSeconds - 1 else state.timerSeconds + 1

                val newFocusTime = if (state.focusTimeForRank < 1800) {
                    val updated = state.focusTimeForRank + 1
                    repository.saveFocusTimeForRank(updated)
                    updated
                } else {
                    state.focusTimeForRank
                }

                if (isCountdown && nextSeconds <= 0) {
                    // Completed 1 hour countdown: trigger Bail Out notification sound & tactile haptics
                    bailOutSoundManager.triggerBailOut(
                        soundEnabled = _uiState.value.isTimerSoundEnabled,
                        hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
                    )
                    val updatedPoints = state.trionPoints + 6
                    repository.saveTrionPoints(updatedPoints)
                    _uiState.update {
                        it.copy(
                            isRunning = false,
                            timerSeconds = 3600,
                            trionPoints = updatedPoints,
                            focusTimeForRank = newFocusTime,
                            rankUnlocked = newFocusTime >= 1800,
                            showCompletedDialog = true,
                            completedDialogTitle = "HOÀN THÀNH 1H TẬP TRUNG!",
                            completedDialogBody = "Bạn đã hoàn thành 1 giờ huấn luyện tập trung và nhận được +6 PTS Trion."
                        )
                    }
                    break
                } else {
                    _uiState.update {
                        it.copy(
                            timerSeconds = nextSeconds,
                            focusTimeForRank = newFocusTime,
                            rankUnlocked = newFocusTime >= 1800
                        )
                    }
                }
            }
        }
    }

    fun pauseTimer() {
        _uiState.update { it.copy(isRunning = false) }
        timerJob?.cancel()
        timerJob = null
    }

    fun resetCountdown() {
        pauseTimer()
        _uiState.update { it.copy(timerSeconds = 3600) }
    }

    // --- Pomodoro Focus Timer Operations ---

    fun togglePomodoroTimer() {
        val currentRunning = _uiState.value.pomodoroState.isRunning
        if (currentRunning) {
            pausePomodoroTimer()
        } else {
            startPomodoroTimer()
        }
    }

    private var pomodoroJob: Job? = null

    private fun startPomodoroTimer() {
        _uiState.update {
            it.copy(pomodoroState = it.pomodoroState.copy(isRunning = true))
        }
        pomodoroJob?.cancel()
        pomodoroJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val currentPomodoro = _uiState.value.pomodoroState
                if (!currentPomodoro.isRunning) break

                val nextRemaining = currentPomodoro.remainingSeconds - 1

                // Accumulate focus time towards Rank unlock if in FOCUS phase
                val newFocusTime = if (currentPomodoro.phase == PomodoroPhase.FOCUS && _uiState.value.focusTimeForRank < 1800) {
                    val updated = _uiState.value.focusTimeForRank + 1
                    repository.saveFocusTimeForRank(updated)
                    updated
                } else {
                    _uiState.value.focusTimeForRank
                }

                if (nextRemaining <= 0) {
                    // Current phase completed
                    handlePomodoroPhaseCompleted(currentPomodoro, newFocusTime)
                    break
                } else {
                    _uiState.update {
                        it.copy(
                            focusTimeForRank = newFocusTime,
                            rankUnlocked = newFocusTime >= 1800,
                            pomodoroState = currentPomodoro.copy(remainingSeconds = nextRemaining)
                        )
                    }
                }
            }
        }
    }

    fun pausePomodoroTimer() {
        _uiState.update {
            it.copy(pomodoroState = it.pomodoroState.copy(isRunning = false))
        }
        pomodoroJob?.cancel()
        pomodoroJob = null
    }

    fun resetPomodoroTimer() {
        pausePomodoroTimer()
        _uiState.update {
            val phase = it.pomodoroState.phase
            val defaultSecs = when (phase) {
                PomodoroPhase.FOCUS -> it.pomodoroState.focusWorkDurationMinutes * 60
                PomodoroPhase.SHORT_BREAK -> it.pomodoroState.shortBreakDurationMinutes * 60
                PomodoroPhase.LONG_BREAK -> it.pomodoroState.longBreakDurationMinutes * 60
            }
            it.copy(
                pomodoroState = it.pomodoroState.copy(
                    remainingSeconds = defaultSecs,
                    totalSecondsForPhase = defaultSecs,
                    isRunning = false
                )
            )
        }
    }

    fun skipPomodoroPhase() {
        pausePomodoroTimer()
        val current = _uiState.value.pomodoroState
        when (current.phase) {
            PomodoroPhase.FOCUS -> {
                // If skipping focus, advance to break
                val nextPhase = if (current.currentCycle % 4 == 0) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                setPomodoroPhase(nextPhase)
            }
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK -> {
                val nextCycle = if (current.phase == PomodoroPhase.LONG_BREAK) 1 else current.currentCycle + 1
                _uiState.update {
                    it.copy(pomodoroState = it.pomodoroState.copy(currentCycle = nextCycle))
                }
                setPomodoroPhase(PomodoroPhase.FOCUS)
            }
        }
    }

    fun setPomodoroPhase(phase: PomodoroPhase) {
        pausePomodoroTimer()
        _uiState.update {
            val defaultSecs = when (phase) {
                PomodoroPhase.FOCUS -> it.pomodoroState.focusWorkDurationMinutes * 60
                PomodoroPhase.SHORT_BREAK -> it.pomodoroState.shortBreakDurationMinutes * 60
                PomodoroPhase.LONG_BREAK -> it.pomodoroState.longBreakDurationMinutes * 60
            }
            it.copy(
                pomodoroState = it.pomodoroState.copy(
                    phase = phase,
                    remainingSeconds = defaultSecs,
                    totalSecondsForPhase = defaultSecs,
                    isRunning = false
                )
            )
        }
    }

    fun setPomodoroSubject(subject: String) {
        _uiState.update {
            it.copy(pomodoroState = it.pomodoroState.copy(selectedSubject = subject))
        }
    }

    // --- Study Task Management ---

    fun setActiveStudyTask(taskId: Long?) {
        _uiState.update { it.copy(activeStudyTaskId = taskId) }
        val task = _uiState.value.studyTasks.find { it.id == taskId }
        if (task != null) {
            setPomodoroSubject(task.title)
        }
    }

    fun setTaskTagFilter(tag: String) {
        _uiState.update { it.copy(taskTagFilter = tag) }
    }

    fun addStudyTask(
        title: String,
        category: String = "Coding",
        tags: String = "",
        targetPomodoros: Int = 1,
        priority: String = "MEDIUM",
        notes: String = ""
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = StudyTaskEntity(
                title = title.trim(),
                category = category.trim().ifBlank { "Coding" },
                tags = tags.trim(),
                targetPomodoros = targetPomodoros.coerceAtLeast(1),
                priority = priority,
                notes = notes.trim()
            )
            val newId = repository.insertStudyTask(task)
            // Auto select as active if no active task
            if (_uiState.value.activeStudyTaskId == null) {
                setActiveStudyTask(newId)
            }
        }
    }

    fun toggleStudyTask(taskId: Long) {
        val task = _uiState.value.studyTasks.find { it.id == taskId } ?: return
        val newStatus = !task.isCompleted
        viewModelScope.launch {
            repository.toggleStudyTaskCompletion(taskId, newStatus)
            if (newStatus && _uiState.value.activeStudyTaskId == taskId) {
                _uiState.update { it.copy(activeStudyTaskId = null) }
            }
        }
    }

    fun deleteStudyTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteStudyTaskById(taskId)
            if (_uiState.value.activeStudyTaskId == taskId) {
                _uiState.update { it.copy(activeStudyTaskId = null) }
            }
        }
    }

    private fun handlePomodoroPhaseCompleted(current: PomodoroState, focusTime: Int) {
        when (current.phase) {
            PomodoroPhase.FOCUS -> {
                // Trigger subtle 'Bail Out' notification sound and tactile haptic feedback
                bailOutSoundManager.triggerBailOut(
                    soundEnabled = _uiState.value.isTimerSoundEnabled,
                    hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
                )

                // Reward Trion points (+2 PTS per 25 min pomodoro)
                val earnedPts = 2
                val updatedPoints = _uiState.value.trionPoints + earnedPts
                repository.saveTrionPoints(updatedPoints)

                val newTotalCount = current.totalCompletedPomodoros + 1
                repository.savePomodoroTotalCount(newTotalCount)

                // If an active study task is being tracked, increment its pomodoro counter
                val activeTaskId = _uiState.value.activeStudyTaskId
                val activeTask = _uiState.value.studyTasks.find { it.id == activeTaskId }
                val subjectTitle = activeTask?.title ?: current.selectedSubject

                if (activeTaskId != null) {
                    viewModelScope.launch {
                        repository.incrementTaskPomodoroCount(activeTaskId)
                    }
                }

                val newLog = PomodoroSessionLog(
                    subject = subjectTitle,
                    durationMinutes = current.focusWorkDurationMinutes,
                    cycleNumber = current.currentCycle,
                    earnedPoints = earnedPts
                )
                val updatedLogs = listOf(newLog) + current.recentLogs
                repository.savePomodoroLogs(updatedLogs)

                // Track daily focus mission progress
                autoProgressFocusMission(current.focusWorkDurationMinutes)

                // Next phase: check if cycle == 4 for long break
                val isLongBreak = current.currentCycle >= 4
                val nextPhase = if (isLongBreak) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
                val nextDuration = if (isLongBreak) current.longBreakDurationMinutes * 60 else current.shortBreakDurationMinutes * 60

                val completionMessage = if (activeTask != null) {
                    val newCompleted = activeTask.completedPomodoros + 1
                    if (newCompleted >= activeTask.targetPomodoros) {
                        "Chiến công xuất sắc! Bạn đã hoàn thành 25 phút tập trung và ĐẠT CHỈ TIÊU (${newCompleted}/${activeTask.targetPomodoros} Pomodoro) cho nhiệm vụ '${activeTask.title}'. Dữ liệu đã lưu vào Room database."
                    } else {
                        "Chúc mừng! Bạn đã hoàn thành 25 phút tập trung cho nhiệm vụ '${activeTask.title}'. Tiến độ Room: ${newCompleted}/${activeTask.targetPomodoros} Pomodoro. Hãy bắt đầu phiên nghỉ giải lao."
                    }
                } else {
                    "Chúc mừng! Bạn đã hoàn thành 25 phút học tập trung chuyên sâu cho mục tiêu '$subjectTitle'. Hãy bắt đầu phiên nghỉ giải lao."
                }

                _uiState.update {
                    it.copy(
                        trionPoints = updatedPoints,
                        focusTimeForRank = focusTime,
                        rankUnlocked = focusTime >= 1800,
                        showCompletedDialog = true,
                        completedDialogTitle = "HOÀN THÀNH POMODORO! (+${earnedPts} PTS)",
                        completedDialogBody = completionMessage,
                        pomodoroState = current.copy(
                            isRunning = false,
                            phase = nextPhase,
                            remainingSeconds = nextDuration,
                            totalSecondsForPhase = nextDuration,
                            totalCompletedPomodoros = newTotalCount,
                            recentLogs = updatedLogs
                        )
                    )
                }
            }
            PomodoroPhase.SHORT_BREAK -> {
                if (_uiState.value.isTimerSoundEnabled) {
                    soundManager.playFocusSessionEndSound()
                }
                val nextCycle = current.currentCycle + 1
                val nextDuration = current.focusWorkDurationMinutes * 60
                _uiState.update {
                    it.copy(
                        showCompletedDialog = true,
                        completedDialogTitle = "KẾT THÚC NGHỈ NGẮN",
                        completedDialogBody = "Hết 5 phút nghỉ! Sẵn sàng tinh thần bước vào chu kỳ học tập tiếp theo (#$nextCycle/4).",
                        pomodoroState = current.copy(
                            isRunning = false,
                            phase = PomodoroPhase.FOCUS,
                            currentCycle = nextCycle,
                            remainingSeconds = nextDuration,
                            totalSecondsForPhase = nextDuration
                        )
                    )
                }
            }
            PomodoroPhase.LONG_BREAK -> {
                if (_uiState.value.isTimerSoundEnabled) {
                    soundManager.playFocusSessionEndSound()
                }
                // Long break finished -> cycle reset to 1
                val nextDuration = current.focusWorkDurationMinutes * 60
                _uiState.update {
                    it.copy(
                        showCompletedDialog = true,
                        completedDialogTitle = "HOÀN TẤT CHU TRÌNH POMODORO!",
                        completedDialogBody = "Bạn đã hoàn thành trọn vẹn chuỗi 4 chu kỳ Pomodoro và hồi phục năng lượng với phiên nghỉ dài 15 phút. Tuyệt vời!",
                        pomodoroState = current.copy(
                            isRunning = false,
                            phase = PomodoroPhase.FOCUS,
                            currentCycle = 1,
                            remainingSeconds = nextDuration,
                            totalSecondsForPhase = nextDuration
                        )
                    )
                }
            }
        }
    }

    fun finishStopwatch() {
        pauseTimer()
        val seconds = _uiState.value.timerSeconds
        val earnedPts = seconds / 600 // 10 minutes = 1 pt
        if (earnedPts > 0) {
            bailOutSoundManager.triggerBailOut(
                soundEnabled = _uiState.value.isTimerSoundEnabled,
                hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
            )
            val updatedPoints = _uiState.value.trionPoints + earnedPts
            repository.saveTrionPoints(updatedPoints)
            _uiState.update {
                it.copy(
                    timerSeconds = 0,
                    trionPoints = updatedPoints,
                    showCompletedDialog = true,
                    completedDialogTitle = "CHỐT SỔ HUẤN LUYỆN TỰ DO",
                    completedDialogBody = "Đã kết thúc phiên huấn luyện! Bạn đạt ${seconds / 60} phút và nhận được +$earnedPts PTS."
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    timerSeconds = 0,
                    showCompletedDialog = true,
                    completedDialogTitle = "CHƯA ĐỦ ĐIỀU KIỆN ĐIỂM",
                    completedDialogBody = "Phiên huấn luyện kết thúc nhưng chưa đủ tối thiểu 10 phút để nhận điểm (10 phút = 1 PTS)."
                )
            }
        }
    }

    fun completeTrainingMission(id: Int, desiredTop15: Boolean) {
        val currentMissions = _uiState.value.trainingMissions
        val mission = currentMissions.find { it.id == id } ?: return
        val wasAchieved = mission.top15Achieved

        var newPoints = _uiState.value.trionPoints
        if (desiredTop15 && !wasAchieved) {
            newPoints += mission.reward
        } else if (!desiredTop15 && wasAchieved) {
            newPoints = (newPoints - mission.reward).coerceAtLeast(0)
        }

        val updatedMissions = currentMissions.map { m ->
            if (m.id == id) {
                m.copy(completed = true, top15Achieved = desiredTop15)
            } else {
                m
            }
        }

        repository.saveTrionPoints(newPoints)
        repository.saveTrainingMissions(updatedMissions)

        _uiState.update {
            it.copy(
                trionPoints = newPoints,
                trainingMissions = updatedMissions
            )
        }
    }

    fun setSoloRankTab(tab: Int) {
        _uiState.update { it.copy(soloRankTab = tab) }
    }

    fun setHistoryFilter(filter: String) {
        _uiState.update { it.copy(historyFilter = filter) }
    }

    fun clearMatchHistory() {
        viewModelScope.launch {
            repository.clearMatchHistory()
        }
    }

    fun handleSoloRankMatch() {
        val state = _uiState.value
        if (!state.rankUnlocked || state.isRolling) return

        _uiState.update { it.copy(isRolling = true) }

        viewModelScope.launch {
            delay(1500L) // Dice rolling animation duration
            val playerRoll = Random.nextInt(1, 51)
            val enemyRoll = Random.nextInt(1, 51)
            val won = playerRoll > enemyRoll
            val pointsDiff = playerRoll

            val currentPoints = _uiState.value.trionPoints
            val newPoints = if (won) {
                currentPoints + pointsDiff
            } else {
                (currentPoints - pointsDiff).coerceAtLeast(0)
            }

            val opponents = listOf(
                Triple("Border Combat AI", "B-RANK", "Trion Drone Unit"),
                Triple("Shun Midorikawa", "B-RANK", "Dual Scorpion"),
                Triple("Ko Murakami", "A-RANK", "Raygust Heavy Shield"),
                Triple("Isami Toma", "A-RANK", "Egret Elevation Shot"),
                Triple("Ken Satori", "A-RANK", "Dual Egret Aerial Snipe"),
                Triple("Yuma Kuga", "B-RANK", "Scorpion Grasshopper"),
                Triple("Border Trainee Alpha", "C-RANK", "Ibis Trainee Squad"),
                Triple("Tatsuhito Ikoma", "C-RANK", "Senku Kogetsu Slash")
            )
            val opponent = opponents.random()

            val tactic = if (won) {
                listOf(
                    "Bắn tỉa Ibis xuyên phá giáp thành công",
                    "Kích hoạt Bagworm ẩn nấp và phản kích chính xác",
                    "Đoán trước quỹ đạo di chuyển của đối phương",
                    "Tận dụng lợi thế điểm cao trên nóc cao ốc",
                    "Dứt điểm bằng đạn chì Lead Bullet khống chế"
                ).random()
            } else {
                listOf(
                    "Bị đối thủ tiếp cận cận chiến quá nhanh",
                    "Lộ vị trí khi chưa kịp kích hoạt Bagworm",
                    "Trion Shield bị áp đảo bởi hỏa lực dồn dập",
                    "Đối phương đổi góc né tránh đường đạn Ibis",
                    "Lệch tâm ngắm do áp lực tấn công liên tục"
                ).random()
            }

            val result = SoloMatchResult(
                playerRoll = playerRoll,
                enemyRoll = enemyRoll,
                won = won,
                pointsDiff = pointsDiff
            )

            val matchEntity = SoloRankMatchEntity(
                agentName = state.agentName,
                opponentName = opponent.first,
                opponentRank = opponent.second,
                playerRoll = playerRoll,
                enemyRoll = enemyRoll,
                won = won,
                pointsDiff = pointsDiff,
                finalPoints = newPoints,
                tacticNote = tactic
            )

            repository.saveTrionPoints(newPoints)
            repository.saveLastMatchResult(result)
            repository.saveFocusTimeForRank(0)
            repository.insertMatchRecord(matchEntity)

            val updatedStats = computeHistoryStats(listOf(matchEntity) + state.matchHistory)
            val updatedBoard = generateLeaderboardWithUser(
                userName = state.agentName,
                userPoints = newPoints,
                userWinRate = "${updatedStats.winRatePercent}%"
            )

            _uiState.update {
                it.copy(
                    isRolling = false,
                    trionPoints = newPoints,
                    matchResult = result,
                    focusTimeForRank = 0,
                    rankUnlocked = false,
                    historyStats = updatedStats,
                    leaderboard = updatedBoard
                )
            }

            // Automatically sync updated rank and points to Cloud Firestore
            syncUserRankToCloud()

            // Update daily Solo Rank War missions progress
            autoProgressMissionsAfterMatch(won = won, isStreak = updatedStats.currentStreak >= 2)
        }
    }

    fun syncUserRankToCloud() {
        val state = _uiState.value
        _uiState.update { it.copy(isSyncingRankToCloud = true) }
        viewModelScope.launch {
            val result = rankCloudSyncService.syncUserRankData(
                profile = state.profile,
                points = state.trionPoints,
                matchesWon = state.historyStats.wins,
                totalMatches = state.historyStats.totalMatches
            )
            if (result.isSuccess) {
                val cloudRank = result.getOrNull()
                _uiState.update {
                    it.copy(
                        isSyncingRankToCloud = false,
                        cloudSyncStatus = "Đã đồng bộ Cloud: ${cloudRank?.points ?: state.trionPoints} PTS (${cloudRank?.rankTag ?: state.currentRank.tag})",
                        toastMessage = "Đồng bộ Rank lên Border Cloud (Firestore) thành công!"
                    )
                }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Lỗi kết nối Firestore"
                _uiState.update {
                    it.copy(
                        isSyncingRankToCloud = false,
                        cloudSyncStatus = "Lưu trữ nội bộ (Cloud offline)",
                        toastMessage = "Chưa kết nối Cloud Firestore: $errorMsg"
                    )
                }
            }
        }
    }

    fun openTriggerCustomizer() {
        if (!_uiState.value.canCustomizeTriggers) {
            _uiState.update {
                it.copy(
                    toastMessage = "CẢNH BÁO: Chỉ đặc vụ B-Rank trở lên (từ 4.000 PTS) mới được phép tùy biến Trigger Set tự do! C-Rank chỉ sử dụng bộ trang bị huấn luyện do Border cấp."
                )
            }
            return
        }
        _uiState.update { it.copy(showTriggerCustomizerDialog = true) }
    }

    fun closeTriggerCustomizer() {
        _uiState.update { it.copy(showTriggerCustomizerDialog = false) }
    }

    fun swapTriggerSlot(isMain: Boolean, slotNumber: Int, newTrigger: TriggerDefinition) {
        if (!_uiState.value.canCustomizeTriggers) {
            _uiState.update {
                it.copy(
                    toastMessage = "Chỉ đặc vụ B-Rank trở lên mới có quyền thay đổi Trigger!"
                )
            }
            return
        }

        val currentSet = _uiState.value.triggerSet
        val newSlot = newTrigger.toTriggerSlot(slotNumber)

        val updatedSet = if (isMain) {
            val updatedMain = currentSet.mainTriggers.map {
                if (it.slotNumber == slotNumber) newSlot else it
            }
            currentSet.copy(mainTriggers = updatedMain)
        } else {
            val updatedSub = currentSet.subTriggers.map {
                if (it.slotNumber == slotNumber) newSlot else it
            }
            currentSet.copy(subTriggers = updatedSub)
        }

        // Check if primary weapon changed (slot 1)
        val firstSlotTriggerName = updatedSet.mainTriggers.firstOrNull { it.slotNumber == 1 }?.name ?: _uiState.value.triggerType
        repository.saveCustomTriggerSet(updatedSet)
        repository.saveTriggerType(firstSlotTriggerName)

        _uiState.update {
            it.copy(
                triggerSet = updatedSet,
                triggerType = firstSlotTriggerName,
                toastMessage = "Đã trang bị [${newTrigger.name}] vào Slot #0$slotNumber (${if (isMain) "Main" else "Sub"})!"
            )
        }
    }

    fun resetTriggerSetToDefault() {
        repository.saveCustomTriggerSet(null)
        val defaultSet = getTriggerSetForPosition(_uiState.value.profile.position)
        val defaultTrigger = defaultSet.mainTriggers.firstOrNull()?.name ?: "Ibis (Sniper)"
        repository.saveTriggerType(defaultTrigger)

        _uiState.update {
            it.copy(
                triggerSet = defaultSet,
                triggerType = defaultTrigger,
                toastMessage = "Đã hoàn tác cấu hình Trigger Set về chuẩn vị trí ${_uiState.value.profile.position}!"
            )
        }
    }

    fun updateAgentInfo(name: String, age: Int, trigger: String) {
        repository.saveAgentName(name)
        repository.saveAgentAge(age)
        repository.saveTriggerType(trigger)
        _uiState.update {
            it.copy(
                agentName = name,
                agentAge = age,
                triggerType = trigger
            )
        }
    }

    fun dismissCompletedDialog() {
        _uiState.update { it.copy(showCompletedDialog = false) }
    }

    fun setTimerSoundEnabled(enabled: Boolean) {
        repository.saveTimerSoundEnabled(enabled)
        _uiState.update { it.copy(isTimerSoundEnabled = enabled) }
    }

    fun toggleTimerSound() {
        val next = !_uiState.value.isTimerSoundEnabled
        setTimerSoundEnabled(next)
    }

    fun toggleBailOutHaptics() {
        val next = !_uiState.value.isBailOutHapticsEnabled
        _uiState.update {
            it.copy(
                isBailOutHapticsEnabled = next,
                toastMessage = if (next) "Đã bật rung phản hồi Bail Out" else "Đã tắt rung phản hồi Bail Out"
            )
        }
    }

    fun playTestSound() {
        bailOutSoundManager.triggerBailOut(
            soundEnabled = _uiState.value.isTimerSoundEnabled,
            hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
        )
    }

    fun playTestBailOut() {
        bailOutSoundManager.triggerBailOut(
            soundEnabled = _uiState.value.isTimerSoundEnabled,
            hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
        )
        _uiState.update {
            it.copy(toastMessage = "⚡ BAIL OUT: Đã phát âm thanh & kích hoạt rung phản hồi chiến thuật!")
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    // --- Daily Solo Rank War Missions Operations ---

    fun toggleDailyMission(mission: DailySoloRankMissionEntity) {
        viewModelScope.launch {
            val willBeCompleted = !mission.isCompleted
            repository.toggleDailyMissionCompletion(mission.id, willBeCompleted)
            if (willBeCompleted) {
                // Trigger subtle Bail Out notification sound & tactical haptic feedback
                bailOutSoundManager.triggerBailOut(
                    soundEnabled = _uiState.value.isTimerSoundEnabled,
                    hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
                )
                val newPoints = _uiState.value.trionPoints + mission.rewardPoints
                repository.saveTrionPoints(newPoints)
                _uiState.update {
                    it.copy(
                        trionPoints = newPoints,
                        toastMessage = "⚡ BAIL OUT! Hoàn thành: ${mission.title} (+${mission.rewardPoints} PTS Trion)!"
                    )
                }
                syncUserRankToCloud()
            } else {
                val newPoints = (_uiState.value.trionPoints - mission.rewardPoints).coerceAtLeast(0)
                repository.saveTrionPoints(newPoints)
                _uiState.update {
                    it.copy(
                        trionPoints = newPoints,
                        toastMessage = "Thu hồi hoàn thành: -${mission.rewardPoints} PTS"
                    )
                }
                syncUserRankToCloud()
            }
        }
    }

    fun addDailyMission(
        title: String,
        description: String,
        targetCount: Int,
        rewardPoints: Int,
        category: String = "SOLO_WAR",
        customNotes: String = "",
        customTags: String = ""
    ) {
        val today = getTodayDateString()
        viewModelScope.launch {
            val mission = DailySoloRankMissionEntity(
                title = title.trim(),
                description = description.trim(),
                targetCount = targetCount.coerceAtLeast(1),
                currentProgress = 0,
                rewardPoints = rewardPoints.coerceAtLeast(50),
                isCompleted = false,
                dateString = today,
                missionCategory = category,
                customNotes = customNotes.trim(),
                customTags = customTags.trim()
            )
            repository.insertDailyMission(mission)
            _uiState.update { it.copy(toastMessage = "Đã thêm nhiệm vụ chiến đấu/học tập mới!") }
        }
    }

    fun updateMissionNotesAndTags(missionId: Long, notes: String, tags: String) {
        viewModelScope.launch {
            repository.updateDailyMissionNotesAndTags(missionId, notes, tags)
            _uiState.update { it.copy(toastMessage = "Đã lưu ghi chú và nhãn tag nhiệm vụ vào Room DB!") }
        }
    }

    fun deleteDailyMission(id: Long) {
        viewModelScope.launch {
            repository.deleteDailyMission(id)
            _uiState.update { it.copy(toastMessage = "Đã xóa nhiệm vụ.") }
        }
    }

    fun incrementDailyMission(id: Long, amount: Int = 1) {
        viewModelScope.launch {
            val completedNow = repository.incrementDailyMissionProgress(id, amount)
            if (completedNow) {
                val mission = _uiState.value.dailySoloRankMissions.find { it.id == id }
                if (mission != null) {
                    onMissionCompletedReward(mission)
                }
            }
        }
    }

    private fun autoProgressMissionsAfterMatch(won: Boolean, isStreak: Boolean) {
        val missions = _uiState.value.dailySoloRankMissions
        viewModelScope.launch {
            missions.forEach { mission ->
                if (!mission.isCompleted) {
                    when (mission.missionCategory) {
                        "SOLO_WAR" -> {
                            val completedNow = repository.incrementDailyMissionProgress(mission.id, 1)
                            if (completedNow) onMissionCompletedReward(mission)
                        }
                        "WIN_MATCH" -> {
                            if (won) {
                                val completedNow = repository.incrementDailyMissionProgress(mission.id, 1)
                                if (completedNow) onMissionCompletedReward(mission)
                            }
                        }
                        "COMBAT_STREAK" -> {
                            if (isStreak) {
                                val completedNow = repository.incrementDailyMissionProgress(mission.id, 1)
                                if (completedNow) onMissionCompletedReward(mission)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun autoProgressFocusMission(durationMinutes: Int) {
        val missions = _uiState.value.dailySoloRankMissions
        viewModelScope.launch {
            missions.forEach { mission ->
                if (!mission.isCompleted && mission.missionCategory == "FOCUS_TRAINING") {
                    val completedNow = repository.incrementDailyMissionProgress(mission.id, durationMinutes)
                    if (completedNow) onMissionCompletedReward(mission)
                }
            }
        }
    }

    private fun onMissionCompletedReward(mission: DailySoloRankMissionEntity) {
        // Trigger subtle Bail Out notification sound and tactile haptics
        bailOutSoundManager.triggerBailOut(
            soundEnabled = _uiState.value.isTimerSoundEnabled,
            hapticsEnabled = _uiState.value.isBailOutHapticsEnabled
        )
        val newPoints = _uiState.value.trionPoints + mission.rewardPoints
        repository.saveTrionPoints(newPoints)
        _uiState.update {
            it.copy(
                trionPoints = newPoints,
                toastMessage = "⚡ BAIL OUT! Nhiệm vụ tác chiến hoàn thành: ${mission.title} (+${mission.rewardPoints} PTS Trion)!"
            )
        }
        syncUserRankToCloud()
    }

    // --- Solo Rank War Daily Mission Reminder Controls ---
    fun toggleDailyMissionReminder(enabled: Boolean) {
        repository.setDailyMissionReminderEnabled(enabled)
        val app = getApplication<Application>()
        if (enabled) {
            val (hour, minute) = repository.getDailyMissionReminderTime()
            DailyMissionNotificationManager.createNotificationChannel(app)
            DailyMissionNotificationManager.scheduleDailyReminder(app, hour, minute)
        } else {
            DailyMissionNotificationManager.cancelDailyReminder(app)
        }

        _uiState.update {
            it.copy(
                isDailyMissionReminderEnabled = enabled,
                toastMessage = if (enabled) "Đã bật chuông nhắc nhở nhiệm vụ Solo Rank War" else "Đã tắt nhắc nhở nhiệm vụ Solo Rank War"
            )
        }
    }

    fun setDailyMissionReminderTime(hour: Int, minute: Int) {
        repository.setDailyMissionReminderTime(hour, minute)
        val app = getApplication<Application>()
        if (_uiState.value.isDailyMissionReminderEnabled) {
            DailyMissionNotificationManager.createNotificationChannel(app)
            DailyMissionNotificationManager.scheduleDailyReminder(app, hour, minute)
        }

        _uiState.update {
            it.copy(
                dailyMissionReminderHour = hour,
                dailyMissionReminderMinute = minute,
                toastMessage = String.format(Locale.getDefault(), "Đã đặt giờ nhắc nhở nhiệm vụ: %02d:%02d", hour, minute)
            )
        }
    }

    fun testTriggerDailyMissionReminder() {
        val app = getApplication<Application>()
        val missions = _uiState.value.dailySoloRankMissions
        val incomplete = missions.filter { !it.isCompleted }

        DailyMissionNotificationManager.createNotificationChannel(app)
        if (incomplete.isEmpty()) {
            DailyMissionNotificationManager.showMissionReminderNotification(
                context = app,
                incompleteCount = 1,
                totalCount = missions.size.coerceAtLeast(1),
                firstIncompleteTitle = "Thử nghiệm: Tham gia 2 trận Solo Rank War"
            )
        } else {
            DailyMissionNotificationManager.showMissionReminderNotification(
                context = app,
                incompleteCount = incomplete.size,
                totalCount = missions.size,
                firstIncompleteTitle = incomplete.first().title
            )
        }

        _uiState.update {
            it.copy(toastMessage = "Đã gửi thông báo nhắc nhở kiểm tra nhiệm vụ!")
        }
    }

    // =========================================================================
    // MULTI-PANE CODE EDITOR ENGINE
    // =========================================================================

    private fun observeSourceFiles() {
        viewModelScope.launch {
            repository.ensureDefaultSourceFiles()
            repository.allSourceFiles.collect { files ->
                _uiState.update { current ->
                    val currentOpen = current.openFileIds.filter { id -> files.any { f -> f.id == id } }
                    val newOpen = if (currentOpen.isEmpty() && files.isNotEmpty()) {
                        listOf(files.first().id)
                    } else {
                        currentOpen
                    }
                    val activeId = if (current.activeSourceFileId != null && files.any { it.id == current.activeSourceFileId }) {
                        current.activeSourceFileId
                    } else {
                        newOpen.firstOrNull()
                    }
                    val activeFile = files.find { it.id == activeId }
                    val content = if (current.activeSourceFileId != activeId || current.activeFileEditorContent.isEmpty()) {
                        activeFile?.content ?: ""
                    } else {
                        current.activeFileEditorContent
                    }

                    current.copy(
                        sourceFiles = files,
                        openFileIds = newOpen,
                        activeSourceFileId = activeId,
                        activeFileEditorContent = content
                    )
                }
            }
        }
    }

    private var codeAnalysisJob: Job? = null

    fun triggerBackgroundCodeAnalysis(content: String, fileExtension: String) {
        codeAnalysisJob?.cancel()
        codeAnalysisJob = viewModelScope.launch(Dispatchers.Default) {
            delay(200) // Debounce so keystrokes remain 60fps fluid
            val diagnostics = CodeDiagnosticsService.analyze(content, fileExtension)
            val errorMessages = diagnostics.map { "[${it.severity}] Dòng ${it.line}, Cột ${it.column}: ${it.message}" }
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        codeDiagnostics = diagnostics,
                        syntaxErrors = errorMessages
                    )
                }
            }
        }
    }

    fun selectSourceFile(fileId: Long) {
        val file = _uiState.value.sourceFiles.find { it.id == fileId } ?: return
        _uiState.update { current ->
            val updatedOpen = if (!current.openFileIds.contains(fileId)) {
                current.openFileIds + fileId
            } else {
                current.openFileIds
            }
            current.copy(
                activeSourceFileId = fileId,
                openFileIds = updatedOpen,
                activeFileEditorContent = file.content
            )
        }
        triggerBackgroundCodeAnalysis(file.content, file.fileExtension)
    }

    fun closeFileTab(fileId: Long) {
        _uiState.update { current ->
            val updatedOpen = current.openFileIds.filter { it != fileId }
            val newActiveId = if (current.activeSourceFileId == fileId) {
                updatedOpen.lastOrNull()
            } else {
                current.activeSourceFileId
            }
            val newActiveFile = current.sourceFiles.find { it.id == newActiveId }
            current.copy(
                openFileIds = updatedOpen,
                activeSourceFileId = newActiveId,
                activeFileEditorContent = newActiveFile?.content ?: ""
            )
        }
    }

    fun updateActiveEditorContent(newContent: String) {
        _uiState.update { it.copy(activeFileEditorContent = newContent) }
        val ext = _uiState.value.activeSourceFile?.fileExtension ?: "kt"
        triggerBackgroundCodeAnalysis(newContent, ext)
    }

    fun saveActiveFile() {
        val activeId = _uiState.value.activeSourceFileId ?: return
        val currentContent = _uiState.value.activeFileEditorContent
        viewModelScope.launch {
            repository.updateSourceFileContent(activeId, currentContent)
            _uiState.update {
                it.copy(toastMessage = "Đã lưu thay đổi vào cơ sở dữ liệu Room!")
            }
        }
    }

    fun revertActiveFile() {
        val activeId = _uiState.value.activeSourceFileId ?: return
        viewModelScope.launch {
            repository.revertSourceFile(activeId)
            val reverted = repository.getSourceFileById(activeId)
            if (reverted != null) {
                _uiState.update {
                    it.copy(
                        activeFileEditorContent = reverted.content,
                        toastMessage = "Đã hoàn tác về phiên bản gốc!"
                    )
                }
            }
        }
    }

    fun createNewSourceFile(name: String, parentDir: String, templateType: String) {
        val cleanName = name.trim()
        if (cleanName.isBlank()) return

        val extension = cleanName.substringAfterLast('.', "kt").lowercase()
        val templateContent = when (templateType.uppercase()) {
            "KOTLIN", "KT" -> """
package com.example.custom

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text

/**
 * Border Tactical Custom Script
 */
class CustomAgentTrigger {
    fun execute() {
        // Tùy chỉnh hành vi kích hoạt
    }
}
""".trimIndent()
            "JSON" -> """
{
  "module": "$cleanName",
  "version": "1.0",
  "enabled": true,
  "config": {}
}
""".trimIndent()
            "XML" -> """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="custom_tactical_key">Giá trị tác chiến mới</string>
</resources>
""".trimIndent()
            "MD", "MARKDOWN" -> """
# $cleanName

Tài liệu hướng dẫn hoặc mô tả chiến thuật.
""".trimIndent()
            else -> "// $cleanName\n\n"
        }

        viewModelScope.launch {
            val created = repository.createSourceFile(
                path = "",
                name = cleanName,
                extension = extension,
                parentDir = parentDir.trim(),
                content = templateContent
            )
            selectSourceFile(created.id)
            _uiState.update {
                it.copy(toastMessage = "Đã tạo tập tin mới: $cleanName")
            }
        }
    }

    fun deleteSourceFile(fileId: Long) {
        viewModelScope.launch {
            repository.deleteSourceFile(fileId)
            closeFileTab(fileId)
            _uiState.update {
                it.copy(toastMessage = "Đã xóa tập tin khỏi dự án!")
            }
        }
    }

    fun resetAllSourceFilesToDefault() {
        viewModelScope.launch {
            repository.resetAllSourceFilesToDefault()
            _uiState.update {
                it.copy(
                    openFileIds = emptyList(),
                    activeSourceFileId = null,
                    activeFileEditorContent = "",
                    toastMessage = "Đã khôi phục toàn bộ cây thư mục mẫu!"
                )
            }
        }
    }

    fun toggleExplorerPane() {
        _uiState.update { it.copy(isExplorerPaneVisible = !it.isExplorerPaneVisible) }
    }

    fun toggleOutputPane() {
        _uiState.update { it.copy(isOutputPaneVisible = !it.isOutputPaneVisible) }
    }

    fun setEditorFontSize(sizeSp: Float) {
        _uiState.update { it.copy(editorFontSizeSp = sizeSp.coerceIn(10f, 22f)) }
    }

    fun toggleLineWrap() {
        _uiState.update { it.copy(isLineWrapEnabled = !it.isLineWrapEnabled) }
    }

    fun setEditorSearchQuery(query: String) {
        _uiState.update { it.copy(editorSearchQuery = query) }
    }

    fun setActiveOutputTab(tabIndex: Int) {
        _uiState.update { it.copy(activeOutputTab = tabIndex, isOutputPaneVisible = true) }
    }

    fun runSimulatedBuild() {
        val files = _uiState.value.sourceFiles
        _uiState.update {
            it.copy(
                isBuildingCode = true,
                isOutputPaneVisible = true,
                activeOutputTab = 1
            )
        }

        viewModelScope.launch {
            val logs = mutableListOf<String>()
            logs.add("Executing Gradle build tasks: [:app:compileDebugKotlin, :app:processDebugResources]")
            logs.add("> Task :app:preBuild UP-TO-DATE")
            logs.add("> Task :app:checkDebugAarMetadata UP-TO-DATE")
            delay(180)
            logs.add("> Task :app:processDebugResources (${files.size} source resources verified)")
            delay(220)
            logs.add("> Task :app:compileDebugKotlin (Kotlin 2.0.21 on JVM 17)")

            // Quick simulated lint checks
            val errors = inspectProjectSyntax(files)
            if (errors.isNotEmpty()) {
                logs.add("e: [Build Diagnostic Failure]: Found ${errors.size} syntax notice(s).")
                errors.forEach { err -> logs.add("   $err") }
                logs.add("BUILD COMPLETED WITH WARNINGS in 0.84s")
            } else {
                logs.add("v: All source syntax blocks passed syntactic AST parsing.")
                logs.add("BUILD SUCCESSFUL in 0.62s (32 actionable tasks, all up-to-date)")
            }

            _uiState.update {
                it.copy(
                    isBuildingCode = false,
                    simulatedBuildLogs = logs,
                    syntaxErrors = errors
                )
            }
        }
    }

    fun checkSyntax() {
        val activeFile = _uiState.value.activeSourceFile ?: return
        val currentContent = _uiState.value.activeFileEditorContent
        val diagnostics = CodeDiagnosticsService.analyze(currentContent, activeFile.fileExtension)
        val errorMessages = diagnostics.map { "[${it.severity}] Dòng ${it.line}, Cột ${it.column}: ${it.message}" }

        _uiState.update {
            it.copy(
                codeDiagnostics = diagnostics,
                syntaxErrors = errorMessages,
                isOutputPaneVisible = true,
                activeOutputTab = 0,
                toastMessage = if (diagnostics.isEmpty()) "Cú pháp hoàn toàn hợp lệ!" else "Phát hiện ${diagnostics.size} lưu ý cú pháp & logic!"
            )
        }
    }

    fun insertCodeSnippet(snippet: String) {
        val current = _uiState.value.activeFileEditorContent
        val newContent = if (current.endsWith("\n") || current.isEmpty()) {
            current + snippet + "\n"
        } else {
            current + "\n\n" + snippet + "\n"
        }
        _uiState.update {
            it.copy(
                activeFileEditorContent = newContent,
                toastMessage = "Đã chèn đoạn mã mẫu (Snippet)!"
            )
        }
    }

    fun showCommandPalette() {
        _uiState.update { it.copy(isCommandPaletteVisible = true) }
    }

    fun hideCommandPalette() {
        _uiState.update { it.copy(isCommandPaletteVisible = false) }
    }

    fun toggleCommandPalette(visible: Boolean? = null) {
        _uiState.update { it.copy(isCommandPaletteVisible = visible ?: !it.isCommandPaletteVisible) }
    }

    fun formatActiveCode() {
        val activeFile = _uiState.value.activeSourceFile ?: return
        val current = _uiState.value.activeFileEditorContent
        val formatted = CodeFormattingService.formatCode(current, activeFile.fileExtension)
        if (formatted != current) {
            _uiState.update {
                it.copy(
                    activeFileEditorContent = formatted,
                    toastMessage = "Đã định dạng mã nguồn (Format Code)!"
                )
            }
        } else {
            _uiState.update {
                it.copy(toastMessage = "Mã nguồn đã được định dạng chuẩn xác!")
            }
        }
    }

    fun saveProject() {
        val activeId = _uiState.value.activeSourceFileId
        val currentContent = _uiState.value.activeFileEditorContent
        viewModelScope.launch {
            if (activeId != null) {
                repository.updateSourceFileContent(activeId, currentContent)
            }
            _uiState.update {
                it.copy(toastMessage = "Đã lưu toàn bộ dự án & tập tin tác chiến (Save Project)!")
            }
        }
    }

    fun deployProject() {
        val files = _uiState.value.sourceFiles
        _uiState.update {
            it.copy(
                isBuildingCode = true,
                isOutputPaneVisible = true,
                activeOutputTab = 1
            )
        }

        viewModelScope.launch {
            val logs = mutableListOf<String>()
            logs.add("════════════════════════════════════════════════════════════════")
            logs.add(" [BORDER HQ] BẮT ĐẦU QUY TRÌNH TRIỂN KHAI TÁC CHIẾN (DEPLOY)...")
            logs.add("════════════════════════════════════════════════════════════════")
            logs.add("> [DEPLOY 01/05] Kiểm tra cú pháp AST và tính toàn vẹn Trigger Set (${files.size} tập tin)...")
            delay(200)

            val errors = inspectProjectSyntax(files)
            if (errors.isNotEmpty()) {
                logs.add("! [CẢNH BÁO] Phát hiện ${errors.size} lưu ý cú pháp, đang áp dụng cơ chế tự sửa lỗi...")
            } else {
                logs.add("✓ AST Audit: Toàn bộ cú pháp và trigger logic hợp lệ.")
            }
            delay(220)

            logs.add("> [DEPLOY 02/05] Đóng gói gói nhị phân Triggers: BorderPackage-v2.4.bin (JVM Target 17)...")
            delay(240)
            logs.add("> [DEPLOY 03/05] Thiết lập kết nối mã hóa Trion Link tới máy chủ trung tâm Border HQ...")
            delay(220)
            logs.add("> [DEPLOY 04/05] Đồng bộ hóa tham số radar tác chiến và trigger slots...")
            delay(260)
            logs.add("✓ Trion Matrix Sync: Băng thông phản hồi 120Hz, độ trễ 0.4ms.")
            logs.add("> [DEPLOY 05/05] TRIỂN KHAI HOÀN TẤT: Triggers đã trực tuyến trên hệ thống Border HQ!")
            logs.add("════════════════════════════════════════════════════════════════")

            _uiState.update {
                it.copy(
                    isBuildingCode = false,
                    simulatedBuildLogs = logs,
                    syntaxErrors = errors,
                    toastMessage = "Triển khai thành công: Triggers đã được nạp lên Border HQ!"
                )
            }
        }
    }

    private fun inspectProjectSyntax(files: List<SourceFileEntity>): List<String> {
        val list = mutableListOf<String>()
        files.forEach { file ->
            val content = if (file.id == _uiState.value.activeSourceFileId) {
                _uiState.value.activeFileEditorContent
            } else {
                file.content
            }
            val openBraces = content.count { it == '{' } - content.count { it == '}' }
            if (openBraces != 0) {
                list.add("${file.fileName}: Lệch ngoặc nhọn ($openBraces)")
            }
        }
        return list
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        pomodoroJob?.cancel()
        soundManager.stopAndReleasePlayer()
        missionRewardSoundManager.release()
        bailOutSoundManager.release()
    }
}
