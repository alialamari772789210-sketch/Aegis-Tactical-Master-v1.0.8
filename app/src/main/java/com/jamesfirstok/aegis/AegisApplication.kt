package com.jamesfirstok.aegis

import android.app.Application
import android.util.Log

/**
 * AEGIS SYSTEM APPLICATION CORE
 * هذا الكلاس هو أول ما يتم تشغيله عند فتح التطبيق.
 * وظيفته تهيئة المكتبات الأمنية ومنع الانهيار المفاجئ.
 */
class AegisApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        Log.i("AEGIS_SYSTEM", "Starting Aegis Tactical Sovereign Core...")

        // محاولة تحميل المكتبة الأصلية بأمان
        try {
            // تحميل مكتبة الأمان المركزية
            System.loadLibrary("aegis-security-lib")
            Log.i("AEGIS_SYSTEM", "Native Security Library loaded successfully.")
        } catch (e: UnsatisfiedLinkError) {
            // في حال فشل التحميل، لا ينهار التطبيق بل يطبع الخطأ
            Log.e("AEGIS_SYSTEM", "CRITICAL ERROR: Native library 'aegis-security-lib' not found!")
            Log.e("AEGIS_SYSTEM", "Reason: ${e.message}")
            Log.w("AEGIS_SYSTEM", "System will continue in FALLBACK LITE mode.")
        } catch (e: Exception) {
            Log.e("AEGIS_SYSTEM", "Unexpected error during initialization: ${e.message}")
        }
    }
}
