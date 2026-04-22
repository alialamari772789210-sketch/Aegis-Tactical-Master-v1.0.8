package com.jamesfirstok.aegis.model // توحيد المسار مع النواة الموحدة

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * AEGIS TACTICAL CONFIGURATION MANAGER v7.2.6
 * المسؤول عن إدارة الترددات السيادية والارتباط الفضائي المشفر
 * إشراف: العقيد المهندس علي العماري
 */
class ConfigManager(context: Context) {

    private val PREFS_NAME = "AegisSovereignConfig_v7"
    private val SATELLITE_LINK_KEY = "sat_link_active"
    private val RECON_FREQUENCY_KEY = "recon_freq_khz"
    private val DEFAULT_FREQ = 433000 // التردد القياسي للرصد SIGINT

    private val prefs: SharedPreferences

    init {
        // إنشاء مفتاح السيادة (Master Key) لتشفير الترددات في ذاكرة الجهاز
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        // الانتقال من التخزين العادي إلى التخزين السيادي المشفر
        this.prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * تفعيل الربط مع الأقمار الصناعية (Satellite Link)
     * يتم حفظ الحالة بتشفير AES لضمان عدم اختراق بروتوكول الاتصال
     */
    fun setSatelliteLink(active: Boolean) {
        prefs.edit().putBoolean(SATELLITE_LINK_KEY, active).apply()
    }

    /**
     * تحديث تردد الرصد التكتيكي (SIGINT Frequency)
     * يتيح للمنظومة التنقل بين الترددات لتجنب التشويش المعادي
     */
    fun updateFrequency(freq: Int) {
        prefs.edit().putInt(RECON_FREQUENCY_KEY, freq).apply()
    }

    /**
     * استدعاء التردد المخزن
     * في حال فشل الاستدعاء، يعود النظام تلقائياً للتردد الآمن (433MHz)
     */
    fun getStoredFrequency(): Int {
        return prefs.getInt(RECON_FREQUENCY_KEY, DEFAULT_FREQ)
    }

    /**
     * بروتوكول تطهير البيانات (Void-Zero Integration)
     * لمسح كافة الترددات المسجلة فور صدور أمر الطوارئ
     */
    fun clearSovereignData() {
        prefs.edit().clear().apply()
    }
}
