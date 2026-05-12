package com.jamesfirstok.aegis.service

import android.app.*
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import com.jamesfirstok.aegis.model.SecurityModel
import java.util.UUID

/**
 * AEGIS TACTICAL MESH SERVICE v2.0
 * المعالجة الشاملة لمشاكل البث، الطاقة، والأمان.
 */
class AegisService : Service() {

    private val binder = LocalBinder()
    private val securityModel = SecurityModel()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null

    // حفظ المراجع (Callbacks) لضمان الإيقاف الصحيح ومنع استنزاف البطارية
    private var activeAdvertisingSet: AdvertisingSet? = null
    private val advCallback = object : AdvertisingSetCallback() {
        override fun onAdvertisingSetStarted(set: AdvertisingSet?, p1: Int, status: Int) {
            if (status == ADVERTISE_SUCCESS) activeAdvertisingSet = set
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            processIncomingSignal(result)
        }
    }

    companion object {
        private const val TAG = "AegisTactical"
        private const val CHANNEL_ID = "AegisCore"
        private val TACTICAL_SERVICE_UUID = ParcelUuid(UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb"))
    }

    override fun onCreate() {
        super.onCreate()
        setupBluetooth()
        startForeground(1, buildNotification())
    }

    private fun setupBluetooth() {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        
        // التحقق من دعم الـ Extended Advertising والـ Coded PHY (المسافات الطويلة)
        if (bluetoothAdapter?.isLeCodedPhySupported == true) {
            advertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        }
        scanner = bluetoothAdapter?.bluetoothLeScanner
    }

    /**
     * إرسال SOS تكتيكي مصحح:
     * 1. معالجة حجم البيانات (ByteArray بدل String).
     * 2. التحقق من الصلاحيات والـ PHY.
     */
    fun startTacticalSos() {
        if (!checkRuntimePermissions()) return
        if (advertiser == null) return

        // 1. التشفير الخام (Direct ByteArray) لتوفير الحجم ومنع مشاكل الـ UTF-8
        val payload = "SOS|${System.currentTimeMillis() / 1000}".toByteArray()
        val encryptedData = securityModel.encryptRawBytes(payload)

        // 2. التحقق من حجم الـ Payload (BLE limit 31 bytes legacy / 255 extended)
        if (encryptedData.size > 251) {
            Log.e(TAG, "Payload exceeds BLE limit. Encryption overhead too high.")
            return
        }

        val params = AdvertisingSetParameters.Builder()
            .setLegacyMode(false) // استخدام Extended Advertising لزيادة السعة والمدى
            .setPrimaryPhy(BluetoothDevice.PHY_LE_1M)
            .setSecondaryPhy(BluetoothDevice.PHY_LE_CODED) // Coded PHY لاختراق العوائق
            .setInterval(AdvertisingSetParameters.INTERVAL_HIGH)
            .setTxPowerLevel(AdvertisingSetParameters.TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceData(TACTICAL_SERVICE_UUID, encryptedData)
            .build()

        advertiser?.startAdvertisingSet(params, data, null, null, null, advCallback)
    }

    /**
     * استقبال الإشارات بمعالجة آمنة للبيانات
     */
    private fun processIncomingSignal(result: ScanResult?) {
        val rawData = result?.scanRecord?.getServiceData(TACTICAL_SERVICE_UUID) ?: return
        
        // فحص الـ Anti-Replay وتكامل الحزمة (Packet Integrity)
        val decrypted = securityModel.decryptRawBytes(rawData)
        decrypted?.let {
            Log.d(TAG, "Tactical Mesh Signal Received: ${String(it)}")
            // هنا يتم تمرير البيانات لمحرك الـ Routing الخاص بالـ Mesh
        }
    }

    fun startListening() {
        if (!checkRuntimePermissions() || scanner == null) return

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.ScanModeLowLatency)
            .apply {
                if (bluetoothAdapter?.isLeCodedPhySupported == true) {
                    setPhy(ScanSettings.PHY_LE_ALL_SUPPORTED)
                }
            }.build()

        // استخدام مرجع الـ callback الفعلي للإيقاف لاحقاً
        scanner?.startScan(null, settings, scanCallback)
    }

    private fun checkRuntimePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        // الإيقاف الصحيح لجميع الموارد لمنع استهلاك البطارية (Battery Drain Fix)
        activeAdvertisingSet?.let { advertiser?.stopAdvertisingSet(advCallback) }
        scanner?.stopScan(scanCallback)
        super.onDestroy()
    }

    inner class LocalBinder : Binder() { fun getService() = this@AegisService }
    override fun onBind(intent: Intent?) = binder
    private fun buildNotification(): Notification = Notification.Builder(this, CHANNEL_ID)
        .setContentTitle("AEGIS TACTICAL MESH").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).build()
}
