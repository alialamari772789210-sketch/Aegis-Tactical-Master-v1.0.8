package com.jamesfirstok.aegis.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class AlertManager(private val context: Context) {

    fun triggerAlert(distance: Int) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when {
                distance < 300 -> {
                    val timings = longArrayOf(0, 200, 100, 200, 100, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
                distance < 800 -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 50))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }
}
