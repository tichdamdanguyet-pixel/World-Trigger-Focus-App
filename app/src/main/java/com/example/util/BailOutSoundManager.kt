package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

/**
 * Manages subtle audio notifications and tactile haptic feedback triggers for Border Agency
 * "Bail Out" events - specifically triggered when a focus timer session successfully completes
 * or when a scheduled Solo Rank War mission is marked finished.
 */
class BailOutSoundManager(private val context: Context) {

    private var soundPool: SoundPool? = null
    private var bailOutSoundId: Int = 0
    private var isSoundPoolLoaded: Boolean = false
    private var initAttempted: Boolean = false

    private var activeMediaPlayer: MediaPlayer? = null

    init {
        ensureSoundPoolInitialized()
    }

    @Synchronized
    private fun ensureSoundPoolInitialized() {
        if (initAttempted) return
        initAttempted = true
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(audioAttributes)
                .build()

            soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == bailOutSoundId) {
                    isSoundPoolLoaded = true
                    Log.d(TAG, "Bail Out notification sound loaded successfully into SoundPool (sampleId: $sampleId)")
                }
            }

            try {
                bailOutSoundId = soundPool?.load(context, R.raw.bail_out, 1) ?: 0
            } catch (e: Throwable) {
                Log.w(TAG, "Could not load R.raw.bail_out into SoundPool: ${e.message}")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "SoundPool initialization failed: ${e.message}")
            soundPool = null
        }
    }

    /**
     * Primary trigger function that simultaneously executes the subtle 'Bail Out' notification sound
     * and the tactical multi-pulse haptic vibration feedback.
     *
     * @param soundEnabled Whether audio playback is enabled by user settings
     * @param hapticsEnabled Whether haptic tactile vibration is enabled
     */
    fun triggerBailOut(soundEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (soundEnabled) {
            playBailOutSound()
        }
        if (hapticsEnabled) {
            triggerBailOutHaptics()
        }
    }

    /**
     * Plays the subtle 'Bail Out' sound effect via low-latency SoundPool with fallback to MediaPlayer.
     */
    @Synchronized
    fun playBailOutSound(volume: Float = 0.95f) {
        try {
            ensureSoundPoolInitialized()

            val pool = soundPool
            if (pool != null && isSoundPoolLoaded && bailOutSoundId != 0) {
                val streamId = pool.play(bailOutSoundId, volume, volume, 1, 0, 1.0f)
                if (streamId != 0) {
                    Log.d(TAG, "Played Bail Out sound via SoundPool (streamId: $streamId)")
                    return
                }
            }

            // Fallback to MediaPlayer
            playViaMediaPlayer()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to play Bail Out sound: ${e.message}", e)
        }
    }

    private fun playViaMediaPlayer() {
        try {
            stopMediaPlayer()

            var player: MediaPlayer? = null
            try {
                player = MediaPlayer.create(context, R.raw.bail_out)
            } catch (e: Throwable) {
                Log.w(TAG, "MediaPlayer could not load R.raw.bail_out: ${e.message}")
            }

            // Secondary fallback to timer end chime or system notification
            if (player == null) {
                try {
                    player = MediaPlayer.create(context, R.raw.timer_end_chime)
                } catch (_: Throwable) {
                }
            }

            if (player == null) {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (uri != null) {
                    try {
                        player = MediaPlayer.create(context, uri)
                    } catch (_: Throwable) {
                    }
                }
            }

            if (player != null) {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                player.setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (_: Throwable) {}
                    if (activeMediaPlayer == mp) {
                        activeMediaPlayer = null
                    }
                }
                player.setOnErrorListener { mp, _, _ ->
                    try {
                        mp.release()
                    } catch (_: Throwable) {}
                    if (activeMediaPlayer == mp) {
                        activeMediaPlayer = null
                    }
                    true
                }
                activeMediaPlayer = player
                player.start()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Fallback MediaPlayer playback failed: ${e.message}")
        }
    }

    /**
     * Triggers the subtle 'Bail Out' tactile haptic feedback signature.
     * Pattern:
     * 1. Initial quick crisp preparation tap (40ms)
     * 2. Short micro-pause (50ms)
     * 3. Secondary tactical pulse (70ms)
     * 4. Micro-pause (50ms)
     * 5. Resonant teleportation discharge pulse (140ms)
     */
    fun triggerBailOutHaptics() {
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
                    // Tactile Bail Out pattern: double subtle prep tap followed by smooth discharge
                    val timings = longArrayOf(0, 40, 50, 70, 50, 140)
                    val amplitudes = intArrayOf(0, 130, 0, 180, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 40, 50, 70, 50, 140), -1)
                }
                Log.d(TAG, "Triggered Bail Out haptic feedback")
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Bail Out haptics skipped: ${e.message}")
        }
    }

    private fun stopMediaPlayer() {
        try {
            activeMediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
        } catch (_: Throwable) {
        } finally {
            activeMediaPlayer = null
        }
    }

    fun release() {
        stopMediaPlayer()
        try {
            soundPool?.release()
        } catch (_: Throwable) {
        } finally {
            soundPool = null
            isSoundPoolLoaded = false
            bailOutSoundId = 0
        }
    }

    companion object {
        private const val TAG = "BailOutSoundManager"
    }
}
