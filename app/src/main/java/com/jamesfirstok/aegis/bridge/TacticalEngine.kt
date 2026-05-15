package com.jamesfirstok.aegis.core

import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import kotlin.math.log10
import kotlin.math.pow

data class ThreatAnalysis(
    val status: String,
    val confidence: Float,
    val estimatedDistance: Float,
    val classification: String,
    val recommendedAction: String,
    val isNativeEngaged: Boolean = false
)

class TacticalEngine(private val wifiManager: WifiManager) {

    // استدعاء قنوات التحكم والاختراق الناتجة من دوال الـ C++
    private external fun mavlinkOverride(lastSeq: Int): Boolean
    private external fun activateControlLoop(): Float
    private external fun transmitSdrSirenJamming(freqMhz: Double): Boolean

    init {
        try {
            System.loadLibrary("aegis-core")
            Log.i("TacticalEngine", "✅ Aegis Native Core C++ Libraries Engaged.")
        } catch (e: UnsatisfiedLinkError) {
            Log.e("TacticalEngine", "Critical: Native Binaries Missing: ${e.message}")
        }
    }

    /**
     * التحليل والاشتباك التلقائي الموحد (يدعم إدخال بيانات الهاتف أو الـ SDR)
     */
    fun analyzeAndEngageHybrid(ssid: String, freqMhz: Int, rssi: Int, isSdrMode: Boolean): ThreatAnalysis {
        val distance = estimateDistance(rssi, freqMhz)
        val classification = classifySignal(ssid, freqMhz, rssi, isSdrMode)
        val confidence = calculateConfidence(classification, rssi, distance, isSdrMode)

        var isEngaged = false
        val action = decideAction(classification, confidence, distance)

        // بروتوكول الاشتباك الصارم الميداني
        if (confidence > 0.82f && distance < 70f) {
            isEngaged = executeCombatNeutralization(freqMhz, isSdrMode)
        }

        return ThreatAnalysis(
            status = if (confidence > 0.80f) "CRITICAL_ALERT" else "MONITORING",
            confidence = confidence,
            estimatedDistance = distance,
            classification = classification,
            recommendedAction = if (isEngaged) "TARGET_NEUTRALIZED" else action,
            isNativeEngaged = isEngaged
        )
    }

    private fun executeCombatNeutralization(frequencyMhz: Int, isSdrMode: Boolean): Boolean {
        return try {
            if (isSdrMode) {
                // [1. التحييد الحربي الكامل]: بث موجات تشويش كهرومغناطيسية عبر واجهة الـ SDR
                transmitSdrSirenJamming(frequencyMhz.toDouble())
                activateControlLoop()
                mavlinkOverride(1)
            } else {
                // [2. التحييد السيبراني الخفيف]: محاولة حقن حزم قمعية عبر الرابط اللاسلكي الداخلي
                activateControlLoop()
                mavlinkOverride(0)
            }
            true
        } catch (e: Exception) {
            Log.e("TacticalEngine", "Combat Operation Fault: ${e.message}")
            false
        }
    }

    private fun estimateDistance(rssi: Int, freqMhz: Int): Float {
        val txPower = -40
        return try {
            val exponent = (txPower - rssi) / (20 * log10(freqMhz.toDouble()) + 32.44)
            10.0.pow(exponent).toFloat()
        } catch (e: Exception) { -1f }
    }

    private fun classifySignal(ssid: String, freq: Int, rssi: Int, isSdrMode: Boolean): String {
        val lower = ssid.lowercase()
        val droneKeywords = listOf("dji", "mavic", "phantom", "fpv", "uav", "drone", "autel", "skydio")
        
        return when {
            isSdrMode -> "MILITARY_RF_THREAT_DETECTED" // أي إشارة ملتقطة بالـ SDR خارج النطاق المدني
            droneKeywords.any { lower.contains(it) } -> "POTENTIAL_DRONE_LINK"
            freq in 5725..5850 && rssi > -55 -> "HIGH_POWER_UAV_VHF"
            freq in 430..440 -> "TACTICAL_LPD_JAMMER" // [تعديل فيزيائي]: فحص نطاق الـ 433 ميجاهرتز الفعلي
            else -> "NON_THREAT_WIFI"
        }
    }

    private fun calculateConfidence(cls: String, rssi: Int, dist: Float, isSdrMode: Boolean): Float {
        var score = 0.2f
        if (isSdrMode) score += 0.6f
        if (cls == "POTENTIAL_DRONE_LINK" || cls == "TACTICAL_LPD_JAMMER") score += 0.4f
        if (rssi > -55) score += 0.2f
        if (dist in 0.5f..120f) score += 0.1f
        return score.coerceIn(0f, 1f)
    }

    private fun decideAction(cls: String, conf: Float, dist: Float): String {
        return when {
            conf > 0.8f && dist < 50f -> "IMMEDIATE_NEUTRALIZATION"
            conf > 0.5f -> "ACTIVE_TRACKING"
            else -> "PASSIVE_SURVEILLANCE"
        }
    }
}
