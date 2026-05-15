package com.jamesfirstok.aegis.manager

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.jamesfirstok.aegis.core.HardwareBypassEngine
import com.jamesfirstok.aegis.model.SecurityModel
import com.jamesfirstok.aegis.tactical.RadioAcquisitionProcessor
import java.io.File

/**
 * ============================================================================
 * AEGIS INDEPENDENT OPERATIONS MANAGER v5.2 - COMMAND & CONTROL
 * ============================================================================
 * إدارة العمليات المستقلة، توحيد نبضات الرصد، وبروتوكولات التطهير النهائي المادي
 * ============================================================================
 */
class IndependentOpsManager(
    private val context: Context,
    private val securityModel: SecurityModel,
    private val bypassEngine: HardwareBypassEngine
) {
    private var currentMissionStatus = "STANDBY"
    
    // [تصحيح هندسي حاسم]: استدعاء الـ WifiManager وتمريره للمشيد الموحد لمعالج الالتقاط اللاسلكي
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val radioProcessor = RadioAcquisitionProcessor(context, wifiManager)

    /**
     * بدء عملية الاستطلاع المستقلة وتفعيل وضع الأداء الأقصى للبطارية والعتاد
     */
    fun startAutonomousRecon(isSdrActive: Boolean, rawSdrBuffer: DoubleArray? = null) {
        currentMissionStatus = "ACTIVE_RECON"
        
        // [تصحيح الدالة المتناقضة]: تفعيل وضع الاستيقاظ عالي الكثافة للـ WakeLock
        bypassEngine.engageOperationalMode()
        
        Log.i("AEGIS_OPS", "Autonomous Recon In-Progress. Mode [SDR:$isSdrActive]")
        initiateLocalSignalAnalysis(isSdrActive, rawSdrBuffer)
    }

    private fun initiateLocalSignalAnalysis(isSdrActive: Boolean, rawSdrBuffer: DoubleArray?) {
        try {
            // [تصحيح سيل البيانات]: تمرير بارامترات الوضع الهجين لضمان عدم تعمية رقاقات الـ SDR
            val targets = radioProcessor.executeFullScan(isSdrActive, rawSdrBuffer)
            
            if (targets.isNotEmpty()) {
                val topThreat = targets.maxByOrNull { it.threatScore }
                
                if (topThreat != null && topThreat.threatScore > 0.85f) {
                    Log.w("AEGIS_OPS", "⚠️ [CRITICAL LOCK] Core threat verified: Score ${topThreat.threatScore}")
                    currentMissionStatus = "CRITICAL"
                    triggerCriticalDefense()
                }
            }
        } catch (e: Exception) {
            Log.e("AEGIS_OPS", "Signal Analysis Failure: ${e.message}")
        }
    }

    /**
     * بروتوكول VOID-ZERO: التدمير الذاتي الرقمي ومسح الآثار الفيزيائية من الذاكرة والتخزين الصلب
     */
    fun triggerCriticalDefense() {
        if (currentMissionStatus != "CRITICAL") return

        Log.e("AEGIS_OPS", "!!! EXECUTING VOID-ZERO COGNITIVE PURGE PROTOCOL !!!")

        try {
            // 1. التدمير المنظم والمضمون للمجلدات التكتيكية وقواعد البيانات المحلية المخزنة
            val baseDir = context.filesDir.parentFile
            val targetFolders = listOf("databases", "shared_prefs", "no_backup")
            
            targetFolders.forEach { folderName ->
                val folder = File(baseDir, folderName)
                if (folder.exists()) {
                    folder.deleteRecursively()
                    Log.d("AEGIS_OPS", "[VOID-ZERO] Purged physical sector: $folderName")
                }
            }

            // 2. [تصحيح الروابط]: استدعاء دالة التطهير المادي والتصفيري الموحدة للـ RAM والقتل الفوري للعملية
            currentMissionStatus = "TERMINATED"
            securityModel.executeProtocolVoidZero()
            
        } catch (e: Exception) {
            Log.e("AEGIS_OPS", "VOID-ZERO Emergency Fallback Loop Failure: ${e.message}")
            // خروج اضطراري نهائي لمنع المهاجم من تعطيل القتل الفوري
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }

    fun getOpsStatus(): String = currentMissionStatus
}
