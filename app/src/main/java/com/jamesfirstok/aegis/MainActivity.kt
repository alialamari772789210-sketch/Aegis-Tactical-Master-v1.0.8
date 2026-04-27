package com.jamesfirstok.aegis

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.jamesfirstok.aegis.model.SecurityModel
import com.jamesfirstok.aegis.service.AegisService
import org.json.JSONObject
import java.util.*

/**
 * AEGIS TACTICAL MASTER - SOVEREIGN CORE v7.2.6
 * Architect: Colonel Ali Al-Ammari
 * منظومة القيادة والسيطرة الموحدة: رادار تكتيكي، رصد SIGINT، وارتباط فضائي رقمي.
 */
class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var webView: WebView
    private val securityModel = SecurityModel()
    private var service: AegisService? = null
    private var isBound = false
    private lateinit var sensorManager: SensorManager
    private var magField = FloatArray(3)

    // الربط مع المحرك الخلفي (Service Engine)
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(n: ComponentName?, b: IBinder?) {
            val binder = b as AegisService.LocalBinder
            service = binder.getService()
            isBound = true
            // تشغيل المحرك التكتيكي فور الاتصال
            service?.startEngine()
            injectLogToUI("Aegis Engine: Connected & Secured")
        }
        override fun onServiceDisconnected(n: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. تهيئة الواجهة التكتيكية (Autonomous UI Engine)
        webView = WebView(this)
        setContentView(webView)
        configureSecureWebView()
        
        // 2. تفعيل الحساسات للرصد الميداني (Magnetic Radar)
        initSensors()
        
        // 3. فحص الصلاحيات والتشغيل
        checkPermissions()
        
        // 4. الربط مع المحرك السيادي
        startAegisService()
    }

    private fun configureSecureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        // جسر الربط السيادي (The Sovereign Bridge)
        webView.addJavascriptInterface(AegisInterface(), "AegisBridge")
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // تحية القائد عند تشغيل النظام
                webView.evaluateJavascript("triggerGreeting('Colonel Ali Al-Ammari');", null)
            }
        }
        
        // تحميل واجهة الرادار التكتيكية (HUD) من مجلد assets
        webView.loadUrl("file:///android_asset/code.html")
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    /**
     * الجسر البرمجي بين واجهة الرادار والنواة العصبية
     */
    inner class AegisInterface {
        @JavascriptInterface
        fun getTacticalData(): String {
            val data = JSONObject().apply {
                put("commander", "Ali Al-Ammari")
                put("rank", "Colonel (Eng)")
                put("status", "MISSION READY")
                put("mag_x", magField[0])
                put("sat_link", "CONNECTED (DELTA-992)")
            }
            // تشفير البيانات قبل إرسالها للواجهة لضمان السرية التامة
            return securityModel.encryptTacticalData(data.toString())
        }

        @JavascriptInterface
        fun triggerSos() {
            service?.startSos()
            injectLogToUI("SOS Protocol: Activated by Commander")
        }

        @JavascriptInterface
        fun speak(message: String) {
            // واجهة المنظومة الصوتية v2.3.0
            println("TACTICAL_VOICE: $message")
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magField = event.values
            // تحديث الرادار بنبضات مشفرة لحظية
            val signal = securityModel.encryptTacticalData(magField[0].toString())
            webView.evaluateJavascript("updateRadar('$signal')", null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun injectLogToUI(msg: String) {
        webView.evaluateJavascript("console.log('AEGIS_CORE: $msg');", null)
    }

    private fun checkPermissions() {
        val perms = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION, 
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.FOREGROUND_SERVICE
        )
        if (Build.VERSION.SDK_INT >= 31) {
            perms.addAll(listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE))
        }
        val missing = perms.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 101)
    }

    private fun startAegisService() {
        val intent = Intent(this, AegisService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
        sensorManager.unregisterListener(this)
    }
}
