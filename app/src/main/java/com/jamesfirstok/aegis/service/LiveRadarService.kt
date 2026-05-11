package com.jamesfirstok.aegis.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.content.ContextCompat
import com.jamesfirstok.aegis.radar.DspProcessor
import kotlinx.coroutines.*

class LiveRadarService : RadarService() {

    private var wifiManager: WifiManager? = null
    private val uiHandler = Handler(Looper.getMainLooper())
    private var scanRunnable: Runnable? = null
    private var webViewBridge: WebViewBridge? = null

    // كائن الجسر الذي سيُحقن في WebView
    inner class WebViewBridge {
        @JavascriptInterface
        fun getLatestSignalData(): String {
            // سيتم استدعاؤها من JavaScript لجلب أحدث البيانات
            return latestSignalJson
        }
    }

    // متغير يحمل أحدث بيانات الإشارة بصيغة JSON
    @Volatile
    private var latestSignalJson: String = "{\"signal\":0, \"threats\":[]}"

    override fun onCreate() {
        super.onCreate()
        webViewBridge = WebViewBridge()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        startWifiScanLoop()
        Log.i("LiveRadar", "Live Radar Service with Wi-Fi bridge created.")
    }

    // حلقة مسح Wi-Fi كل 3 ثوانٍ
    private fun startWifiScanLoop() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LiveRadar", "Location permission missing, Wi-Fi scan disabled.")
            return
        }

        scanRunnable = object : Runnable {
            override fun run() {
                wifiManager?.startScan()
                processScanResults()
                uiHandler.postDelayed(this, 3000) // كل 3 ثوانٍ
            }
        }
        uiHandler.post(scanRunnable!!)
    }

    private fun processScanResults() {
        try {
            val results = wifiManager?.scanResults ?: return
            val threats = mutableListOf<Map<String, Any>>()
            
            for (result in results) {
                // استخدام نفس منطق RadioAcquisitionProcessor
                val suspiciousPatterns = listOf("DJI", "AUTEL", "UAV", "FPV", "DRONE", "SKY", "QUAD")
                val isThreat = suspiciousPatterns.any { result.SSID.uppercase().contains(it) } || result.level > -40
                
                if (isThreat) {
                    threats.add(mapOf(
                        "ssid" to result.SSID,
                        "rssi" to result.level,
                        "freq" to result.frequency,
                        "threat" to (if (result.level > -40) "HIGH" else "MEDIUM")
                    ))
                }
            }

            // بناء JSON
            val maxRssi = results.maxOfOrNull { it.level } ?: -100
            val signalPercent = ((maxRssi + 100) / 60.0 * 100).toInt().coerceIn(0, 100)
            
            latestSignalJson = """
                {
                    "signal": $signalPercent,
                    "maxRssi": $maxRssi,
                    "threatCount": ${threats.size},
                    "threats": ${threats.joinToString(prefix = "[", postfix = "]") { 
                        "{\"ssid\":\"${it["ssid"]}\",\"rssi\":${it["rssi"]},\"freq\":${it["freq"]},\"threat\":\"${it["threat"]}\"}" 
                    }}
                }
            """.trimIndent()

            // إذا وجد تهديد، استدعاء التنبيه
            if (threats.isNotEmpty()) {
                val alertDistance = estimateDistanceFromRssi(maxRssi)
                triggerAlert(alertDistance)
            }

        } catch (e: Exception) {
            Log.e("LiveRadar", "Error processing scan results", e)
        }
    }

    private fun estimateDistanceFromRssi(rssi: Int): Int {
        val txPower = -59
        val ratio = if (rssi == 0) 1.0 else (txPower - rssi) / 20.0
        return (Math.pow(10.0, ratio) * 3).toInt() // تقريب بالأمتار
    }

    fun getBridge(): WebViewBridge = webViewBridge!!

    override fun onDestroy() {
        super.onDestroy()
        scanRunnable?.let { uiHandler.removeCallbacks(it) }
        Log.i("LiveRadar", "Live Radar Service destroyed.")
    }
}
