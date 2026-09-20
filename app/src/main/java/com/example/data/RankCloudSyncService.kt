package com.example.data

import android.util.Log
import com.example.model.AgentProfile
import com.example.model.getRankForPoints
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CloudAgentRank(
    val agentId: String = "",
    val agentName: String = "",
    val points: Int = 0,
    val rankTag: String = "C-RANK",
    val rankName: String = "C-Rank",
    val position: String = "Sniper",
    val matchesWon: Int = 0,
    val totalMatches: Int = 0,
    val lastSyncedAt: Long = 0L
)

sealed class CloudSyncState {
    object Idle : CloudSyncState()
    object Syncing : CloudSyncState()
    data class Success(val lastSyncedAt: Long, val message: String) : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()
}

class RankCloudSyncService {

    private val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance could not be initialized: ${e.message}")
            null
        }
    }

    suspend fun syncUserRankData(
        profile: AgentProfile,
        points: Int,
        matchesWon: Int = 0,
        totalMatches: Int = 0
    ): Result<CloudAgentRank> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(
            IllegalStateException("Firestore is not initialized. Ensure Firebase is configured.")
        )

        try {
            val rank = getRankForPoints(points)
            val agentDocId = profile.name.trim()
                .lowercase()
                .replace(Regex("[^a-z0-9_-]"), "_")
                .ifEmpty { "border_agent_default" }

            val cloudRank = CloudAgentRank(
                agentId = agentDocId,
                agentName = profile.name,
                points = points,
                rankTag = rank.tag,
                rankName = rank.name,
                position = profile.position,
                matchesWon = matchesWon,
                totalMatches = totalMatches,
                lastSyncedAt = System.currentTimeMillis()
            )

            val docData = mapOf(
                "agentId" to cloudRank.agentId,
                "agentName" to cloudRank.agentName,
                "points" to cloudRank.points,
                "rankTag" to cloudRank.rankTag,
                "rankName" to cloudRank.rankName,
                "position" to cloudRank.position,
                "matchesWon" to cloudRank.matchesWon,
                "totalMatches" to cloudRank.totalMatches,
                "lastSyncedAt" to cloudRank.lastSyncedAt
            )

            db.collection(COLLECTION_RANKS)
                .document(agentDocId)
                .set(docData)
                .await()

            Log.d(TAG, "Successfully synced user rank data for ${profile.name} ($points pts)")
            Result.success(cloudRank)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user rank data to Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchCloudLeaderboard(limit: Long = 20): Result<List<CloudAgentRank>> = withContext(Dispatchers.IO) {
        val db = firestore ?: return@withContext Result.failure(
            IllegalStateException("Firestore is not initialized.")
        )

        try {
            val snapshot = db.collection(COLLECTION_RANKS)
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val ranks = snapshot.documents.mapNotNull { doc ->
                try {
                    CloudAgentRank(
                        agentId = doc.getString("agentId") ?: doc.id,
                        agentName = doc.getString("agentName") ?: "Đặc vụ Border",
                        points = doc.getLong("points")?.toInt() ?: 0,
                        rankTag = doc.getString("rankTag") ?: "C-RANK",
                        rankName = doc.getString("rankName") ?: "C-Rank",
                        position = doc.getString("position") ?: "Sniper",
                        matchesWon = doc.getLong("matchesWon")?.toInt() ?: 0,
                        totalMatches = doc.getLong("totalMatches")?.toInt() ?: 0,
                        lastSyncedAt = doc.getLong("lastSyncedAt") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(ranks)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching cloud leaderboard from Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "RankCloudSyncService"
        const val COLLECTION_RANKS = "border_agent_ranks"
    }
}
