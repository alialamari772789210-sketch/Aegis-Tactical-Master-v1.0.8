package com.jamesfirstok.aegis.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.*
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jamesfirstok.aegis.radar.DspProcessor
import kotlinx.coroutines.*

class RadarService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private lateinit var audioRecorder: AudioRecord
    private val processor = DspProcessor()
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification())
        startRadarProcessing()
    }
    
    private fun startRadarProcessing() {
        val bufferSize = AudioRecord.getMinBufferSize(
            48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT
        )
        
        audioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            48000, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_FLOAT,
            bufferSize
        )
        audioRecorder.startRecording()
        
        scope.launch {
            val buffer = FloatArray(2048)
            while (isActive) {
                audioRecorder.read(buffer, 0, buffer.size)
                val result = processor.processFrame(buffer)
                // Threat analysis + alerts
                AegisAnalyzer.analyze(result.melSpectrogram)
                delay(10) // 100 FPS
            }
        }
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "radar_channel",
            "Aegis Tactical Radar",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }
    
    private fun createNotification(): Notification = NotificationCompat.Builder(this, "radar_channel")
        .setContentTitle("Aegis Tactical Active")
        .setContentText("Real-time radar processing")
        .setSmallIcon(R.drawable.ic_radar)
        .build()
    
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        scope.cancel()
        audioRecorder.stop()
        audioRecorder.release()
        super.onDestroy()
    }
}
