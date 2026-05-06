package com.jamesfirstok.aegis.core

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class SentinelProcessor(private val sensorManager: SensorManager) : SensorEventListener {
    
    private var fusionData = FloatArray(9)
    
    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> fusionData[0..2] = event.values
            Sensor.TYPE_MAGNETIC_FIELD -> fusionData[3..5] = event.values
            Sensor.TYPE_GYROSCOPE -> fusionData[6..8] = event.values
        }
        
        val confidence = calculateFusionConfidence()
        if (confidence > 0.95f) {
            triggerNeutralization()
        }
    }
    
    private fun calculateFusionConfidence(): Float {
        val mag = sqrt(fusionData.sumOf { it * it }.toDouble()).toFloat()
        return (mag / 100f).coerceIn(0f, 1f)
    }
}
