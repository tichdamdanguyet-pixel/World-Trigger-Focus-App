package com.example.data

import com.example.data.local.entity.SourceFileEntity

object DefaultSourceFiles {
    fun getInitialProjectFiles(): List<SourceFileEntity> {
        val files = mutableListOf<SourceFileEntity>()

        // 1. AgentViewModel.kt
        val agentViewModelCode = """
package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AgentRank
import com.example.model.AgentTriggerSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Border Defense Agency - Core Tactical Agent ViewModel
 * Manages Solo Rank Wars, Trion energy reserves, and Focus Training loops.
 */
class AgentViewModel : ViewModel() {
    private val _agentState = MutableStateFlow(AgentState())
    val agentState = _agentState.asStateFlow()

    fun updateTrionPoints(ptsToAdd: Int) {
        _agentState.update { current ->
            val updated = (current.trionPoints + ptsToAdd).coerceAtLeast(0)
            current.copy(
                trionPoints = updated,
                currentRank = calculateRankTier(updated)
            )
        }
    }

    private fun calculateRankTier(points: Int): AgentRank {
        return when {
            points >= 10000 -> AgentRank.RANK_S
            points >= 8000 -> AgentRank.RANK_A
            points >= 4000 -> AgentRank.RANK_B
            else -> AgentRank.RANK_C
        }
    }

    fun triggerBailOut() {
        _agentState.update { it.copy(isBailedOut = true) }
    }
}

data class AgentState(
    val codename: String = "Tamasoma-02",
    val trionPoints: Int = 4250,
    val currentRank: AgentRank = AgentRank.RANK_B,
    val isBailedOut: Boolean = false
)
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/java/com/example/ui/AgentViewModel.kt",
                fileName = "AgentViewModel.kt",
                fileExtension = "kt",
                parentDir = "app/src/main/java/com/example/ui",
                content = agentViewModelCode,
                originalContent = agentViewModelCode
            )
        )

        // 2. TriggerSetModel.kt
        val triggerModelCode = """
package com.example.model

/**
 * World Trigger Weapon & Defense Trigger Architecture
 */
enum class TriggerCategory {
    MAIN_ATTACK,
    SUB_ATTACK,
    DEFENSE,
    OPTIONAL,
    TACTICAL
}

data class TriggerSlot(
    val id: String,
    val name: String,
    val category: TriggerCategory,
    val trionCost: Int,
    val rangeMeters: Float,
    val description: String
)

object BorderWeaponArsenal {
    val Asteroid = TriggerSlot(
        id = "w_ast",
        name = "Asteroid (Standard Bullet)",
        category = TriggerCategory.MAIN_ATTACK,
        trionCost = 12,
        rangeMeters = 80f,
        description = "High-velocity non-tracking trion shooter projectile."
    )

    val Kogetsu = TriggerSlot(
        id = "w_kog",
        name = "Kogetsu (Katana Blade)",
        category = TriggerCategory.MAIN_ATTACK,
        trionCost = 15,
        rangeMeters = 2.5f,
        description = "Traditional Japanese blade with superior edge sharpness."
    )

    val Shield = TriggerSlot(
        id = "w_shi",
        name = "Shield (Dual Layer)",
        category = TriggerCategory.DEFENSE,
        trionCost = 20,
        rangeMeters = 15f,
        description = "Versatile barrier against incoming trion impacts."
    )

    val Bagworm = TriggerSlot(
        id = "w_bag",
        name = "Bagworm (Cloaking Mantle)",
        category = TriggerCategory.OPTIONAL,
        trionCost = 5,
        rangeMeters = 0f,
        description = "Hides radar signature by absorbing sensor frequencies."
    )
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/java/com/example/model/TriggerModel.kt",
                fileName = "TriggerModel.kt",
                fileExtension = "kt",
                parentDir = "app/src/main/java/com/example/model",
                content = triggerModelCode,
                originalContent = triggerModelCode
            )
        )

        // 3. BailOutSoundManager.kt
        val bailOutCode = """
package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

/**
 * Tactical Emergency Sound & Haptics Controller for Border Agents
 */
class BailOutSoundManager(private val context: Context) {

    fun triggerBailOut(soundEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (soundEnabled) {
            playTacticalAudio()
        }
        if (hapticsEnabled) {
            triggerPulseHaptics()
        }
    }

    private fun playTacticalAudio() {
        try {
            val player = MediaPlayer()
            // In a real device, loads from R.raw.bail_out
            player.setVolume(0.9f, 0.9f)
        } catch (_: Throwable) {}
    }

    private fun triggerPulseHaptics() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 40, 90, 60, 160)
            val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
            vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/java/com/example/util/BailOutSoundManager.kt",
                fileName = "BailOutSoundManager.kt",
                fileExtension = "kt",
                parentDir = "app/src/main/java/com/example/util",
                content = bailOutCode,
                originalContent = bailOutCode
            )
        )

        // 4. SoloRankScreen.kt
        val soloRankCode = """
package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SoloRankScreen(
    currentPoints: Int,
    onStartMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "BORDER RANK WAR ARENA",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Current Trion: ${'$'}currentPoints PTS",
            style = MaterialTheme.typography.bodyMedium
        )
        Button(onClick = onStartMatch) {
            Text("ENGAGE OPPONENT")
        }
    }
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/java/com/example/ui/screens/SoloRankScreen.kt",
                fileName = "SoloRankScreen.kt",
                fileExtension = "kt",
                parentDir = "app/src/main/java/com/example/ui/screens",
                content = soloRankCode,
                originalContent = soloRankCode
            )
        )

        // 5. strings.xml
        val stringsXmlCode = """
<resources>
    <string name="app_name">Border Agent OS</string>
    <string name="mission_focus_title">Chế độ tập trung tác chiến</string>
    <string name="rank_solo_war_label">Đấu Rank Cá Nhân</string>
    <string name="status_bail_out">Tín hiệu Bail Out đã kích hoạt!</string>
</resources>
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/res/values/strings.xml",
                fileName = "strings.xml",
                fileExtension = "xml",
                parentDir = "app/src/main/res/values",
                content = stringsXmlCode,
                originalContent = stringsXmlCode
            )
        )

        // 6. colors.xml
        val colorsXmlCode = """
<resources>
    <color name="border_cyan_500">#06B6D4</color>
    <color name="border_slate_950">#020617</color>
    <color name="border_rank_a">#C084FC</color>
    <color name="border_rank_s">#EF4444</color>
</resources>
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/src/main/res/values/colors.xml",
                fileName = "colors.xml",
                fileExtension = "xml",
                parentDir = "app/src/main/res/values",
                content = colorsXmlCode,
                originalContent = colorsXmlCode
            )
        )

        // 7. build.gradle.kts
        val gradleKtsCode = """
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.borderagent.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "app/build.gradle.kts",
                fileName = "build.gradle.kts",
                fileExtension = "kts",
                parentDir = "app",
                content = gradleKtsCode,
                originalContent = gradleKtsCode
            )
        )

        // 8. config/agent_tactics.json
        val tacticsJsonCode = """
{
  "system": "Border Agent Tactical Operating System",
  "version": "2.4.0",
  "rules": {
    "auto_bail_out_trion_threshold": 120,
    "solo_rank_points_per_victory": 150,
    "focus_session_duration_minutes": 25,
    "tactical_haptic_strength": "HIGH"
  },
  "loadout_defaults": {
    "sniper": ["Ibis", "Lightning", "Bagworm", "Shield"],
    "attacker": ["Kogetsu", "Senku", "Raygust", "Thruster"],
    "gunner": ["Asteroid", "Meteor", "Hound", "Escudo"]
  }
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "config/agent_tactics.json",
                fileName = "agent_tactics.json",
                fileExtension = "json",
                parentDir = "config",
                content = tacticsJsonCode,
                originalContent = tacticsJsonCode
            )
        )

        // 9. README.md
        val readmeMdCode = """
# Border Agent OS (World Trigger)

Hệ điều hành giao diện chiến thuật dành cho đặc vụ Border.

## Tính năng chính:
- **Trình soạn thảo mã nguồn (Multi-Pane IDE)**: Tùy chỉnh các cấu hình Trigger, script tác chiến, và kiểm tra cú pháp trực tiếp.
- **Đấu Rank Đơn (Solo Rank Wars)**: Mô phỏng trận đấu với các đặc vụ hạng A/B/S.
- **Bộ đếm thời gian tập trung (Pomodoro Focus)**: Rèn luyện khả năng tập trung tác chiến.
- **Tín hiệu Bail Out**: Âm thanh cảnh báo và rung phản hồi khi khẩn cấp hoặc hoàn tất nhiệm vụ.
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "README.md",
                fileName = "README.md",
                fileExtension = "md",
                parentDir = "",
                content = readmeMdCode,
                originalContent = readmeMdCode
            )
        )

        // 10. metadata.json
        val metadataJsonCode = """
{
  "name": "Border Agent OS",
  "description": "World Trigger inspired study and focus training app with radar parameters and solo rank wars",
  "requestFramePermissions": [],
  "majorCapabilities": ["MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API"]
}
""".trimIndent()

        files.add(
            SourceFileEntity(
                filePath = "metadata.json",
                fileName = "metadata.json",
                fileExtension = "json",
                parentDir = "",
                content = metadataJsonCode,
                originalContent = metadataJsonCode
            )
        )

        return files
    }
}
