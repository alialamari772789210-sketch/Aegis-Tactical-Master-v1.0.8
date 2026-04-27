package com.jamesfirstok.aegis.bridge

import android.webkit.JavascriptInterface
import android.util.Log
import com.jamesfirstok.aegis.service.AlertManager

/**
 * AEGIS TACTICAL BRIDGE
 * الربط السيادي بين النواة وواجهة العرض التكتيكية (HUD).
 */
class AegisBridge(private val alertManager: AlertManager) {

    @JavascriptInterface
    fun triggerTacticalAlert(level: String, distance: Int) {
        Log.i("AEGIS_BRIDGE", "Inbound Signal: Level $level at $distance meters")
        // تفعيل المحرك المادي للتنبيه (اهتزاز، صوت، ضوء)
        alertManager.triggerAlert(distance)
    }

    @JavascriptInterface
    fun getSystemStatus(): String {
        // هذه القيمة تظهر في شاشة الرادار المتغيرة
        return "SOVEREIGN_V7.2.6_ACTIVE"
    }

    @JavascriptInterface
    fun logToAndroid(message: String) {
        Log.d("AEGIS_HUD_LOG", message)
    }
}
