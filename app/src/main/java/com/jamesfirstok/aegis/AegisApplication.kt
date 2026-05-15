package com.jamesfirstok.aegis

import android.app.Application
import android.content.Intent
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.jamesfirstok.aegis.core.AegisSystemOrchestrator
import com.jamesfirstok.aegis.security.SovereigntyVerifier
import com.jamesfirstok.aegis.service.AegisService

class AegisApplication : Application() {
    lateinit var sovereigntyVerifier: SovereigntyVerifier
    lateinit var orchestrator: AegisSystemOrchestrator

    override fun onCreate() {
        super.onCreate()
        sovereigntyVerifier = SovereigntyVerifier(this)
        if (!sovereigntyVerifier.isSystemSecure()) {
            Log.e("AEGIS", "CRITICAL SECURITY ALERT: Tampering located."); return
        }
        try { if (!Python.isStarted()) Python.start(AndroidPlatform(this)) } catch (e: Exception) {}
        orchestrator = AegisSystemOrchestrator(this)
        orchestrator.initializeTacticalCore()
        try { startService(Intent(this, AegisService::class.java)) } catch (e: Exception) {}
    }
}
