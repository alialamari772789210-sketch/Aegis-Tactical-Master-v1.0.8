package com.jamesfirstok.aegis.core

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import com.jamesfirstok.aegis.ai.AegisAIAnalyzer
import com.jamesfirstok.aegis.radar.TacticalRadar
import com.jamesfirstok.aegis.security.NeutralizationCore
import com.jamesfirstok.aegis.service.AlertManager
import kotlinx.coroutines.*

class AegisSystemOrchestrator(private val context: Context) {
    private val bypassEngine = HardwareBypassEngine(context)
    private val neutralizationCore = NeutralizationCore()
    private val aiAnalyzer = AegisAIAnalyzer(context)
    private val alertManager = AlertManager(context)
    private val tacticalRadar = TacticalRadar()
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    private var isExternalHardwareActive = false
    private var connectedSdrDevice: UsbDevice? = null

    fun setOperationMode(isExternal: Boolean, usbDevice: UsbDevice? = null) {
        this.isExternalHardwareActive = isExternal
        this.connectedSdrDevice = usbDevice
    }

    fun initializeTacticalCore() {
        Log.i("AEGIS", "INIT: Sovereign Tactical Core starting...")
        bypassEngine.engageOperationalMode()
        scope.launch { tacticalDecisionLoop() }
    }
    
    private suspend fun tacticalDecisionLoop() {
        while (isActive) {
            try {
                var rssi = -100; var freq = 2412; var isSpoofed = false
                if (!isExternalHardwareActive) {
                    val rfData = bypassEngine.getRawRadioData()
                    rssi = rfData["rssi"] as? Int ?: -100
                    freq = rfData["freq_mhz"] as? Int ?: 2412
                    isSpoofed = rfData["spoofing_detected"] as? Boolean ?: false
                    tacticalRadar.startSurveillance(DoubleArray(128) { Math.abs(rssi.toDouble()) })
                } else {
                    val rawSdrSignal = DoubleArray(64) { Math.sin(it.toDouble() * 0.5) }
                    val powerSpectrum = tacticalRadar.startSurveillance(rawSdrSignal)
                    freq = 2412; rssi = -45; isSpoofed = false
                }
                val threatInput = floatArrayOf(rssi.toFloat() / -100f, freq.toFloat() / 6000f, if (isSpoofed) 1.0f else 0.0f, if (isExternalHardwareActive) 1.0f else 0.0f)
                val prediction = aiAnalyzer.analyzeThreat(threatInput)
                if (prediction.confidence > 0.85f || (isSpoofed && rssi > -55)) {
                    executeRedAlert(freq, rssi, "AI_TACTICAL_LOCK")
                }
            } catch (e: Exception) { Log.e("AEGIS", "Cycle Error: ${e.message}") }
            delay(20L)
        }
    }
    
    private fun executeRedAlert(frequency: Int, rssi: Int, triggerSource: String) {
        alertManager.triggerAlert(500)
        if (isExternalHardwareActive) {
            neutralizationCore.nativeMavlinkInject()
            neutralizationCore.nativeSignalJam(frequency.toFloat())
        }
    }

    fun executeManualOverride() { executeRedAlert(2412, -30, "COMMANDER_DIRECT_ORDER") }
    fun emergencySelfDestruct() { bypassEngine.releaseResources(); scope.cancel() }
    fun shutdown() { bypassEngine.releaseResources(); scope.cancel() }
}
