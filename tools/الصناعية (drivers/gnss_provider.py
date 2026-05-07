package com.jamesfirstok.aegis.drivers

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class GnssProvider : LocationListener {
    var lastTacticalLocation: Location? = null

    fun getPreciseCoordinates(): Map<String, Double> {
        return mapOf(
            "lat" to (lastTacticalLocation?.latitude ?: 0.0),
            "lon" to (lastTacticalLocation?.longitude ?: 0.0),
            "alt" to (lastTacticalLocation?.altitude ?: 0.0),
            "accuracy" to (lastTacticalLocation?.accuracy?.toDouble() ?: -1.0)
        )
    }

    override fun onLocationChanged(location: Location) {
        // بروتوكول تصفية الإشارات الضعيفة (NMEA Filtering)
        if (location.accuracy < 10.0) {
            lastTacticalLocation = location
        }
    }
}
