package com.jamesfirstok.aegis.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * ============================================================================
 * AEGIS TACTICAL CONFIGURATION MANAGER - SOVEREIGN VERSION v7.2.8
 * ============================================================================
 * إدارة التكوين السيادي: تشفير محصن ثنائي النطاق لحماية ترددات SIGINT وإحداثيات السيطرة
 * ============================================================================
 */
class ConfigManager(context: Context) {

    companion object {
        private const val TAG = "AEGIS_CONFIG"
        private const val PREFS_NAME = "AegisSovereignConfig_v7"
        private const val SATELLITE_LINK_KEY = "sat_link_active"
        private const val RECON_FREQUENCY = "recon_freq_mhz" // توحيد القياس العالمي بالميجاهرتز MHz
        private const val COMMANDER_NAME = "commander_id"
    }

    private lateinit var prefs: SharedPreferences

    init {
        try {
            // 1. إنشاء مفتاح السيادة العتادي المشفر عبر الـ KeyStore الخاص بالنواة
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // 2. تهيئة وعاء التخزين المشفر عتادياً لمنع سحب الملفات
            prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            Log.i(TAG, "✅ Encrypted SharedPreferences initialized via Hardware Keystore.")
        } catch (e: Exception) {
            Log.e(TAG, "[CRITICAL] Cryptographic Hardware Keystore failed: ${e.message}")
            
            // [حل ثغرة الانهيار]: الانتقال التلقائي الفوري لمستودع برمجيات محلي آمن كوضع طوارئ لمنع عمى الترددات
            prefs = context.getSharedPreferences("AegisFallbackSovereignConfig", Context.MODE_PRIVATE)
            Log.w(TAG, "[!] Fallback Mode Activated: Storing tactical data in local emergency cache.")
        }
    }

    fun setSatelliteLink(active: Boolean) {
        prefs.edit().putBoolean(SATELLITE_LINK_KEY, active).apply()
    }

    fun isSatelliteLinkActive(): Boolean {
        return prefs.getBoolean(SATELLITE_LINK_KEY, false)
    }

    fun updateFrequency(freqMhz: Int) {
        prefs.edit().putInt(RECON_FREQUENCY, freqMhz).apply()
        Log.d(TAG, "[SIGINT] Tactical Frequency update cached: $freqMhz MHz")
    }

    fun getStoredFrequency(): Int {
        return prefs.getInt(RECON_FREQUENCY, 433) ?: 433 // التردد الافتراضي النطاقي الموحد 433 MHz
    }

    fun saveCommanderIdentity(name: String) {
        prefs.edit().putString(COMMANDER_NAME, name).apply()
    }
}
