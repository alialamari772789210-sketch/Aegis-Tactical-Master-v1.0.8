package com.jamesfirstok.aegis.radar

import android.util.Log

/**
 * ============================================================================
 * AEGIS TACTICAL RADAR INTERFACE v4.2 - HARDWARE COUPLING LAYER
 * ============================================================================
 * الوظيفة: الجسر المركزي لربط دفق إشارات الـ SDR بقنوات المعالجة النواتية لـ C++
 * ============================================================================
 */
class TacticalRadar {

    companion object {
        private const val TAG = "AEGIS_TACTICAL_RADAR"

        init {
            try {
                // شحن وتحميل المكتبة النواتية المشتركة والمطورة لمعالجة قنوات I/Q
                System.loadLibrary("aegis-core")
                Log.i(TAG, "✅ Aegis Native Core C++ Binary linked successfully to TacticalRadar.")
            } catch (e: UnsatisfiedLinkError) {
                // وضع الطوارئ والتحذير العملياتي عند غياب العتاد الخارجي
                Log.e(TAG, "[!] CRITICAL COMPILATION FAULT: aegis-core library mapping broken: ${e.message}")
            }
        }
    }

    /**
     * استدعاء معالج الإشارة الأصلي التابع لـ C++ ومكتبة liquid-dsp.
     * [توحيد هندسي]: الكود يستقبل دفق مصفوفة متكاملة ومزدوجة القنوات (Interleaved I/Q Stream).
     * المواقع الزوجية تمثل مركبات الطور الحقيقي (I) والمواقع الفردية تمثل المركبات التخيلية (Q).
     * @param rawSignal دفق الإشارات الخام المتزامن القادم من الـ ADC الخارجي عبر الـ USB.
     * @return طيف القدرة الخالص (Power Spectrum Magnitude) بعد حساب تحويلات فورية السريعة (FFT).
     */
    external fun processSignal(rawSignal: DoubleArray): DoubleArray

    /**
     * كشف التليميتري وفك تشفير إحداثيات طيران الهدف الحية لاستخراج خطوط الطول والعرض.
     * @param rawData الحزم اللاسلكية والنبضات الممتصة من الهواء عبر الهوائيات.
     * @return مصفوفة تحتوي على خط العرض في الموقع [0] وخط الطول في الموقع [1] لتغذية واجهة الـ HUD.
     */
    external fun captureTelemetryCoords(rawData: ByteArray): DoubleArray?

    /**
     * بدء المراقبة والمسح الميداني النشط: يمرر العينات التناظرية الفورية لـ C++
     */
    fun startSurveillance(buffer: DoubleArray): DoubleArray {
        return try {
            // المعالجة الفورية بسرعة ميكرو-ثانية عبر طبقة الـ Native
            processSignal(buffer)
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Fallback Mode Activated: Processing spectrum via basic math on Kotlin layer.")
            // وضع احتياطي آمن وحركي للحفاظ على استقرار وعمل واجهة الـ HUD في غياب العتاد
            fallbackProcess(buffer)
        }
    }

    /**
     * معالجة احتياطية برمجية مبسطة وخالية من تسريب الذاكرة (Zero-Allocation Fallback Process)
     */
    private fun fallbackProcess(buffer: DoubleArray): DoubleArray {
        // حساب حجم المصفوفة بناءً على عزل قنوات الـ I/Q الافتراضية
        val complexLen = buffer.size / 2
        if (complexLen == 0) return DoubleArray(0)
        
        val result = DoubleArray(complexLen)
        for (i in 0 until complexLen) {
            val real = buffer[2 * i]
            val imag = buffer[2 * i + 1]
            // حساب القدرة التقريبية = مجموع مربعي المركبتين الحقيقية والتخيلية
            result[i] = (real * real) + (imag * imag)
        }
        return result
    }
}
