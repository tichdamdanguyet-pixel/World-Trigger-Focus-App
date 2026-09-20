package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

/**
 * Manages low-latency audio playback for rewarding game actions in Border Agency,
 * particularly Solo Rank War mission completions, using Android's SoundPool.
 */
class MissionRewardSoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var rewardSoundId: Int = 0
    private var bailOutSoundId: Int = 0
    private var isLoaded: Boolean = false
    private var initAttempted: Boolean = false

    @Synchronized
    private fun ensureSoundPoolInitialized() {
        if (initAttempted) return
        initAttempted = true
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    isLoaded = true
                    Log.d(TAG, "Mission reward sound loaded successfully (sampleId: $sampleId)")
                }
            }

            try {
                bailOutSoundId = soundPool?.load(context, R.raw.bail_out, 1) ?: 0
            } catch (e: Throwable) {
                Log.w(TAG, "Could not load R.raw.bail_out: ${e.message}")
            }

            try {
                rewardSoundId = soundPool?.load(context, R.raw.mission_complete_reward, 1) ?: 0
            } catch (e: Throwable) {
                Log.w(TAG, "Could not load R.raw.mission_complete_reward: ${e.message}")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "SoundPool initialization skipped or unavailable: ${e.message}")
            soundPool = null
        }
    }

    /**
     * Plays the subtle 'Bail Out' sound effect and tactical haptics for completing a scheduled rank war mission.
     */
    fun playMissionCompletedSound(volume: Float = 0.95f) {
        try {
            ensureSoundPoolInitialized()

            val pool = soundPool
            val targetSound = if (bailOutSoundId != 0) bailOutSoundId else rewardSoundId

            if (pool != null && targetSound != 0) {
                pool.play(targetSound, volume, volume, 1, 0, 1.0f)
            } else {
                // Graceful fallback to MediaPlayer if SoundPool wasn't initialized
                try {
                    val mp = android.media.MediaPlayer.create(context, R.raw.bail_out)
                        ?: android.media.MediaPlayer.create(context, R.raw.mission_complete_reward)
                    mp?.setOnCompletionListener { it.release() }
                    mp?.start()
                } catch (_: Throwable) {
                }
            }

            // Provide crisp tactical Bail Out haptic feedback
            triggerRewardHaptics()
        } catch (e: Throwable) {
            Log.w(TAG, "Error playing reward sound: ${e.message}")
        }
    }

    private fun triggerRewardHaptics() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Tactile Bail Out pattern: double subtle prep tap followed by rewarding discharge pulse
                    val timings = longArrayOf(0, 40, 50, 70, 50, 140)
                    val amplitudes = intArrayOf(0, 140, 0, 180, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 40, 50, 70, 50, 140), -1)
                }
            }
        } catch (_: Throwable) {
            // Graceful ignore on devices without vibration or during unit testing
        }
    }

    /**
     * Releases SoundPool native resources when no longer needed.
     */
    fun release() {
        try {
            soundPool?.release()
        } catch (_: Throwable) {
        } finally {
            soundPool = null
            isLoaded = false
            rewardSoundId = 0
        }
    }

    companion object {
        private const val TAG = "MissionRewardSound"
    }
}
