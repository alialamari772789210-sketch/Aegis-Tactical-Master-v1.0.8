package com.jamesfirstok.aegis.core

import android.net.wifi.WifiManager
import kotlin.math.log10
import kotlin.math.pow

data class ThreatAnalysis(
    val status: String,
    val signalQuality: Float,
    val distance: Float,
    val action: String
)

class TacticalEngine(private val wifiManager: WifiManager) {
    
    fun analyzeThreat(rssi: Int, frequency: Int): ThreatAnalysis {
        val isJamming = frequency in 430000..440000
        val quality = if (rssi < 0) (rssi / -100f) * 100f else 100f
        
        return if (isJamming) {
            ThreatAnalysis("CRITICAL", quality, 0f, "FREQUENCY_HOPPING")
        } else {
            ThreatAnalysis("SECURE", quality, estimateDistance(rssi), "STEALTH")
        }
    }
    
    private fun estimateDistance(rssi: Int): Float {
        val txPower = -59f
        val ratio = rssi / txPower.toFloat()
        return if (ratio < 1.0f) {
            ratio.pow(10)
        } else {
            0.89976f * ratio.pow(7.7095f) + 0.111f
        }
    }
}
