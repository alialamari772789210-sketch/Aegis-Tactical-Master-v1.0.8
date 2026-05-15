package com.jamesfirstok.aegis.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.log10
import kotlin.math.pow

class HardwareBypassEngine(private val context: Context) {

    companion object {
        private const val TAG = "AEGIS_HARDWARE_ENGINE"
    }

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val operationalMode = AtomicBoolean(false)

    // قفل المعالجة التكتيكي عالي الطاقة لمنع نوم التطبيق في الخلفية الميدانية
    private val wakeLock: PowerManager.WakeLock = powerManager.newWakeLock(
        PowerManager.PARTIAL_WAKE_LOCK, "AEGIS::OperationalRF"
    ).apply {
        setReferenceCounted(false)
    }

    private val RF_FINGERPRINTS = mapOf(
        "DJI_O3" to listOf("80MHz", "802.11ax"),
        "AUTEL_EVO" to listOf("40MHz", "802.11ac"),
        "GENERIC_FPV" to listOf("20MHz", "LEGACY")
    )

    /**
     * جلب وتحليل البيانات الراديوية الخام للوضع الهجين
     */
    fun getRawRadioData(): Map<String, Any> {
        if (!hasLocationPermission()) return buildFallbackData("PERMISSION_DENIED")
        if (!wifiManager.isWifiEnabled) return buildFallbackData("WIFI_DISABLED")

        return try {
            // [تعديل تكتيكي حاسم]: لتخطي حظر أندرويد لـ startScan، نتحول للمسح السلبي المستقر (Passive Scan Reception)
            // الكود يسحب أحدث تدفق لعينات الطيف المسجلة بالهوائي فوراً دون طلب إعادة مسح ميكانيكي حابس
            val results = wifiManager.scanResults ?: emptyList()
            if (results.isEmpty()) return buildFallbackData("NO_SIGNALS")

            val strongest = results.maxByOrNull { it.level }
            strongest?.let { analyzeTargetFingerprint(it) } ?: buildFallbackData("NO_TARGET")
        } catch (e: Exception) {
            Log.e(TAG, "RF acquisition failure: ${e.message}")
            buildFallbackData("RF_FAILURE")
        }
    }

    /**
     * تحليل البصمة اللاسلكية وكشف التزييف والخداع الترددي المعادي
     */
    private fun analyzeTargetFingerprint(result: ScanResult): Map<String, Any> {
        val standard = parseWifiStandard(result)
        val width = parseChannelWidth(result.channelWidth)
        val bssid = result.BSSID?.uppercase() ?: "00:00:00:00:00:00"
        val ssid = result.SSID ?: "<HIDDEN_SIGNAL_SPOOF>"

        var spoofingAlert = false
        RF_FINGERPRINTS.forEach { (type, traits) ->
            if (traits.contains(standard) && traits.contains(width)) {
                // إذا تطابقت البصمة الطيفية للمسيرة ولكن تم تزويد الاسم ليبدو كراوتر تقليدي، يتم إطلاق الإنذار
                if (!ssid.contains(type, ignoreCase = true)) {
                    spoofingAlert = true
                }
            }
        }

        val freqMhz = result.frequency
        val rssi = result.level

        return mapOf(
            "ssid" to ssid,
            "bssid" to bssid,
            "rssi" to rssi,
            "freq_mhz" to freqMhz,
            "width" to width,
            "standard" to standard,
            "spoofing_detected" to spoofingAlert,
            "fingerprint_match" to if (spoofingAlert) "SUSPICIOUS_UAV" else "NORMAL",
            "distance_m" to estimateTacticalDistance(rssi, freqMhz), // استخدام الدالة الفيزيائية الموحدة
            "timestamp" to System.currentTimeMillis(),
            "mode" to if (operationalMode.get()) "ACTIVE" else "PASSIVE"
        )
    }

    fun engageOperationalMode() {
        if (!wakeLock.isHeld) {
            // الاستيقاظ القسري لمدة 15 دقيقة تكتيكية متتالية قابلة للتجديد
            wakeLock.acquire(15 * 60 * 1000L)
            operationalMode.set(true)
            Log.i(TAG, "Sovereign RF Bypass Core Engaged. Fingerprinting active.")
        }
    }

    fun releaseResources() {
        try { if (wakeLock.isHeld) wakeLock.release() } catch (_: Exception) {}
        operationalMode.set(false)
        Log.w(TAG, "Operational resources released to fallback state.")
    }

    /**
     * [توحيد رياضي]: ربط دالة حساب المسافة بمعادلة الفراغ الحر FSPL القياسية والمعتمدة في بقية الأنوية الصلبة
     */
    private fun estimateTacticalDistance(rssi: Int, freqMHz: Int): Float {
        val txPower = -40f // قدرة الإرسال المرجعية المحدثة لهوائيات المسيرات
        return try {
            if (freqMHz <= 0 || rssi == 0) return -1f
            val exponent = (txPower - rssi) / (20 * log10(freqMHz.toDouble()) + 32.44)
            Math.pow(10.0, exponent).toFloat()
        } catch (_: Exception) { -1f }
    }

    private fun parseChannelWidth(width: Int) = when (width) {
        ScanResult.CHANNEL_WIDTH_20MHZ -> "20MHz"
        ScanResult.CHANNEL_WIDTH_40MHZ -> "40MHz"
        ScanResult.CHANNEL_WIDTH_80MHZ -> "80MHz"
        ScanResult.CHANNEL_WIDTH_160MHZ -> "160MHz"
        else -> "UNKNOWN"
    }

    private fun parseWifiStandard(result: ScanResult): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return "LEGACY"
        return when (result.wifiStandard) {
            ScanResult.WIFI_STANDARD_11N -> "802.11n"
            ScanResult.WIFI_STANDARD_11AC -> "802.11ac"
            ScanResult.WIFI_STANDARD_11AX -> "802.11ax"
            else -> "LEGACY"
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun buildFallbackData(reason: String) = mapOf(
        "rssi" to -100, 
        "freq_mhz" to 2412,
        "spoofing_detected" to false,
        "status" to reason, 
        "timestamp" to System.currentTimeMillis()
    )
}
