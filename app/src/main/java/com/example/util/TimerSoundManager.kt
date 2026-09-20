package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.R

/**
 * Manages audio and haptic notifications for Pomodoro and Focus timer completions.
 * Utilizes Android's MediaPlayer to deliver clear audio chimes when focus sessions end.
 */
class TimerSoundManager(private val context: Context) {

    private var activeMediaPlayer: MediaPlayer? = null

    /**
     * Plays the focus session end chime sound using Android MediaPlayer.
     * Incorporates custom bundled sound with system notification ringtone fallback and haptic buzz.
     */
    fun playFocusSessionEndSound() {
        playSoundInternal(isAlert = true)
    }

    /**
     * Plays a test preview chime so users can calibrate sound volume directly in the app.
     */
    fun playTestSound() {
        playSoundInternal(isAlert = false)
    }

    @Synchronized
    private fun playSoundInternal(isAlert: Boolean) {
        try {
            stopAndReleasePlayer()

            var player: MediaPlayer? = null

            // 1. Try bundled Bail Out sound or timer chime
            try {
                player = MediaPlayer.create(context, R.raw.bail_out)
            } catch (e: Throwable) {
                Log.w("TimerSoundManager", "Could not load R.raw.bail_out: ${e.message}")
            }

            if (player == null) {
                try {
                    player = MediaPlayer.create(context, R.raw.timer_end_chime)
                } catch (e: Throwable) {
                    Log.w("TimerSoundManager", "Could not load R.raw.timer_end_chime: ${e.message}")
                }
            }

            // 2. Fallback to system default notification or alarm sound
            if (player == null) {
                val soundUri = RingtoneManager.getDefaultUri(
                    if (isAlert) RingtoneManager.TYPE_NOTIFICATION else RingtoneManager.TYPE_NOTIFICATION
                ) ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

                if (soundUri != null) {
                    try {
                        player = MediaPlayer.create(context, soundUri)
                    } catch (e: Throwable) {
                        Log.w("TimerSoundManager", "MediaPlayer.create with system sound failed: ${e.message}")
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
            } else {
                // Secondary fallback: RingtoneManager
                try {
                    val fallbackUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    if (fallbackUri != null) {
                        val ringtone = RingtoneManager.getRingtone(context, fallbackUri)
                        ringtone?.play()
                    }
                } catch (e: Throwable) {
                    Log.w("TimerSoundManager", "Ringtone fallback failed: ${e.message}")
                }
            }

            // Haptic vibration feedback
            triggerVibration()
        } catch (e: Throwable) {
            Log.e("TimerSoundManager", "Failed to play timer sound notification", e)
        }
    }

    private fun triggerVibration() {
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
                    val timings = longArrayOf(0, 180, 80, 240)
                    val amplitudes = intArrayOf(0, 180, 0, 240)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 180, 80, 240), -1)
                }
            }
        } catch (_: Throwable) {
            // Ignore in test or unsupported environments
        }
    }

    @Synchronized
    fun stopAndReleasePlayer() {
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
}
