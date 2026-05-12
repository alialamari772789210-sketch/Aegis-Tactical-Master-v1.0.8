package com.jamesfirstok.aegis.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.*
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jamesfirstok.aegis.radar.DspProcessor
import kotlinx.coroutines.*

class RadarService : Service() {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var audioRecorder: AudioRecord
    private val processor = DspProcessor()
    private var alertManager: AlertManager? = null

    companion object {
        private const val TAG = "RadarService"
        private const val CHANNEL_ID = "radar_channel"
    }

    override fun onCreate() {
        super.onCreate()
        alertManager = AlertManager(this)
        createNotificationChannel()
        startForeground(1, createNotification())
        startRadarProcessing()
        Log.i(TAG, "Radar service created and processing started.")
    }

    private fun startRadarProcessing() {
        val sampleRate = 48000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT
        ).coerceAtLeast(2048)

        try {
            audioRecorder = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_FLOAT,
                bufferSize
            )

            if (audioRecorder.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord failed to initialize.")
                return
            }

            audioRecorder.startRecording()
            Log.i(TAG, "Audio recording started (buffer=$bufferSize).")

            scope.launch {
                val buffer = FloatArray(2048)
                while (isActive) {
                    val read = audioRecorder.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
                    if (read > 0) {
                        val result = processor.processFrame(buffer.copyOf(read))
                        analyzeThreat(result)
                    }
                    delay(10) // ~100 FPS
                }
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Microphone permission denied.", e)
        } catch (e: Exception) {
            Log.e(TAG, "AudioRecord error: ${e.message}", e)
        }
    }

    /**
     * تحليل الإطار الصوتي واتخاذ إجراء إذا كانت السعة عالية جداً.
     */
    private fun analyzeThreat(frame: DspProcessor.ProcessedFrame) {
        // عتبة بسيطة: إذا تجاوزت السعة 0.6 فهذا صوت عالٍ غير طبيعي (محرك مسيّرة قريبة)
        if (frame.amplitude > 0.6f) {
            Log.w(TAG, "High amplitude detected: ${frame.amplitude}")
            // تنبيه حسّي
            alertManager?.triggerAlert(distance = 200)  // مسافة تقديرية قريبة
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Aegis Tactical Radar",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Aegis Radar Active")
            .setContentText("Audio surveillance running")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        if (::audioRecorder.isInitialized) {
            audioRecorder.stop()
            audioRecorder.release()
        }
        Log.i(TAG, "Radar service destroyed.")
        super.onDestroy()
    }
}
