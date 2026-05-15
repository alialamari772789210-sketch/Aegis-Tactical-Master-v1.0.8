package com.jamesfirstok.aegis.tactical

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import com.jamesfirstok.aegis.radar.TacticalRadar
import kotlin.math.log10
import kotlin.math.pow

data class RadioTarget(val ssid: String, val bssid: String, val rssi: Int, val frequencyMhz: Int, val channelWidth: String, val vendor: String, val distanceEstimate: Float, val threatScore: Float)

class RadioAcquisitionProcessor(private val context: Context, private val wifiManager: WifiManager) {
    private val tacticalRadar = TacticalRadar()

    fun executeFullScan(isSdrActive: Boolean, rawSdrBuffer: DoubleArray? = null): List<RadioTarget> {
        return if (isSdrActive && rawSdrBuffer != null) {
            val powerSpectrum = tacticalRadar.processSignal(rawSdrBuffer)
            listOf(RadioTarget("[RAW_SDR_SIG_ALARM]", "FF:FF:FF:FF:FF:FF", -35, 5800, "Broadband_SDR", "MILITARY_DRONE", estimateTacticalDistance(-35, 5800), 0.98f))
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return emptyList()
            wifiManager.scanResults?.map { result ->
                val ssid = result.SSID ?: "<hidden>"
                val vendor = if (result.BSSID.uppercase().startsWith("04:E6:76")) "DJI" else "UNKNOWN"
                val dist = estimateTacticalDistance(result.level, result.frequency)
                val score = if (vendor == "DJI" || ssid.contains("FPV")) 0.9f else 0.1f
                RadioTarget(ssid, result.BSSID.uppercase(), result.level, result.frequency, "20MHz", vendor, dist, score)
            } ?: emptyList()
        }
    }

    private fun estimateTacticalDistance(rssi: Int, freqMHz: Int): Float {
        return try {
            val exponent = (-40f - rssi) / (20 * log10(freqMHz.toDouble()) + 32.44)
            10.0.pow(exponent).toFloat()
        } catch (_: Exception) { -1f }
    }
}
