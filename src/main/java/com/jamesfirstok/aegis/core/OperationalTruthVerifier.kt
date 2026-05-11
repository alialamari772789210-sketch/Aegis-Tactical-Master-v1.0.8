package com.jamesfirstok.aegis.core

import android.content.Context
import android.hardware.SensorManager
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.jamesfirstok.aegis.ai.AegisAIAnalyzer
import com.jamesfirstok.aegis.radar.TacticalRadar
import java.io.File

class OperationalTruthVerifier(private val context: Context) {

    data class ComponentStatus(
        val name: String,
        val status: String,
        val details: String
    )

    fun verifySovereigntyTruth(): List<ComponentStatus> {
        val report = mutableListOf<ComponentStatus>()

        // 1. رادار ومعالجة الإشارة (Native + Wi‑Fi)
        report.add(checkRadarAndRadio())

        // 2. الذكاء الاصطناعي (TFLite model)
        report.add(checkAiAutonomy())

        // 3. التحييد والتشويش (Root & tools)
        report.add(checkJammingCapability())

        // 4. اختراق الجهاز وصلاحيات متقدمة
        report.add(checkPrivilegeEscalation())

        // 5. مكتبات DSP (JTransforms)
        report.add(checkDspLibraries())

        // 6. مستشعرات الحركة والتوجيه (ضرورية للدمج)
        report.add(checkMotionSensors())

        // 7. صلاحيات الموقع (مسح Wi‑Fi)
        report.add(checkLocationPermission())

        return report
    }

    // -----------------------------------------------------------
    private fun checkRadarAndRadio(): ComponentStatus {
        return try {
            // Test loading native lib
            System.loadLibrary("aegis-core")
            val radar = TacticalRadar()
            // Simple test with fixed signal to ensure JNI works
            val testSignal = DoubleArray(64) { 1.0 }
            val result = radar.processSignal(testSignal)
            if (result.isNotEmpty()) {
                // Also check if Wi‑Fi is available
                val wifi = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                if (wifi.isWifiEnabled) {
                    ComponentStatus("رادار وراديو", "✅ حقيقي", "مكتبة C++ محملة و Wi‑Fi نشط")
                } else {
                    ComponentStatus("رادار وراديو", "⚠️ محدود", "المكتبة محملة لكن Wi‑Fi غير مفعل")
                }
            } else {
                ComponentStatus("رادار وراديو", "⚠️ غير مكتمل", "المكتبة محملة لكن المعالجة لم تنتج بيانات")
            }
        } catch (e: UnsatisfiedLinkError) {
            ComponentStatus("رادار وراديو", "❌ معطل", "مكتبة aegis-core غير موجودة")
        } catch (e: Exception) {
            ComponentStatus("رادار وراديو", "❌ خطأ", "فشل في الاتصال: ${e.message}")
        }
    }

    // -----------------------------------------------------------
    private fun checkAiAutonomy(): ComponentStatus {
        return try {
            val analyzer = AegisAIAnalyzer(context)
            // Feed dummy data to ensure interpreter is allocated
            val dummyInput = FloatArray(64) { 0.5f }
            val prediction = analyzer.analyzeThreat(dummyInput)
            if (prediction.classId in 0..3) {
                ComponentStatus("الذكاء الاصطناعي", "✅ حقيقي", "نموذج TFLite محمل ويتجاوب (class=${prediction.classId})")
            } else {
                ComponentStatus("الذكاء الاصطناعي", "⚠️ غير معتاد", "النموذج يعمل لكن المخرجات غير متوقعة")
            }
        } catch (e: Exception) {
            ComponentStatus("الذكاء الاصطناعي", "❌ فشل", "فشل تحميل النموذج: ${e.message}")
        }
    }

    // -----------------------------------------------------------
    private fun checkJammingCapability(): ComponentStatus {
        val isRooted = checkRootMethod()
        val hasAireplay = arrayOf(
            "/data/data/com.termux/files/usr/bin/aireplay-ng",
            "/usr/bin/aireplay-ng",
            "/system/bin/aireplay-ng"
        ).any { File(it).exists() }
        val canMonitorMode = try {
            // check if wlan0 can be set to monitor (requires root)
            val process = Runtime.getRuntime().exec("su -c 'iw dev wlan0 info'")
            process.waitFor()
            process.inputStream.bufferedReader().readText().contains("type monitor")
        } catch (e: Exception) { false }

        return when {
            isRooted && hasAireplay -> ComponentStatus("التحييد والتشويش", "✅ حقيقي", "صلاحيات روت + aireplay-ng متاحة للتشويش الهجومي")
            isRooted && !hasAireplay -> ComponentStatus("التحييد والتشويش", "⚠️ جزئي", "روت موجود لكن aireplay-ng غير منصّب")
            canMonitorMode -> ComponentStatus("التحييد والتشويش", "⚠️ جزئي", "وضع مراقبة متاح دون airplay")
            else -> ComponentStatus("التحييد والتشويش", "⚠️ محدود", "سيتم استخدام إغراق الشبكة فقط")
        }
    }

    // -----------------------------------------------------------
    private fun checkPrivilegeEscalation(): ComponentStatus {
        val isRooted = checkRootMethod()
        val hasLocationHardware = context.checkCallingOrSelfPermission(
            "android.permission.LOCATION_HARDWARE"
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return when {
            isRooted -> ComponentStatus("الاختراق والتحصين", "✅ حقيقي", "صلاحيات روت متاحة، تجاوز كامل للقيود")
            hasLocationHardware -> ComponentStatus("الاختراق والتحصين", "⚠️ جزئي", "صلاحية LOCATION_HARDWARE ممنوحة")
            else -> ComponentStatus("الاختراق والتحصين", "⚠️ صندوق رمل", "صلاحيات المستخدم العادي")
        }
    }

    // -----------------------------------------------------------
    private fun checkDspLibraries(): ComponentStatus {
        return try {
            Class.forName("org.jtransforms.fft.DoubleFFT_1D")
            ComponentStatus("مكتبات DSP", "✅ حقيقي", "JTransforms مثبتة وجاهزة")
        } catch (e: Exception) {
            ComponentStatus("مكتبات DSP", "❌ وهمي", "JTransforms غير موجودة في المسار")
        }
    }

    // -----------------------------------------------------------
    private fun checkMotionSensors(): ComponentStatus {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accel = sm.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)
        val gyro = sm.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE)
        val mag = sm.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD)

        val present = listOf(accel, gyro, mag).count { it != null }
        return when {
            present >= 3 -> ComponentStatus("مستشعرات الحركة", "✅ حقيقي", "تسارع + جيروسكوب + مغناطيسي")
            present >= 2 -> ComponentStatus("مستشعرات الحركة", "⚠️ جزئي", "بعض المستشعرات متاحة")
            else -> ComponentStatus("مستشعرات الحركة", "❌ ناقص", "مستشعرات غير كافية")
        }
    }

    // -----------------------------------------------------------
    private fun checkLocationPermission(): ComponentStatus {
        val fine = context.checkCallingOrSelfPermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = context.checkCallingOrSelfPermission(
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        return when {
            fine -> ComponentStatus("صلاحية الموقع", "✅ دقيقة", "GPS و Wi‑Fi مسموح")
            coarse -> ComponentStatus("صلاحية الموقع", "⚠️ تقريبية", "مسموح فقط موقع تقريبي")
            else -> ComponentStatus("صلاحية الموقع", "❌ ممنوع", "لا صلاحيات موقع")
        }
    }

    // -----------------------------------------------------------
    private fun checkRootMethod(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/bin/su"
        )
        return paths.any { File(it).exists() }
    }
}
