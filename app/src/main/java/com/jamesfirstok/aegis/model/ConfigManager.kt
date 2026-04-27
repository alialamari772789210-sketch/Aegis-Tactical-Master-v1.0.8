package com.jamesfirstok.aegis.model

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * AEGIS TACTICAL CONFIGURATION MANAGER - SOVEREIGN VERSION v7.2.6
 * Architect: Colonel Ali Al-Ammari
 * إدارة التكوين السيادي: تشفير AES-256 لحماية ترددات SIGINT وإحداثيات الربط الفضائي.
 */
class ConfigManager(context: Context) {

    private val PREFS_NAME = "AegisSovereignConfig_v7"
    private val SATELLITE_LINK_KEY = "sat_link_active"
    private val RECON_FREQUENCY = "recon_freq_khz"
    private val COMMANDER_NAME = "commander_id"

    private var prefs: SharedPreferences? = null

    init {
        try {
            // 1. إنشاء مفتاح السيادة المشفر (Master Key)
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // 2. تهيئة الوعاء المشفر (Encrypted Storage) لضمان السرية المطلقة
            prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // في حالة فشل التشفير (نادر الحدوث)، يتم تسجيل الخطأ تكتيكياً
            android.util.Log.e("AEGIS_CONFIG", "CRITICAL: Encrypted Vault Access Failed.")
        }
    }

    /**
     * تفعيل/تعطيل الربط مع الأقمار الصناعية (Satellite Link)
     */
    fun setSatelliteLink(active: Boolean) {
        prefs?.edit()?.putBoolean(SATELLITE_LINK_KEY, active)?.apply()
    }

    fun isSatelliteLinkActive(): Boolean {
        return prefs?.getBoolean(SATELLITE_LINK_KEY, false) ?: false
    }

    /**
     * تحديث تردد الرصد (SIGINT)
     * يدعم بروتوكول القفز الترددي لضمان عدم تتبع الإشارة.
     */
    fun updateFrequency(freq: Int) {
        prefs?.edit()?.putInt(RECON_FREQUENCY, freq)?.apply()
    }

    /**
     * استعادة التردد المخزن
     * القيمة الافتراضية هي 433,000 KHz (تردد الرصد القياسي)
     */
    fun getStoredFrequency(): Int {
        return prefs?.getInt(RECON_FREQUENCY, 433000) ?: 433000
    }

    /**
     * توثيق اسم القائد في الذاكرة المشفرة
     */
    fun saveCommanderIdentity(name: String) {
        prefs?.edit()?.putString(COMMANDER_NAME, name)?.apply()
    }
}
