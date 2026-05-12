package com.jamesfirstok.aegis

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.room.Room
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.jamesfirstok.aegis.core.AegisSystemOrchestrator
import com.jamesfirstok.aegis.security.SovereigntyVerifier
import com.jamesfirstok.aegis.service.AegisService
import com.jamesfirstok.aegis.service.RadarService

/**
 * AEGIS SOVEREIGN APPLICATION [FINAL MISSION STATE]
 * المصمم: العقيد المهندس علي العماري
 */
class AegisApplication : Application() {

    lateinit var sovereigntyVerifier: SovereigntyVerifier
    lateinit var orchestrator: AegisSystemOrchestrator
    lateinit var database: AegisDatabase

    override fun onCreate() {
        super.onCreate()

        // 1. بروتوكول السيادة (Sovereignty Check) - الدفاع الأول
        sovereigntyVerifier = SovereigntyVerifier(this)
        if (!sovereigntyVerifier.isSystemSecure()) {
            Log.e("AEGIS", "SECURITY ALERT: System Compromised. Critical functions locked.")
            // ملاحظة: هنا يمكن توجيه المستخدم لشاشة الطوارئ أو تفعيل المسح الذاتي
            return 
        }

        // 2. حقن نواة الذكاء الاصطناعي (Python Engine)
        initializePython()

        // 3. تهيئة البيانات التكتيكية (Tactical Storage)
        database = Room.databaseBuilder(
            this,
            AegisDatabase::class.java,
            "aegis_tactical_db"
        ).fallbackToDestructiveMigration().build()

        // 4. تعيين قائد المنظومة وتفعيل النواة
        orchestrator = AegisSystemOrchestrator(this)
        orchestrator.initializeTacticalCore()

        // 5. إطلاق الخدمات الميدانية (Foreground Services)
        // هذه الخدمات تضمن بقاء الرادار والتحييد فعالين في أصعب الظروف
        startCoreServices()

        Log.i("AEGIS", "Aegis Sovereign Core v7.3.0 - Operational ✅")
    }

    private fun initializePython() {
        try {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(this))
            }
        } catch (e: Exception) {
            Log.e("AEGIS", "Python Engine Failure: ${e.message}")
        }
    }

    private fun startCoreServices() {
        try {
            startService(Intent(this, AegisService::class.java))
            startService(Intent(this, RadarService::class.java))
            Log.i("AEGIS", "Radar and Security Services deployed.")
        } catch (e: Exception) {
            Log.e("AEGIS", "Service Deployment Failed: ${e.message}")
        }
    }
}
