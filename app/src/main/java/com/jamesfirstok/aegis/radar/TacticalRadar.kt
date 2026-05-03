package com.jamesfirstok.aegis.radar

import android.location.Location

class TacticalRadar {
    private val detectionRange = 5000.0 // مدى الرصد بالامتار (5 كم)

    fun calculateTargetVector(userLoc: Location, targetLoc: Location): Pair<Double, Double> {
        val distance = userLoc.distanceTo(targetLoc).toDouble()
        val bearing = userLoc.bearingTo(targetLoc).toDouble()
        
        return if (distance <= detectionRange) {
            Pair(distance, bearing)
        } else {
            Pair(0.0, 0.0) // خارج النطاق
        }
    }
}
