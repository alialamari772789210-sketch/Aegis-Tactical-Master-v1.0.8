package com.jamesfirstok.aegis

import android.app.Application
import android.util.Log
import androidx.room.Room
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.jamesfirstok.aegis.security.SovereigntyVerifier
import com.jamesfirstok.aegis.core.AegisSystemOrchestrator

/**
 * AEGIS APPLICATION CORE [OPERATIONAL STATE]
 * وظيفة: تشغيل النواة المستقلة وإدارة السيادة الرقمية فور الإقلاع.
 */
class AegisApplication : Application() {
    lateinit var sovereigntyVerifier: SovereigntyVerifier
    lateinit var database: AegisDatabase
    
    override fun onCreate() {
        super.onCreate()
        
        // 1. بدء بروتوكول التحقق من السيادة (الحماية من الاختراق الداخلي)
        sovereigntyVerifier = SovereigntyVerifier(this)
        if (!sovereigntyVerifier.isSystemSecure()) {
            Log.e("AEGIS", "SECURITY ALERT: System integrity compromised!")
            // هنا يمكن تفعيل بروتوكول VOID-ZERO للمسح الذاتي
        }

        // 2. حقن محرك بايثون (نواة الذكاء الاصطناعي المستقلة)
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        // 3. تهيئة قاعدة البيانات التكتيكية (تخزين بصمات الترددات المرصودة)
        database = Room.databaseBuilder(
            this,
            AegisDatabase::class.java,
            "aegis_tactical_db"
        ).fallbackToDestructiveMigration().build()

        Log.i("AEGIS", "Aegis Sovereign Core v7.2.6 - Initialized Successfully ✅")
    }
}
