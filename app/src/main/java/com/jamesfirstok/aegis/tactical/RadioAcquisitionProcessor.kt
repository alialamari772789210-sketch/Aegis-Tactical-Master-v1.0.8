package com.jamesfirstok.aegis.tactical

import android.Manifest
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.log10
import kotlin.math.pow

data class RadioTarget(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channelWidth: String, // عرض النطاق (مهم للتمييز)
    val vendor: String,       // الشركة المصنعة للراديو
    val distanceEstimate: Float,
    val threatScore: Float
)

class RadioAcquisitionProcessor(
    private val context: Context,
    private val wifiManager: WifiManager
) {
    
    companion object {
        private const val TAG = "TacticalRadar"
        private val DRONE_VENDORS = mapOf(
            "60:60:1F" to "DJI",
            "00:26:7E" to "Parrot",
            "90:03:B7" to "Parrot",
            "00:03:2F" to "Autel",
            "04:E6:76" to "DJI"
        )
    }

    /**
     * الرادار التكتيكي: يستخدم المسح السلبي لتجاوز قيود أندرويد
     */
    fun executeTacticalRadar(): List<RadioTarget> {
        if (!hasScanPermission()) return emptyList()
        
        // بدلاً من startScan المتكرر، نأخذ آخر نتائج رصدها الهوائي (Passive)
        val results = wifiManager.scanResults ?: emptyList()
        
        return results
            .map { result -> analyzeTacticalTarget(result) }
            .sortedByDescending { it.threatScore }
    }

    private fun analyzeTacticalTarget(result: ScanResult): RadioTarget {
        val ssid = result.SSID ?: "<HIDDEN_SIGNAL>"
        val bssid = result.BSSID.uppercase()
        val rssi = result.level
        val freq = result.frequency
        
        // 1. تحديد جهة التصنيع من الـ MAC Address (أول 3 مقاطع)
        val vendorKey = if (bssid.length >= 8) bssid.substring(0, 8) else "UNKNOWN"
        val vendor = DRONE_VENDORS.getOrDefault(vendorKey, "UNKNOWN_RF_SOURCE")

        // 2. حساب المسافة مع معامل تصحيح للبيئة المفتوحة (Path Loss Exponent)
        val distance = estimateTacticalDistance(rssi, freq)

        // 3. تحليل التهديد (Logic Engine)
        val threatScore = calculateAdvancedThreat(ssid, vendor, freq, rssi)

        return RadioTarget(
            ssid = ssid,
            bssid = bssid,
            rssi = rssi,
            frequency = freq,
            channelWidth = translateWidth(result.channelWidth),
            vendor = vendor,
            distanceEstimate = distance,
            threatScore = threatScore
        )
    }

    private fun calculateAdvancedThreat(ssid: String, vendor: String, freq: Int, rssi: Int): Float {
        var score = 0.1f

        // أ. فحص البصمة الصناعية (Vendor Analysis)
        if (vendor != "UNKNOWN_RF_SOURCE") score += 0.5f

        // ب. فحص الأنماط النصية
        val dronePatterns = listOf("DJI", "MAVIC", "FPV", "UAV", "SKY-")
        if (dronePatterns.any { ssid.uppercase().contains(it) }) score += 0.3f

        // ج. تحليل القناة الترددية (قنوات الـ 5.8GHz غالباً ما تكون للمسيرات في المسافات القريبة)
        if (freq > 5700) score += 0.1f

        // د. القرب المكاني (RSSI)
        if (rssi > -45) score += 0.2f

        return score.coerceIn(0f, 1f)
    }

    private fun estimateTacticalDistance(rssi: Int, freqMHz: Int): Float {
        val txPower = -42f // قدرة إرسال مرجعية أقرب للواقع للمسيرات
        // معادلة FSPL محسنة
        return try {
            val exp = (txPower - rssi) / (20 * log10(freqMHz.toDouble()) + 32.44)
            10.0.pow(exp).toFloat()
        } catch (e: Exception) { -1f }
    }

    private fun translateWidth(width: Int): String = when(width) {
        ScanResult.CHANNEL_WIDTH_20MHZ -> "20MHz"
        ScanResult.CHANNEL_WIDTH_40MHZ -> "40MHz"
        ScanResult.CHANNEL_WIDTH_80MHZ -> "80MHz"
        else -> "N/A"
    }

    private fun hasScanPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
}
