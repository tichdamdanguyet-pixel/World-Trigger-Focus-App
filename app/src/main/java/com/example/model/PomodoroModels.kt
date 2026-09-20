package com.example.model

enum class PomodoroPhase(
    val title: String,
    val subtitle: String,
    val defaultDurationSeconds: Int
) {
    FOCUS(
        title = "PHIÊN HỌC TẬP TRUNG",
        subtitle = "Tập trung cao độ giải quyết mục tiêu học tập",
        defaultDurationSeconds = 25 * 60
    ),
    SHORT_BREAK(
        title = "NGHỈ GIẢI LAO NGẮN",
        subtitle = "Thư giãn mắt, hít thở và uống nước",
        defaultDurationSeconds = 5 * 60
    ),
    LONG_BREAK(
        title = "NGHỈ GIẢI LAO DÀI",
        subtitle = "Hồi phục toàn diện năng lượng Trion",
        defaultDurationSeconds = 15 * 60
    )
}

data class PomodoroSessionLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val durationMinutes: Int,
    val cycleNumber: Int,
    val earnedPoints: Int
)

data class PomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingSeconds: Int = 25 * 60,
    val totalSecondsForPhase: Int = 25 * 60,
    val isRunning: Boolean = false,
    val currentCycle: Int = 1, // 1 to 4
    val totalCompletedPomodoros: Int = 0,
    val selectedSubject: String = "Ngoại ngữ Sniper",
    val focusWorkDurationMinutes: Int = 25,
    val shortBreakDurationMinutes: Int = 5,
    val longBreakDurationMinutes: Int = 15,
    val autoStartBreaks: Boolean = false,
    val recentLogs: List<PomodoroSessionLog> = emptyList()
) {
    val progressFraction: Float
        get() = if (totalSecondsForPhase > 0) {
            1f - (remainingSeconds.toFloat() / totalSecondsForPhase.toFloat())
        } else 0f
}
