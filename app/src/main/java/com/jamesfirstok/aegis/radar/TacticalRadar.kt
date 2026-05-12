package com.jamesfirstok.aegis.radar

class TacticalRadar {

    companion object {
        init {
            try {
                System.loadLibrary("aegis-core")
            } catch (e: UnsatisfiedLinkError) {
                // المكتبة غير موجودة، سيتم استخدام المعالجة البرمجية الاحتياطية
                android.util.Log.e("TacticalRadar", "aegis-core library not found: ${e.message}")
            }
        }
    }

    /**
     * استدعاء معالج الإشارة الأصلي (C++/liquid-dsp).
     * @param rawSignal الإشارة الخام (قيم حقيقية).
     * @return طيف القدرة بعد FFT والكشف.
     */
    external fun processSignal(rawSignal: DoubleArray): DoubleArray

    /**
     * بدء المراقبة: يمرر البيانات الخام من Wi‑Fi/SDR إلى نواة C++.
     */
    fun startSurveillance(buffer: DoubleArray): DoubleArray {
        return try {
            processSignal(buffer)
        } catch (e: UnsatisfiedLinkError) {
            // وضع احتياطي: إذا لم توجد المكتبة، نستخدم معالجة برمجية بسيطة
            fallbackProcess(buffer)
        }
    }

    /**
     * معالجة احتياطية بدون المكتبة الأصلية.
     * تحسب متوسط الطاقة لكل عينة (يبقي النظام عاملاً).
     */
    private fun fallbackProcess(buffer: DoubleArray): DoubleArray {
        val result = DoubleArray(buffer.size)
        for (i in buffer.indices) {
            // قدرة بسيطة = مربع القيمة
            result[i] = buffer[i] * buffer[i]
        }
        return result
    }
}
