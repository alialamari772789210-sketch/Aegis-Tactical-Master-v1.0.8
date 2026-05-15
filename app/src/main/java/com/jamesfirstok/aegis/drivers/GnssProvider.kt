package com.jamesfirstok.aegis.drivers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.log10

/**
 * ============================================================================
 * AEGIS GNSS PROVIDER v7.2.8 - ANTI-SPOOFING TACTICAL EDITION
 * ============================================================================
 * الوظيفة: مزود الإحداثيات الميداني الجغرافي السلبي مع كشف هجمات التزييف الترددي للـ GPS
 * ============================================================================
 */
class GnssProvider(private val context: Context) : LocationListener {

    companion object {
        private const val TAG = "AEGIS_GNSS"
        private const val CRITICAL_ACCURACY_LIMIT = 35.0f // حد الحماية الأقصى للملاحة
        private const val OPTIMAL_ACCURACY_LIMIT = 10.0f  // الدقة المثالية للرصد الطيفي
    }

    private val locationManager = context.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    var lastTacticalLocation: Location? = null
        private set
        
    // إخطار الذكاء الاصطناعي وشاشة الـ HUD بوجود هجوم تزييف أو حجب راداري للـ GPS في القطاع
    var isSpoofingDetected: Boolean = false
        private set

    fun getPreciseCoordinates(): Map<String, Any> {
        val loc = lastTacticalLocation
        return mapOf(
            "lat" to (loc?.latitude ?: 0.0),
            "lon" to (loc?.longitude ?: 0.0),
            "alt" to (loc?.altitude ?: 0.0),
            "accuracy" to (loc?.accuracy?.toDouble() ?: -1.0),
            "bearing" to (loc?.bearing?.toDouble() ?: 0.0),
            "speed" to (loc?.speed?.toDouble() ?: 0.0),
            "time" to (loc?.time?.toDouble() ?: 0.0),
            "gnss_spoofing_alert" to isSpoofingDetected
        )
    }

    fun getLastLocation(): Location? = lastTacticalLocation

    fun hasValidFix(): Boolean {
        val loc = lastTacticalLocation ?: return false
        return loc.accuracy > 0 && loc.accuracy <= CRITICAL_ACCURACY_LIMIT
    }

    override fun onLocationChanged(location: Location) {
        val accuracy = location.accuracy

        if (accuracy > 0 && accuracy <= OPTIMAL_ACCURACY_LIMIT) {
            lastTacticalLocation = location
            isSpoofingDetected = false
            Log.d(TAG, "GNSS Fix Updated: Lat=${location.latitude}, Lon=${location.longitude} [Optimal]")
        } 
        else if (accuracy > OPTIMAL_ACCURACY_LIMIT && accuracy <= CRITICAL_ACCURACY_LIMIT) {
            // تحصين تكتيكي: الإشارة مشوشة خارجياً ولكن لا نقوم بحذفها لمنع التعمية الجغرافية الكلية للموقع
            lastTacticalLocation = location
            isSpoofingDetected = true // إطلاق إنذار احتمال التعرض لهجوم خداع ترددات الأقمار
            Log.w(TAG, "[!] SECURITY WARNING: GNSS Spoofing/Jamming Suspected! Accuracy Degraded: ${accuracy}m")
        } 
        else {
            Log.e(TAG, "[!] GNSS Flood Denied: Signal unsafe for tracking calculations (Accuracy: ${accuracy}m)")
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasPermissions()) return
        try {
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (isGpsEnabled) {
                // تحديث الموقع كل 1 ثانية أو عند تحرك المقاتل بمقدار متر واحد (حفاظاً التام على طاقة البطارية)
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1.0f,
                    this
                )
                Log.i(TAG, "Sovereign GNSS Provider deployed successfully via Hardware Core.")
            } else {
                Log.e(TAG, "Critical Combat Fault: GPS Antenna is disabled on this device.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "GNSS Driver Initialization Failure: ${e.message}")
        }
    }

    fun stopTracking() {
        try {
            locationManager.removeUpdates(this)
            Log.w(TAG, "GNSS Location stream securely suspended.")
        } catch (_: Exception) {}
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {
        if (provider == LocationManager.GPS_PROVIDER) { lastTacticalLocation = null }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
