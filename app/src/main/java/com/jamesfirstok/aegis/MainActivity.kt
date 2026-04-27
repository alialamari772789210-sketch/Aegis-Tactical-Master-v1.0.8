package com.jamesfirstok.aegis

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.jamesfirstok.aegis.model.SecurityModel
import com.jamesfirstok.aegis.service.AegisService
import com.jamesfirstok.aegis.service.AlertManager
import com.jamesfirstok.aegis.tactical.RadioAcquisitionProcessor
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

/**
 * AEGIS TACTICAL MASTER - SOVEREIGN CORE v7.2.6
 * Architect: Colonel Ali Al-Ammari
 * المنظومة الموحدة: رصد مغناطيسي، رصد راديوي، وارتباط فضائي تكتيكي.
 */
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var webView: WebView
    private val securityModel = SecurityModel()
    private var aegisService: AegisService? = null
    private var isBound = false
    
    private lateinit var sensorManager: SensorManager
    private lateinit var alertManager: AlertManager
    private lateinit var radioProcessor: RadioAcquisitionProcessor
    
    private var magField = FloatArray(3)

    companion object {
        init {
            // تحميل المكتبة الصلبة المكتوبة بلغة C++
            System.loadLibrary("aegis-security-lib")
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(n: ComponentName?, b: IBinder?) {
            val binder = b as AegisService.LocalBinder
            aegisService = binder.getService()
            isBound = true
            aegisService?.startEngine()
            injectLogToUI("Aegis Engine: Sovereign Link Established")
        }
        override fun onServiceDisconnected(n: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. تهيئة المحركات التكتيكية والمادية
        alertManager = AlertManager(this)
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        radioProcessor = RadioAcquisitionProcessor(wifiManager)
        
        // 2. إعداد شاشة الـ HUD التكتيكية
        webView = WebView(this)
        setContentView(webView)
        configureSecureWebView()
        
        // 3. تفعيل البروتوكولات الميدانية
        initSensors()
        checkPermissions()
        startAegisService()
        startRadioScanLoop()
    }

    private fun configureSecureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        webView.addJavascriptInterface(AegisInterface(), "AegisBridge")
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                webView.evaluateJavascript("triggerGreeting('Colonel Ali Al-Ammari');", null)
            }
        }
        
        webView.loadUrl("file:///android_asset/code.html")
    }

    private fun startRadioScanLoop() {
        Timer().scheduleAtFixedRate(timerTask {
            val targets = radioProcessor.executeRadioAcquisition()
            if (targets.isNotEmpty()) {
                runOnUiThread {
                    webView.evaluateJavascript("updateRadarDisplay('${targets.size} Targets Detected');", null)
                    // تنبيه نبضي في حال اقتراب الهدف (إشارة > -50dBm)
                    val topGain = targets[0]["gain"] as Int
                    if (topGain > -50) alertManager.triggerAlert(300)
                }
            }
        }, 0, 5000)
    }

    inner class AegisInterface {
        @JavascriptInterface
        fun getTacticalData(): String {
            val data = JSONObject().apply {
                put("commander", "Ali Al-Ammari")
                put("status", "MISSION READY")
                put("mag_x", magField[0])
                put("sat_link", "ACTIVE")
            }
            return securityModel.encryptTacticalData(data.toString())
        }

        @JavascriptInterface
        fun triggerSos() {
            aegisService?.startSos()
            alertManager.triggerAlert(100)
        }
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magField = event.values
            val signal = securityModel.encryptTacticalData(magField[0].toString())
            webView.evaluateJavascript("updateMagneticPulse('$signal')", null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun injectLogToUI(msg: String) {
        webView.evaluateJavascript("console.log('AEGIS_CORE: $msg');", null)
    }

    private fun checkPermissions() {
        val perms = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 31) {
            perms.addAll(listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE))
        }
        val missing = perms.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 101)
    }

    private fun startAegisService() {
        val intent = Intent(this, AegisService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
        else startService(intent)
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) unbindService(connection)
        sensorManager.unregisterListener(this)
    }

    external fun validateSecurity(): String
}
