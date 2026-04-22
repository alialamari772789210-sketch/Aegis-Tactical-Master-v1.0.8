package com.jamesfirstok.aegis

import android.content.Context
import android.hardware.*
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.jamesfirstok.aegis.model.SecurityModel // استدعاء الحماية
import org.json.JSONObject

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var webView: WebView
    private lateinit var sensorManager: SensorManager
    private val securityModel = SecurityModel()
    private var magField = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)
        setupWebCore()
        initSensors()
    }

    private fun setupWebCore() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
        }
        
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun getAegisData(): String {
                val rawData = JSONObject().apply {
                    put("mag_x", magField[0])
                    put("control_status", 88) // نسبة السيطرة السيادية
                    put("neural_link", "CONNECTED") // حالة الربط الذكي
                    put("architect", "Ali Al-Ammari") // توثيق المعماري
                }.toString()
                
                // تشفير شامل لكافة البيانات قبل خروجها من النواة
                return securityModel.encryptTacticalData(rawData)
            }
            
            @JavascriptInterface
            fun runForcedAccess() {
                // تفعيل بروتوكول الولوج القسري السيادي
            }
        }, "AegisBridge")

        webView.loadUrl("file:///android_asset/code.html")
    }

    private fun initSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magField = event.values
            // إرسال نبضة الرادار مشفرة لضمان سرية الرصد الميداني
            val encryptedSignal = securityModel.encryptTacticalData(magField[0].toString())
            webView.evaluateJavascript("updateRadar('$encryptedSignal')", null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
