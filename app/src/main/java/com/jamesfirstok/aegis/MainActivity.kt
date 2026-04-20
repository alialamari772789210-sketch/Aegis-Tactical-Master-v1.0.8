package com.jamesfirstok.aegis

import android.content.Context
import android.hardware.*
import android.location.*
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var webView: WebView
    private lateinit var sensorManager: SensorManager
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
        
        // الجسر الرابط بين النواة والواجهة (السيطرة العالمية)
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun getAegisData(): String {
                val data = JSONObject().apply {
                    put("mag_x", magField[0])
                    put("control_status", 88) // نسبة السيطرة الظاهرة في الصور
                    put("neural_link", "CONNECTED")
                }
                return data.toString()
            }
            
            @JavascriptInterface
            fun runForcedAccess() {
                // تفعيل بروتوكول الولوج القسري لتعزيز الرصد
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
            // إرسال نبضات البيانات للرادار لتحديث الصور الحية
            webView.evaluateJavascript("updateRadar(${magField[0]})", null)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
