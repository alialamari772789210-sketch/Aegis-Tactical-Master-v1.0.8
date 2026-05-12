package com.jamesfirstok.aegis.drivers

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log

/**
 * AEGIS GNSS PROVIDER v7.2.6
 * مزود الإحداثيات السيادي – يقرأ GPS حقيقي من مستشعر الهاتف.
 * يطبق بروتوكول تصفية الإشارات الضعيفة (NMEA Filtering).
 */
class GnssProvider : LocationListener {

    companion object {
        private const val TAG = "GnssProvider"
        private const val MIN_ACCURACY_METERS = 10.0f  // أدنى دقة مقبولة
    }

    var lastTacticalLocation: Location? = null
        private set

    /**
     * استخراج الإحداثيات الدقيقة بصيغة خريطة.
     */
    fun getPreciseCoordinates(): Map<String, Double> {
        val loc = lastTacticalLocation
        return mapOf(
            "lat" to (loc?.latitude ?: 0.0),
            "lon" to (loc?.longitude ?: 0.0),
            "alt" to (loc?.altitude ?: 0.0),
            "accuracy" to (loc?.accuracy?.toDouble() ?: -1.0),
            "bearing" to (loc?.bearing?.toDouble() ?: 0.0),
            "speed" to (loc?.speed?.toDouble() ?: 0.0),
            "time" to (loc?.time?.toDouble() ?: 0.0)
        )
    }

    /**
     * استخراج الإحداثيات ككائن Location مباشرة.
     */
    fun getLastLocation(): Location? = lastTacticalLocation

    /**
     * التحقق من توفر إشارة GPS صالحة.
     */
    fun hasValidFix(): Boolean {
        val loc = lastTacticalLocation ?: return false
        return loc.accuracy > 0 && loc.accuracy <= MIN_ACCURACY_METERS
    }

    /**
     * استقبال تحديثات الموقع من النظام.
     * يطبق تصفية للإشارات الضعيفة.
     */
    override fun onLocationChanged(location: Location) {
        if (location.accuracy > 0 && location.accuracy <= MIN_ACCURACY_METERS) {
            lastTacticalLocation = location
            Log.d(TAG, "موقع تكتيكي محدث: lat=${location.latitude}, lon=${location.longitude}, acc=${location.accuracy}m")
        } else {
            Log.w(TAG, "إشارة GPS ضعيفة مهملة: accuracy=${location.accuracy}m")
        }
    }

    override fun onProviderEnabled(provider: String) {
        Log.i(TAG, "مزود الموقع مفعّل: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.w(TAG, "مزود الموقع معطّل: $provider")
        if (provider == LocationManager.GPS_PROVIDER) {
            lastTacticalLocation = null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "حالة المزود تغيرت: $provider -> $status")
    }
}
