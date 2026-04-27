package com.jamesfirstok.aegis.service

import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect
import android.os.Build

/**
 * AEGIS ALERT MANAGER
 * إدارة التنبيهات المادية بناءً على القرب التكتيكي من الهدف.
 */
class AlertManager(private val context: Context) {

    fun triggerAlert(distance: Int) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when {
                distance < 300 -> { // خطر داهم (High Alert)
                    val timings = longArrayOf(0, 200, 100, 200, 100, 500)
                    val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                }
                distance < 800 -> { // هدف يقترب (Medium Alert)
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                else -> { // رصد بعيد
                    vibrator.vibrate(VibrationEffect.createOneShot(100, 50))
                }
            }
        } else {
            // دعم الإصدارات القديمة من أندرويد
            vibrator.vibrate(500)
        }
    }
}
