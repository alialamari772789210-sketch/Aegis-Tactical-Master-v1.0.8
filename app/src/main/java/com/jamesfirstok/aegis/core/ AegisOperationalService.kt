package com.jamesfirstok.aegis.core

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.jamesfirstok.aegis.tactical.RadioAcquisitionProcessor
import kotlinx.coroutines.*

class AegisOperationalService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)
    private lateinit var radioProcessor: RadioAcquisitionProcessor
    private lateinit var engagementEngine: TacticalEngagementEngine

    override fun onCreate() {
        super.onCreate()
        val wifiManager = getSystemService(WIFI_SERVICE) as WifiManager
        radioProcessor = RadioAcquisitionProcessor(this, wifiManager)
        engagementEngine = TacticalEngagementEngine(this, AegisNativeCore())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification("نظام AEGIS: الرصد السيادي المتكامل نشط"))
        serviceScope.launch {
            while (isActive) {
                val isSdrConnected = engagementEngine.checkExternalHardware()
                val scanInterval = if (isSdrConnected) 50L else 400L
                val targets = radioProcessor.executeFullScan(isSdrConnected, if (isSdrConnected) DoubleArray(64) else null)
                targets.forEach { engagementEngine.executeTacticalAction(it) }
                delay(scanInterval)
            }
        }
        return START_STICKY
    }

    private fun createNotification(content: String): Notification {
        val channelId = "AEGIS_SOVEREIGN_OPS"
        getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(channelId, "Ops", NotificationManager.IMPORTANCE_HIGH))
        return NotificationCompat.Builder(this, channelId).setContentTitle("AEGIS C2").setContentText(content).setSmallIcon(android.R.drawable.ic_menu_compass).setOngoing(true).build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { serviceJob.cancel(); super.onDestroy() }
}
