package com.jamesfirstok.aegis.core

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.jamesfirstok.aegis.service.AlertManager
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * ============================================================================
 * AEGIS Sentinel Processor v12.2 - HARDWARE ANTI-TAMPERING LAYER
 * ============================================================================
 * الوظيفة: حماية البيانات السيادية وتدمير الموارد المؤقتة والـ RAM عند الصدمات العنيفة أو الأسر
 * ============================================================================
 */
class SentinelProcessor(
    private val sensorManager: SensorManager,
    private val orchestrator: AegisSystemOrchestrator, // ربط مباشر مع قائد المنظومة
    private val alertManager: AlertManager
) : SensorEventListener {

    private var lastAccel = FloatArray(3)
    private var tamperConfidence = 0.0f 

    companion object {
        private const val TAG = "AEGIS_SENTINEL"
        private const val SHOCK_THRESHOLD = 45f     // حد الصدمة المادية القوية (انفجار، سقوط قسري، انتزاع الجهاز)
        private const val TAMPER_WEIGHT = 0.5f      // وزن اهتزاز الحركة في كشف التلاعب
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val (x, y, z) = event.values
            
            val deltaX = x - lastAccel[0]
            val deltaY = y - lastAccel[1]
            val deltaZ = z - lastAccel[2]

            val kineticEnergy = sqrt(deltaX.pow(2) + deltaY.pow(2) + deltaZ.pow(2))

            // تقييم احتمالية تعرض الجهاز للتلاعب أو المحاولات الفيزيائية الانفصالية
            evaluateTamperStatus(kineticEnergy)

            if (tamperConfidence > 0.90f || kineticEnergy > SHOCK_THRESHOLD) {
                executeDataSanitizationProtocol()
            }

            lastAccel[0] = x; lastAccel[1] = y; lastAccel[2] = z
        }
    }

    private fun evaluateTamperStatus(energy: Float) {
        if (energy > 18f) {
            tamperConfidence += (energy / 100f) * TAMPER_WEIGHT
        } else {
            tamperConfidence -= 0.03f // تبريد وهدوء النظام عند الاستقرار الفيزيائي
        }
        tamperConfidence = tamperConfidence.coerceIn(0f, 1f)
    }

    private fun executeDataSanitizationProtocol() {
        Log.e(TAG, "[CRITICAL] TAMPER OR SHOCK DETECTED. INITIATING DATA WIPE PROTOCOL FOR SECURITY.")
        
        // إطلاق تحذير اهتزازي مشفر لإعلام المقاتل بنجاح الحماية الذاتية
        alertManager.triggerAlert(1000)

        // استدعاء دالة المسح العسكري العميق المتكاملة الحقيقية المفرودة داخل الـ Orchestrator لتصفير الـ RAM
        orchestrator.emergencySelfDestruct()

        tamperConfidence = 0f
    }

    fun startMonitoring() {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelSensor?.let {
            // استخدام SENSOR_DELAY_NORMAL لحماية بطارية الهاتف من الاستنزاف والتفريغ الميداني المتسارع
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.i(TAG, "Sovereign Anti-Tampering Layer Active [Power Optimized Mode]")
        }
    }

    fun stopMonitoring() {
        sensorManager.unregisterListener(this)
        Log.w(TAG, "Sentinel monitoring suspended.")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
