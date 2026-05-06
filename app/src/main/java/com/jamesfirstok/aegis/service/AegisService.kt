package com.jamesfirstok.aegis.service

import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import com.jamesfirstok.aegis.model.SecurityModel
import java.util.UUID

class AegisService : Service() {

    private val binder = LocalBinder()
    private val securityModel = SecurityModel()
    private var bluetoothAdapter: BluetoothAdapter? = null

    inner class LocalBinder : Binder() {
        fun getService(): AegisService = this@AegisService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        startForeground(1, buildNotification())
        Log.d("AegisEngine", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AegisEngine", "Service started")
        return START_STICKY
    }

    fun startEngine() {
        Log.d("AegisEngine", "Engine initialization complete")
    }

    fun startSos() {
        val payload = "SOS:${System.currentTimeMillis()}"
        val encrypted = securityModel.encryptTacticalData(payload)
        Log.d("AegisEngine", "Prepared encrypted SOS payload: $encrypted")
        // هنا فقط نعد رسالة؛ الإرسال الفعلي يعتمد على BLE capabilities والأذونات
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "AegisCore",
                "Aegis Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        return Notification.Builder(this, "AegisCore")
            .setContentTitle("Aegis Active")
            .setContentText("Foreground service running")
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        Log.d("AegisEngine", "Service destroyed")
        super.onDestroy()
    }
}
