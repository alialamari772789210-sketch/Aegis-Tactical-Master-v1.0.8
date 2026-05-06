package com.jamesfirstok.aegis.tactical

import android.Manifest
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.pow
import kotlin.math.sqrt

data class RadioTarget(
    val ssid: String,
    val rssi: Int,
    val frequency: Int,
    val distanceEstimate: Float, // meters
    val threatScore: Float
)

class RadioAcquisitionProcessor(private val context: Context, private val wifiManager: WifiManager) {
    
    suspend fun executeFullScan(): List<RadioTarget> {
        if (!hasScanPermission()) return emptyList()
        
        wifiManager.startScan()
        
        return wifiManager.scanResults
            .filter { isPotentialThreat(it) }
            .map { result ->
                val distance = estimateDistance(result.level)
                val score = calculateThreatScore(result, distance)
                RadioTarget(
                    ssid = result.SSID,
                    rssi = result.level,
                    frequency = result.frequency,
                    distanceEstimate = distance,
                    threatScore = score
                )
            }
            .sortedByDescending { it.threatScore }
    }
    
    private fun hasScanPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    
    private fun isPotentialThreat(result: ScanResult): Boolean {
        val suspiciousPatterns = listOf(
            "DJI", "AUTEL", "UAV", "FPV", "DRONE", "SKY", "QUAD"
        )
        val highPower = result.level > -40 // Strong signal
        val suspiciousName = suspiciousPatterns.any { 
            result.SSID.uppercase().contains(it) 
        }
        return highPower || suspiciousName
    }
    
    private fun estimateDistance(rssi: Int): Float {
        // Free space path loss model
        val txPower = -59 // 2.4GHz reference
        val pathLoss = txPower - rssi
        return 10f.pow((27.55f - (20f * log10(2400f)) + pathLoss) / 20f)
    }
}
