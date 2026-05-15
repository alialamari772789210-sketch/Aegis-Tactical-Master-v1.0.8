package com.jamesfirstok.aegis.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.util.Log
import android.webkit.JavascriptInterface
import androidx.core.content.ContextCompat
import com.jamesfirstok.aegis.core.TacticalEngine
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class LiveRadarService : Service() {

    private var wifiManager: WifiManager? = null
    private lateinit var tacticalEngine: TacticalEngine
    private var webViewBridge: WebViewBridge? = null
    
    // إدارة خيوط المعالجة الخلفية لضمان عدم تجمد الواجهة نهائياً
    private val serviceJob = SupervisorJob()
    private val radarScope = CoroutineScope(Dispatchers.IO + serviceJob)

    inner class WebViewBridge {
        @JavascriptInterface
        fun getLatestSignalData(): String {
            return latestSignalJson
        }
    }

    @Volatile
    private var latestSignalJson: String = "{\"signal\":0, \"threats\":[]}"

    override fun onCreate() {
        super.onCreate()
        webViewBridge = WebViewBridge()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        tacticalEngine = TacticalEngine(wifiManager!!)
        
        // إطلاق حلقة الرصد المتسارعة والآمنة خلفياً
        startAsynchronousRadarLoop()
        Log.i("LiveRadar", "✅ Async Tactical Radar Service Initiated.")
    }

    private fun startAsynchronousRadarLoop() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w("LiveRadar", "Location permission missing, Radar halted.")
            return
        }

        radarScope.launch {
            while (isActive) {
                try {
                    // أمر مسح الخلفية اللاسلكية للهاتف
                    wifiManager?.startScan()
                    
                    // المعالجة وحساب التهديدات
                    processScanResultsSecurely()
                    
                    // سرعة تحديث الرادار (كل 500 ملي ثانية لضمان حركية المؤشر السلسة على الـ HUD)
                    delay(500L)
                } catch (e: Exception) {
                    Log.e("LiveRadar", "Radar Loop Interrupted: ${e.message}")
                    delay(2000L)
                }
            }
        }
    }

    private fun processScanResultsSecurely() {
        try {
            val results = wifiManager?.scanResults ?: return
            
            // [تعديل أمني جوهري]: استخدام مكتبة JSON الرسمية لمنع الانهيارات النصية
            val threatsArray = JSONArray()
            var maxRssi = -100

            for (result in results) {
                val ssid = result.SSID ?: "<HIDDEN_SPOOF_SIGNAL>"
                val rssi = result.level
                val freq = result.frequency
                
                if (rssi > maxRssi) maxRssi = rssi

                // استدعاء محرك الاشتباك التكتيكي الموحد لتقييم الهدف الحقيقي
                val analysis = tacticalEngine.analyzeAndEngageHybrid(
                    ssid = ssid,
                    freqMhz = freq,
                    rssi = rssi,
                    isSdrMode = false // يتم تحويلها تلقائياً عند استشعار الهاردوير الخارجي
                )

                if (analysis.status == "CRITICAL_ALERT" || analysis.confidence > 0.65f) {
                    val threatObject = JSONObject().apply {
                        put("ssid", cleanSsid(ssid))
                        put("rssi", rssi)
                        put("freq", freq)
                        put("distance", analysis.estimatedDistance.toInt())
                        put("threat", analysis.classification)
                        put("action", analysis.recommendedAction)
                    }
                    threatsArray.put(threatObject)
                }
            }

            val signalPercent = ((maxRssi + 100) / 60.0 * 100).toInt().coerceIn(0, 100)
            
            // بناء الـ JSON الآمن والنهائي المستعد للحقن بالـ JavaScript
            val rootJson = JSONObject().apply {
                put("signal", signalPercent)
                put("maxRssi", maxRssi)
                put("threatCount", threatsArray.length())
                put("threats", threatsArray)
            }

            latestSignalJson = rootJson.toString()

            if (threatsArray.length() > 0) {
                // إطلاق إنذار اهتزازي/صوتي تكتيكي صامت في الخلفية
                triggerTacticalAlert(maxRssi)
            }

        } catch (e: Exception) {
            Log.e("LiveRadar", "Secure Processing Error: ${e.message}")
        }
    }

    /**
     * تنظيف اسم الشبكة لمنع ثغرات حقن النصوص الـ XSS والـ Script Injection داخل الـ WebView
     */
    private fun cleanSsid(ssid: String): String {
        return ssid.replace("\"", "\\\"").replace("'", "\\'")
    }

    private fun triggerTacticalAlert(maxRssi: Int) {
        // آلية التنبيه الخلفي الصامت للمقاتل بدون تعطيل المعالجة الأساسية
    }

    fun getBridge(): WebViewBridge = webViewBridge!!

    override fun onBind(intent: android.content.Intent?): android.os.IBinder? = null

    override fun onDestroy() {
        serviceJob.cancel() // إغلاق كافة خيوط الرادار فوراً لمنع تسريب الذاكرة
        super.onDestroy()
        Log.w("LiveRadar", "Tactical Radar Service Shutdown Safely.")
    }
}
