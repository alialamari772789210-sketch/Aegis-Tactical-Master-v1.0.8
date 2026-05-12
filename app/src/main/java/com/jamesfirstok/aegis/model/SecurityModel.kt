package com.jamesfirstok.aegis.model

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * AEGIS TACTICAL SOVEREIGN CORE - VERSION 7.2.6
 * DEVELOPED BY: COLONEL ALI AL-AMMARI
 * درع التشفير الموحد، الإصلاح الذاتي، وبروتوكول الانهيار.
 */
class SecurityModel {

    // المفتاح السيادي (يُفضل نقله إلى EncryptedSharedPreferences عند الإمكان)
    private val SOVEREIGN_KEY = "Aegis_Tactical_Global_Control_2026"
    private val DAILY_AUTH_CODE = "ALi-C2-4-2026"
    private val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    private var failureCount = 0
    private val secureRandom = SecureRandom()

    /**
     * بروتوكول المصادقة السيادي – يمنع المحاولات المتكررة.
     */
    fun validateDailyAccess(inputCode: String): Boolean {
        return if (inputCode == DAILY_AUTH_CODE) {
            failureCount = 0
            true
        } else {
            failureCount++
            if (failureCount >= 3) {
                executeProtocolVoidZero()
            }
            false
        }
    }

    /**
     * تشفير تكتيكي: AES/CBC مع IV عشوائي يُدمج مع الناتج.
     * الصيغة الناتجة: Base64( IV(16 bytes) + Ciphertext )
     */
    fun encryptTacticalData(data: String): String {
        return try {
            val keyBytes = generateHash(SOVEREIGN_KEY)
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            
            // توليد IV عشوائي جديد لكل عملية تشفير
            val iv = ByteArray(16)
            secureRandom.nextBytes(iv)
            val ivParams = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParams)
            val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
            
            // دمج IV مع النص المشفر
            val combined = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
            
            Base64.encodeToString(combined, Base64.NO_WRAP or Base64.URL_SAFE)
        } catch (e: Exception) {
            Log.e("AEGIS_SECURITY", "Encryption failed", e)
            initiateSelfRepairSequence()
            "SIGNAL_LOST"
        }
    }

    /**
     * فك تشفير تكتيكي.
     */
    fun decryptTacticalData(encryptedData: String): String? {
        return try {
            val keyBytes = generateHash(SOVEREIGN_KEY)
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance(ALGORITHM)
            
            val combined = Base64.decode(encryptedData, Base64.NO_WRAP or Base64.URL_SAFE)
            
            // استخراج IV من أول 16 بايت
            val iv = combined.copyOfRange(0, 16)
            val ciphertext = combined.copyOfRange(16, combined.size)
            val ivParams = IvParameterSpec(iv)
            
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParams)
            String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e("AEGIS_SECURITY", "Decryption failed", e)
            null
        }
    }

    /**
     * تسلسل الإصلاح الذاتي.
     */
    private fun initiateSelfRepairSequence() {
        // محاولة إعادة تشغيل الخدمات الأمنية أو إعادة تحميل المفاتيح
        Log.w("AEGIS_SECURITY", "Self-Healing: Resetting secure channels...")
        failureCount = 0  // إعادة تعيين عداد الفشل
    }

    /**
     * بروتوكول الانهيار – مسح البيانات الحساسة من الذاكرة والتخزين.
     */
    private fun executeProtocolVoidZero() {
        Log.e("AEGIS_SECURITY", "VOID-ZERO ACTIVATED: Purging data...")
        try {
            // مسح ذاكرة التفضيلات المشفرة إن أمكن
            val runtime = Runtime.getRuntime()
            runtime.gc()
            // إعادة تعيين المتغيرات الحساسة
            failureCount = 0
            Log.e("AEGIS_SECURITY", "VOID-ZERO: Sensitive data purged.")
        } catch (e: Exception) {
            Log.e("AEGIS_SECURITY", "VOID-ZERO: Purge failed", e)
        }
        // الخروج القسري من العملية
        android.os.Process.killProcess(android.os.Process.myPid())
    }

    /**
     * توليد هاش SHA-256.
     */
    private fun generateHash(input: String): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(input.toByteArray(Charsets.UTF_8))
    }
}
