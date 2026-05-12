package com.jamesfirstok.aegis.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.*
import android.net.wifi.WifiManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jamesfirstok.aegis.radar.DspProcessor
import com.jamesfirstok.aegis.tactical.RadioAcquisitionProcessor
import kotlinx.coroutines.*
import kotlin.math.ln

/**
 * AEGIS TACTICAL RADAR SERVICE v7.2.6
 * 
 * منظومة رصد تكتيكية موحدة تجمع بين:
 * - الرادار الصوتي (الميكروفون) لالتقاط صوت المحركات
 * - الرادار الراديوي (Wi‑Fi) لكشف شبكات المسيرات
 * - محرك استدلال بايزي لتحديث احتمالية التهديد
 * - تنبيه واهتزاز عند تأكيد الهدف
 */
class TacticalRadarService : Service() {

    companion object {
        private const val TAG = "TacticalRadar"
        private const val CHANNEL_ID = "tactical_radar_channel"
        private const val SAMPLE_RATE = 48000
        
        // عتبات الرصد
        private const val RSSI_THRESHOLD = -55        // dBm – قوة إشارة مشبوهة
        private const val AUDIO_THRESHOLD = 0.6f      // سعة صوتية مشبوهة
        private const val BAYESIAN_THRESHOLD = 0.85   // عتبة بايزية للتحييد
        
        // احتمالات قبلية (Prior Probabilities)
        private const val PRIOR_DRONE = 0.1           // احتمال وجود مسيّرة
        private const val PRIOR_CIVILIAN = 0.5        // احتمال جهاز مدني
    }

    // Coroutine
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // الرادار الصوتي
    private lateinit var audioRecorder: AudioRecord
    private val dspProcessor = DspProcessor()

    // الرادار الراديوي
    private var wifiManager: WifiManager? = null
    private var radioProcessor: RadioAcquisitionProcessor? = null

    // محرك بايزي
    private var bayesianProbability = PRIOR_DRONE
    private var consecutiveThreats = 0
    private var totalScans = 0

    // التنبيه
    private var alertManager: AlertManager? = null

    // حالة
    private var isScanning = false

    override fun onCreate() {
        super.onCreate()
        alertManager = AlertManager(this)
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        radioProcessor = wifiManager?.let { RadioAcquisitionProcessor(this, it) }
        
        createNotificationChannel()
        startForeground(1, createNotification())
        
        startAudioRadar()
        startRadioRadar()
        
        Log.i(TAG, "Tactical Radar Service started – Audio + Wi‑Fi + Bayesian.")
    }

    // ============================================================
    // 1. الرادار الصوتي (Audio)
    // ============================================================
    private fun startAudioRadar() {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        ).coerceAtLeast(2048)

        try {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )

            if (audioRecorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize.")
                return
            }

            audioRecorder.startRecording()

            scope.launch {
                val buffer = FloatArray(2048)
                while (isActive) {
                    val read = audioRecorder.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        val frame = dspProcessor.processFrame(buffer.copyOf(read))
                        processAudioThreat(frame.amplitude)
                    }
                    delay(10) // ~100 FPS
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Microphone permission denied.", e)
        }
    }

    // ============================================================
    // 2. الرادار الراديوي (Wi‑Fi)
    // ============================================================
    private fun startRadioRadar() {
        isScanning = true
        scope.launch {
            while (isActive && isScanning) {
                scanWiFi()
                delay(3000) // مسح كل 3 ثوانٍ
            }
        }
    }

    private suspend fun scanWiFi() {
        try {
            val targets = radioProcessor?.executeFullScan() ?: return
            totalScans++
            
            if (targets.isNotEmpty()) {
                val topThreat = targets.first()
                Log.d(TAG, "Wi‑Fi threat: ${topThreat.ssid} RSSI=${topThreat.rssi} Score=${topThreat.threatScore}")
                
                if (topThreat.rssi > RSSI_THRESHOLD) {
                    processRadioThreat(topThreat.rssi, topThreat.threatScore)
                }
            } else {
                // لا تهديدات – اضمحلال الاحتمالية
                updateBayesianProbability(likelihood = 0.3f, isThreat = false)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Wi‑Fi scan permission missing.", e)
        }
    }

    // ============================================================
    // 3. معالجة التهديد الصوتي
    // ============================================================
    private fun processAudioThreat(amplitude: Float) {
        if (amplitude > AUDIO_THRESHOLD) {
            Log.w(TAG, "Audio threat: amplitude=$amplitude")
            updateBayesianProbability(likelihood = 0.7f, isThreat = true)
            checkBayesianThreshold("صوتي")
        }
    }

    // ============================================================
    // 4. معالجة التهديد الراديوي
    // ============================================================
    private fun processRadioThreat(rssi: Int, threatScore: Float) {
        // تحويل RSSI إلى likelihood (كلما كان أقوى كان أخطر)
        val likelihood = (threatScore * 0.6f + (rssi.coerceIn(-100, -30) + 100) / 140f * 0.4f)
        updateBayesianProbability(likelihood, isThreat = true)
        checkBayesianThreshold("راديوي")
    }

    // ============================================================
    // 5. محرك الاستدلال البايزي
    // ============================================================
    private fun updateBayesianProbability(likelihood: Float, isThreat: Boolean) {
        // P(H|E) = P(E|H) * P(H) / P(E)
        // P(E) = P(E|H)*P(H) + P(E|¬H)*P(¬H)
        val p_e_given_h = likelihood
        val p_e_given_not_h = if (isThreat) 0.3f else 0.7f
        
        val numerator = p_e_given_h * bayesianProbability
        val denominator = (p_e_given_h * bayesianProbability) + (p_e_given_not_h * (1f - bayesianProbability))
        
        if (denominator > 0f) {
            bayesianProbability = (numerator / denominator).coerceIn(0.01f, 0.99f)
            Log.d(TAG, "بايز: P(تهديد) = ${"%.3f".format(bayesianProbability)} | likelihood=$likelihood")
        }
    }

    // ============================================================
    // 6. فحص العتبة واتخاذ القرار
    // ============================================================
    private fun checkBayesianThreshold(source: String) {
        if (bayesianProbability >= BAYESIAN_THRESHOLD) {
            consecutiveThreats++
            Log.e(TAG, "!!! تهديد مؤكد ($source) – P=${"%.3f".format(bayesianProbability)} | متتالي=$consecutiveThreats")
            
            // تنبيه حسّي
            val estimatedDistance = if (bayesianProbability > 0.9f) 200 else 500
            alertManager?.triggerAlert(estimatedDistance)
            
            // إذا تأكد التهديد 3 مرات متتالية
            if (consecutiveThreats >= 3) {
                engageNeutralization()
                consecutiveThreats = 0
            }
        } else {
            consecutiveThreats = 0
        }
    }

    // ============================================================
    // 7. التحييد
    // ============================================================
    private fun engageNeutralization() {
        Log.e(TAG, "⚡ تفعيل بروتوكول التحييد التكتيكي!")
        // استدعاء NeutralizationCore
        try {
            val neutralizationCore = com.jamesfirstok.aegis.security.NeutralizationCore()
            neutralizationCore.activateMavlinkHijack()
            neutralizationCore.startSignalJamming(2412f)
        } catch (e: Exception) {
            Log.e(TAG, "فشل التحييد: ${e.message}")
        }
        bayesianProbability = PRIOR_DRONE // إعادة تعيين
    }

    // ============================================================
    // إشعار
    // ============================================================
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Aegis Tactical Radar",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aegis Tactical Radar Active")
            .setContentText("Audio + Wi‑Fi + Bayesian fusion")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    // ============================================================
    // دورة الحياة
    // ============================================================
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isScanning = false
        scope.cancel()
        if (::audioRecorder.isInitialized) {
            audioRecorder.stop()
            audioRecorder.release()
        }
        Log.i(TAG, "Tactical Radar Service destroyed.")
        super.onDestroy()
    }
}
