package com.example.util

import com.example.data.local.entity.DailySoloRankMissionEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for exporting mission history data into formatted CSV format
 * with UTF-8 support and Excel-compatible Byte Order Mark (BOM).
 */
object MissionCsvExporter {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    val UTF8_BOM: ByteArray = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    /**
     * Converts a list of mission entities into CSV string format.
     */
    fun generateCsv(missions: List<DailySoloRankMissionEntity>): String {
        val sb = StringBuilder()

        // Standard CSV Headers
        val headers = listOf(
            "ID",
            "Ngay",
            "Tieu De",
            "Danh Muc",
            "Mo Ta",
            "Ghi Chu (Notes)",
            "Nhan Tag (Tags)",
            "Tien Do",
            "Muc Tieu",
            "Trang Thai",
            "Diem Thuong (PTS)",
            "Thoi Gian Hoan Tat",
            "Thoi Gian Tao"
        )
        sb.append(headers.joinToString(separator = ",", transform = { it.escapeCsv() }))
        sb.append("\r\n")

        for (mission in missions) {
            val completedTimeStr = mission.completedAt?.let {
                dateFormatter.format(Date(it))
            } ?: "Chưa hoàn tất"

            val createdTimeStr = dateFormatter.format(Date(mission.createdAt))

            val categoryLabel = when (mission.missionCategory) {
                "SOLO_WAR" -> "Đấu Rank (Solo War)"
                "WIN_MATCH" -> "Chiến thắng trận đấu"
                "FOCUS_TRAINING" -> "Rèn luyện Pomodoro"
                "STUDY_TACTIC" -> "Nghiên cứu chiến thuật"
                "COMBAT_STREAK" -> "Chuỗi thắng liên tiếp"
                else -> mission.missionCategory
            }

            val statusStr = if (mission.isCompleted) "HOÀN TẤT" else "ĐANG THỰC HIỆN"

            val row = listOf(
                mission.id.toString(),
                mission.dateString,
                mission.title,
                categoryLabel,
                mission.description,
                mission.customNotes,
                mission.customTags,
                mission.currentProgress.toString(),
                mission.targetCount.toString(),
                statusStr,
                mission.rewardPoints.toString(),
                completedTimeStr,
                createdTimeStr
            )

            sb.append(row.joinToString(separator = ",", transform = { it.escapeCsv() }))
            sb.append("\r\n")
        }

        return sb.toString()
    }

    /**
     * Generates a descriptive default filename for saving the CSV.
     */
    fun getDefaultFileName(): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "border_missions_history_$timeStamp.csv"
    }

    private fun String.escapeCsv(): String {
        return if (contains(",") || contains("\"") || contains("\n") || contains("\r")) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }
    }
}
