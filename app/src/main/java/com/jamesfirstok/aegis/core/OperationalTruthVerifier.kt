package com.jamesfirstok.aegis.core

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.jamesfirstok.aegis.ai.AegisAIAnalyzer
import com.scottyab.rootbeer.RootBeer
import java.io.File

/**
 * AEGIS Operational Truth Verifier v6.0
 * وظيفة: التحقق من نزاهة النظام وجاهزية العتاد قبل العمليات.
 */
class OperationalTruthVerifier(private val context: Context) {
    
    private val rootBeer = RootBeer(context)

    fun verifySystemIntegrity(): SystemIntegrityReport {
        val radar = checkRadarHardware()
        val security = checkSecurityIntegrity()
        val ai = checkAiCapabilities()
        
        // حساب الدرجة النهائية بناءً على الأوزان التكتيكية
        val score = calculateIntegrityScore(radar, security, ai)
        
        return SystemIntegrityReport(radar, security, ai, score)
    }

    /**
     * التحقق من العتاد: المكتبات الأصلية + حالة الراديو
     */
    private fun checkRadarHardware(): HardwareStatus {
        return try {
            // 1. التأكد من تحميل محرك التحييد (C++ Fusion)
            System.loadLibrary("aegis-core")
            
            // 2. التأكد من تفعيل الواي فاي للرصد الترددي
            val wifi = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            if (wifi?.isWifiEnabled == true) HardwareStatus.AVAILABLE else HardwareStatus.UNAVAILABLE
        } catch (e: UnsatisfiedLinkError) {
            Log.e("AEGIS_TRUTH", "Native Library Missing!")
            HardwareStatus.UNAVAILABLE
        } catch (e: Exception) {
            HardwareStatus.UNAVAILABLE
        }
    }

    /**
     * التحقق الأمني: كشف الـ Root والتلاعب بالنظام
     */
    private fun checkSecurityIntegrity(): SecurityStatus {
        val isRooted = rootBeer.isRooted || checkRootFilesManually()
        
        // إذا كان الجهاز Rooted، فهو COMPROMISED (غير آمن للعمليات)
        return if (isRooted) SecurityStatus.COMPROMISED else SecurityStatus.SECURE
    }

    private fun checkAiCapabilities(): AiStatus {
        return try {
            // محاولة أولية لتحميل نموذج الذكاء الاصطناعي
            AegisAIAnalyzer(context)
            AiStatus.OPERATIONAL
        } catch (e: Exception) {
            Log.e("AEGIS_TRUTH", "AI Model Failure: ${e.message}")
            AiStatus.DEGRADED
        }
    }

    private fun calculateIntegrityScore(radar: HardwareStatus, security: SecurityStatus, ai: AiStatus): Float {
        var score = 0f
        if (radar == HardwareStatus.AVAILABLE) score += 0.5f // الرادار هو الأهم
        if (security == SecurityStatus.SECURE) score += 0.3f  // الأمان حيوي
        if (ai == AiStatus.OPERATIONAL) score += 0.2f        // الذكاء الاصطناعي مكمل
        return score
    }

    private fun checkRootFilesManually(): Boolean {
        val paths = arrayOf("/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su")
        return paths.any { File(it).exists() }
    }
}
