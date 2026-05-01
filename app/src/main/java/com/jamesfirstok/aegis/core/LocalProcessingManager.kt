package com.jamesfirstok.aegis.core

class LocalProcessingManager {

    fun analyzeSignal(signal: Float): String {
        return if (signal > 50f) {
            "HIGH SIGNAL"
        } else {
            "LOW SIGNAL"
        }
    }
}
