package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.data.local.entity.SoloRankMatchEntity
import com.example.ui.theme.Cyan400
import com.example.ui.theme.RankARank
import com.example.ui.theme.RankBRank
import com.example.ui.theme.RankCRank
import com.example.ui.theme.RankSRank

data class LeaderboardAgent(
    val rankPosition: Int = 0,
    val name: String,
    val squad: String,
    val role: String, // Sniper, Attacker, Shooter, All-Rounder, Gunner
    val points: Int,
    val rankTier: String, // S-RANK, A-RANK, B-RANK, C-RANK
    val tierColor: Color,
    val isUser: Boolean = false,
    val winRate: String,
    val triggerClass: String
)

data class SoloRankHistoryStats(
    val totalMatches: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val winRatePercent: Int = 0,
    val highestRoll: Int = 0,
    val currentStreak: Int = 0,
    val isWinningStreak: Boolean = true,
    val totalPointsGained: Int = 0,
    val totalPointsLost: Int = 0
)

fun computeHistoryStats(matches: List<SoloRankMatchEntity>): SoloRankHistoryStats {
    if (matches.isEmpty()) return SoloRankHistoryStats()

    var wins = 0
    var losses = 0
    var highestRoll = 0
    var pointsGained = 0
    var pointsLost = 0

    // Matches are sorted DESC by timestamp (most recent first)
    var streak = 0
    var streakTypeWon: Boolean? = null

    for ((index, match) in matches.withIndex()) {
        if (match.won) {
            wins++
            pointsGained += match.pointsDiff
        } else {
            losses++
            pointsLost += match.pointsDiff
        }

        if (match.playerRoll > highestRoll) {
            highestRoll = match.playerRoll
        }

        // Streak calculation based on recent matches
        if (index == 0) {
            streakTypeWon = match.won
            streak = 1
        } else if (streakTypeWon != null && match.won == streakTypeWon) {
            streak++
        } else if (streakTypeWon != null && match.won != streakTypeWon) {
            // Broken streak, stop counting current streak
            streakTypeWon = null
        }
    }

    val total = matches.size
    val winRate = if (total > 0) ((wins.toFloat() / total) * 100).toInt() else 0
    val firstMatchWon = matches.firstOrNull()?.won ?: true

    return SoloRankHistoryStats(
        totalMatches = total,
        wins = wins,
        losses = losses,
        winRatePercent = winRate,
        highestRoll = highestRoll,
        currentStreak = streak,
        isWinningStreak = firstMatchWon,
        totalPointsGained = pointsGained,
        totalPointsLost = pointsLost
    )
}

val BORDER_CANONICAL_AGENTS = listOf(
    LeaderboardAgent(
        name = "Shinoda Masafumi",
        squad = "HQ Commander",
        role = "All-Rounder",
        points = 16800,
        rankTier = "S-RANK",
        tierColor = RankSRank,
        winRate = "94%",
        triggerClass = "Kogetsu / Senku"
    ),
    LeaderboardAgent(
        name = "Kei Tachikawa",
        squad = "Tachikawa Squad",
        role = "No. 1 Attacker",
        points = 15400,
        rankTier = "S-RANK",
        tierColor = RankSRank,
        winRate = "91%",
        triggerClass = "Dual Kogetsu / Senku"
    ),
    LeaderboardAgent(
        name = "Masataka Ninomiya",
        squad = "Ninomiya Squad",
        role = "No. 1 Shooter",
        points = 14750,
        rankTier = "S-RANK",
        tierColor = RankSRank,
        winRate = "89%",
        triggerClass = "Asteroid / Hound"
    ),
    LeaderboardAgent(
        name = "Souya Kazama",
        squad = "Kazama Squad",
        role = "No. 2 Attacker",
        points = 12600,
        rankTier = "A-RANK",
        tierColor = RankARank,
        winRate = "86%",
        triggerClass = "Scorpion / Chameleon"
    ),
    LeaderboardAgent(
        name = "Shuji Miwa",
        squad = "Miwa Squad",
        role = "All-Rounder",
        points = 11400,
        rankTier = "A-RANK",
        tierColor = RankARank,
        winRate = "82%",
        triggerClass = "Handgun / Lead Bullet"
    ),
    LeaderboardAgent(
        name = "Isami Toma",
        squad = "Fuyushima Squad",
        role = "No. 1 Sniper",
        points = 10850,
        rankTier = "A-RANK",
        tierColor = RankARank,
        winRate = "84%",
        triggerClass = "Egret / Lightning"
    ),
    LeaderboardAgent(
        name = "Ken Satori",
        squad = "Kusakabe Squad",
        role = "Dual Sniper",
        points = 8900,
        rankTier = "A-RANK",
        tierColor = RankARank,
        winRate = "78%",
        triggerClass = "Dual Egret / Hound"
    ),
    LeaderboardAgent(
        name = "Ko Murakami",
        squad = "Suzunari-1",
        role = "No. 4 Attacker",
        points = 8150,
        rankTier = "A-RANK",
        tierColor = RankARank,
        winRate = "76%",
        triggerClass = "Raygust / Kogetsu"
    ),
    LeaderboardAgent(
        name = "Yuma Kuga",
        squad = "Tamakoma-2",
        role = "Ace Attacker",
        points = 7400,
        rankTier = "B-RANK",
        tierColor = RankBRank,
        winRate = "83%",
        triggerClass = "Scorpion / Grasshopper"
    ),
    LeaderboardAgent(
        name = "Shun Midorikawa",
        squad = "Kusakabe Squad",
        role = "Speed Attacker",
        points = 6200,
        rankTier = "B-RANK",
        tierColor = RankBRank,
        winRate = "72%",
        triggerClass = "Dual Scorpion"
    ),
    LeaderboardAgent(
        name = "Yoko Katori",
        squad = "Katori Squad",
        role = "All-Rounder",
        points = 5600,
        rankTier = "B-RANK",
        tierColor = RankBRank,
        winRate = "68%",
        triggerClass = "Handgun / Scorpion"
    ),
    LeaderboardAgent(
        name = "Chika Amatori",
        squad = "Tamakoma-2",
        role = "Trion Monster Sniper",
        points = 4800,
        rankTier = "B-RANK",
        tierColor = RankBRank,
        winRate = "65%",
        triggerClass = "Ibis / Lead Bullet"
    ),
    LeaderboardAgent(
        name = "Osamu Mikumo",
        squad = "Tamakoma-2",
        role = "Shooter / Captain",
        points = 4250,
        rankTier = "B-RANK",
        tierColor = RankBRank,
        winRate = "61%",
        triggerClass = "Raygust / Spider"
    ),
    LeaderboardAgent(
        name = "Tatsuhito Ikoma",
        squad = "Ikoma Squad",
        role = "No. 6 Attacker",
        points = 3800,
        rankTier = "C-RANK",
        tierColor = RankCRank,
        winRate = "58%",
        triggerClass = "Whirlwind Kogetsu"
    ),
    LeaderboardAgent(
        name = "Border Trainee Alpha",
        squad = "17th Trainee Unit",
        role = "Sniper Cadet",
        points = 1500,
        rankTier = "C-RANK",
        tierColor = RankCRank,
        winRate = "48%",
        triggerClass = "Ibis / Shield"
    ),
    LeaderboardAgent(
        name = "Border Trainee Beta",
        squad = "19th Trainee Unit",
        role = "Attacker Cadet",
        points = 800,
        rankTier = "C-RANK",
        tierColor = RankCRank,
        winRate = "42%",
        triggerClass = "Kogetsu"
    ),
    LeaderboardAgent(
        name = "Border Trainee Gamma",
        squad = "21st Trainee Unit",
        role = "Gunner Cadet",
        points = 350,
        rankTier = "C-RANK",
        tierColor = RankCRank,
        winRate = "35%",
        triggerClass = "Asteroid Submachine"
    )
)

fun generateLeaderboardWithUser(
    userName: String,
    userPoints: Int,
    userWinRate: String
): List<LeaderboardAgent> {
    val userRank = getRankForPoints(userPoints)
    val userAgent = LeaderboardAgent(
        name = "$userName (Bạn)",
        squad = "Sniper Unit",
        role = "Sniper Operative",
        points = userPoints,
        rankTier = userRank.tag,
        tierColor = userRank.color,
        isUser = true,
        winRate = userWinRate,
        triggerClass = "Ibis (High-Caliber)"
    )

    val combined = (BORDER_CANONICAL_AGENTS + userAgent)
        .sortedByDescending { it.points }

    return combined.mapIndexed { index, agent ->
        agent.copy(rankPosition = index + 1)
    }
}
