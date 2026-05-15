package com.jamesfirstok.aegis.core

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.location.GnssStatus
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.jamesfirstok.aegis.model.SecurityModel
import com.jamesfirstok.aegis.tactical.RadioAcquisitionProcessor
import kotlinx.coroutines.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.roundToInt

/**
 * ============================================================================
 * AEGIS TACTICAL RUNTIME & LINK SERVICE v10.6 - SECURE MESH MIGRATED
 * ============================================================================
 * الخدمة الخلفية السيادية الموحدة: إدارة قنوات الـ UDP المشفرة، التبادل الطيفي والـ GNSS
 * المصمم: العقيد المهندس علي العماري
 * ============================================================================
 */
class AegisOperationalService : Service() {

    companion object {
        private const val TAG = "AEGIS_RUNTIME_SERVICE"
        private const val CHANNEL_ID = "AEGIS_SOVEREIGN_OPS" // [توحيد المعرف] متوافق مع المانيفست
        private const val NOTIFICATION_ID = 77

        private const val UDP_PORT = 5555
        private const val DISCOVERY_GROUP = "239.8.8.8"

        private const val HEARTBEAT_INTERVAL = 5000L
        private const val DISCOVERY_INTERVAL = 10000L
        private const val MAX_PACKET_SIZE = 2048
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var securityModel: SecurityModel
    private lateinit var radioProcessor: RadioAcquisitionProcessor
    private lateinit var engagementEngine: TacticalEngagementEngine

    private var wifiManager: WifiManager? = null
    private var locationManager: LocationManager? = null

    // متغيرات الملاحة وتأمين الموقع الجغرافي
    @Volatile private var gpsTime: Long = 0L
    @Volatile private var lastLocation: Location? = null
    @Volatile private var satelliteCount: Int = 0
    @Volatile private var isSdrConnected = false

    // مقابس الشبكة والميكانيكا المشفرة
    private var tacticalSocket: DatagramSocket? = null
    private var multicastSocket: MulticastSocket? = null
    private val outgoingQueue = ConcurrentLinkedQueue<String>()

    // =========================================================================
    // GNSS Status Listener & Callback
    // =========================================================================
    private val gnssCallback = object : GnssStatus.Callback() {
        override fun onFirstFix(ttffMillis: Int) {
            Log.i(TAG, "[GNSS] First lock acquired in: ${ttffMillis}ms")
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            satelliteCount = status.satelliteCount
            gpsTime = System.currentTimeMillis()
            Log.d(TAG, "[GNSS_STREAM] Satellites=$satelliteCount | Unix Stamp=$gpsTime")
        }
    }

    private val locationListener = LocationListener { location ->
        lastLocation = location
    }

    // =========================================================================
    // الوعاء التكتيكي للتهيئة والتشغيل (Lifecycle)
    // =========================================================================
    override fun onCreate() {
        super.onCreate()
        securityModel = SecurityModel()
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        radioProcessor = RadioAcquisitionProcessor(this, wifiManager!!)
        engagementEngine = TacticalEngagementEngine(this)

        createNotificationChannel()

        startForeground(
            NOTIFICATION_ID,
            buildNotification("Operational Tactical Core Active")
        )

        initializeSocketsSecurely()
        initializeGnssHardware()
        startOperationalLoops()

        Log.i(TAG, "🔒 Aegis Operational Link Service fully synchronized.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            isSdrActive = it.getBooleanExtra("IS_SDR_ACTIVE", false)
            val targetUsbDevice = it.getParcelableExtra<UsbDevice>("USB_DEVICE")
            engagementEngine.setOperationMode(isSdrActive, targetUsbDevice)
        }
        Log.i(TAG, "Tactical loop refresh executed successfully.")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // =========================================================================
    // تهيئة المقابس المشفرة عسكرياً (Socket Initialization)
    // =========================================================================
    private fun initializeSocketsSecurely() {
        try {
            // [حل ثغرة التضارب أندرويد 14+]: فصل المنافذ ومشاركة العنوان بأمان
            tacticalSocket = DatagramSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(UDP_PORT))
                broadcast = true
            }

            multicastSocket = MulticastSocket(null).apply {
                reuseAddress = true
                bind(InetSocketAddress(UDP_PORT)) // مشاركة المنفذ لمنع انهيار الـ Datagram Socket
            }

            // الانضمام الحركي للمجموعة المشفرة
            val groupAddress = InetAddress.getByName(DISCOVERY_GROUP)
            val groupSocketAddress = InetSocketAddress(groupAddress, UDP_PORT)
            val networkInterface = NetworkInterface.getByName("wlan0") ?: NetworkInterface.getNetworkInterfaces().nextElement()
            
            multicastSocket?.joinGroup(groupSocketAddress, networkInterface)
            Log.i(TAG, "[NET] Secure Tactical Mesh Ports linked without collisions.")
        } catch (e: Exception) {
            Log.e(TAG, "Socket initialization failure: ${e.message}")
        }
    }

    private fun initializeGnssHardware() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "GNSS Driver halt: location permission missing.")
            return
        }
        try {
            locationManager?.registerGnssStatusCallback(Handler(Looper.getMainLooper()), gnssCallback)
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1f, locationListener, Looper.getMainLooper())
            Log.i(TAG, "GNSS monitoring active.")
        } catch (e: Exception) {
            Log.e(TAG, "GNSS initialization failed: ${e.message}")
        }
    }

    // =========================================================================
    // حلقات التشغيل المتزامنة والرصد والالتقاط (Operational Loops)
    // =========================================================================
    private fun startOperationalLoops() {
        // حلقة النبضة التكتيكية الدورية (Heartbeat Loop)
        serviceScope.launch {
            while (isActive) {
                try {
                    val heartbeat = buildHeartbeatPayload()
                    enqueuePacket(heartbeat)
                    delay(HEARTBEAT_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Heartbeat loop error: ${e.message}")
                }
            }
        }

        // حلقة الاستكشاف والتبادل الشبكي التلقائي (Discovery Loop)
        serviceScope.launch {
            while (isActive) {
                try {
                    sendDiscoveryPacket()
                    delay(DISCOVERY_INTERVAL)
                } catch (e: Exception) {
                    Log.e(TAG, "Discovery loop error: ${e.message}")
                }
            }
        }

        // حلقة الإرسال السريع غير الحابسة (TX Loop)
        serviceScope.launch {
            while (isActive) {
                try {
                    processOutgoingQueue()
                    delay(200L) // نبضة دفع سريعة كل 200 ملي ثانية لتفريغ الطابور
                } catch (e: Exception) {
                    Log.e(TAG, "TX loop error: ${e.message}")
                }
            }
        }

        // حلقة الاستقبال والاعتراض الطيفي والفك التشفيري المستمر (RX Loop)
        serviceScope.launch {
            receiveLoop()
        }
    }

    // =========================================================================
    // بناء حمولة البيانات والـ JSON التكتيكي المشفر
    // =========================================================================
    private fun buildHeartbeatPayload(): String {
        val lat = lastLocation?.latitude ?: 0.0
        val lon = lastLocation?.longitude ?: 0.0

        // حرق الـ JSON بشكل نصي محصن مع تقريب دقة الإحداثيات جغرافياً لحماية التحركات
        val payload = """
            {
                "node":"AEGIS",
                "timestamp":$gpsTime,
                "satellites":$satelliteCount,
                "lat":${(lat * 100000).roundToInt() / 100000.0},
                "lon":${(lon * 100000).roundToInt() / 100000.0},
                "status":"ACTIVE"
            }
        """.trimIndent()

        return securityModel.encryptTacticalData(payload)
    }

    // =========================================================================
    // إدارة طابور النبضات (Queue System)
    // =========================================================================
    private fun enqueuePacket(packet: String) {
        if (packet.length > MAX_PACKET_SIZE) {
            Log.w(TAG, "Packet length exceeds limit [MAX: $MAX_PACKET_SIZE bytes]")
            return
        }
        outgoingQueue.add(packet)
    }

    private suspend fun processOutgoingQueue() {
        while (outgoingQueue.isNotEmpty()) {
            val packet = outgoingQueue.poll() ?: continue
            sendUdpBroadcast(packet)
        }
    }

    private suspend fun sendDiscoveryPacket() {
        val discovery = securityModel.encryptTacticalData(
            "DISCOVERY:${System.currentTimeMillis()}"
        )
        sendMulticast(discovery)
    }

    // =========================================================================
    // محركات البث عبر قنوات الـ UDP والـ Multicast
    // =========================================================================
    private suspend fun sendUdpBroadcast(data: String) {
        withContext(Dispatchers.IO) {
            try {
                val bytes = data.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    bytes,
                    bytes.size,
                    InetSocketAddress("255.255.255.255", UDP_PORT)
                )
                tacticalSocket?.send(packet)
                Log.d(TAG, "[TX] Broadcast pulse injected into network.")
            } catch (e: Exception) {
                Log.e(TAG, "Broadcast packet injection failed: ${e.message}")
            }
        }
    }

    private suspend fun sendMulticast(data: String) {
        withContext(Dispatchers.IO) {
            try {
                val bytes = data.toByteArray(Charsets.UTF_8)
                val packet = DatagramPacket(
                    bytes,
                    bytes.size,
                    InetSocketAddress(DISCOVERY_GROUP, UDP_PORT)
                )
                multicastSocket?.send(packet)
                Log.d(TAG, "[TX] Multicast packet broadcasted to mesh group.")
            } catch (e: Exception) {
                Log.e(TAG, "Multicast packet injection failed: ${e.message}")
            }
        }
    }

    // =========================================================================
    // الاستقبال والاعتراض التكتيكي المشفر (RX Core Loop)
    // =========================================================================
    private suspend fun receiveLoop() {
        withContext(Dispatchers.IO) {
            val buffer = ByteArray(MAX_PACKET_SIZE)
            while (isActive) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    tacticalSocket?.receive(packet)

                    val raw = String(packet.data, 0, packet.length, Charsets.UTF_8)
                    val decrypted = securityModel.decryptTacticalData(raw)

                    if (decrypted != null) {
                        Log.d(TAG, "📥 [NET_RX] Incoming secured tactical message: $decrypted")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "RX packet processing fallback triggered: ${e.message}")
                }
            }
        }
    }

    // =========================================================================
    // الإشعارات والتحصين الأمامي (Notification Hub)
    // =========================================================================
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AEGIS Sovereign Operations",
                NotificationManager.IMPORTANCE_HIGH // تعزيز الأهمية لحماية الخدمة من الإغلاق
            ).apply {
                description = "Operational tactical secure linking service"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AEGIS Operational Core")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    // =========================================================================
    // بروتوكول التطهير الفيزيائي والقطع غير الحابس للخيوط والمقابس (Destroy)
    // =========================================================================
    override fun onDestroy() {
        // قطع وإلغاء كافة الـ Coroutines كأول سطر فوري لتحرير معالج الهاتف
        serviceJob.cancel()

        try {
            locationManager?.removeUpdates(locationListener)
            locationManager?.unregisterGnssStatusCallback(gnssCallback)
            Log.d(TAG, "[CLEANUP] GNSS receivers unregistered.")
        } catch (_: Exception) {}

        // الإغلاق الآمن للمقابس لمنع حظر المنفذ 5555 عند إعادة التشغيل
        try {
            tacticalSocket?.let { if (!it.isClosed) it.close() }
        } catch (_: Exception) {}

        try {
            multicastSocket?.let { if (!it.isClosed) it.close() }
        } catch (_: Exception) {}

        Log.i(TAG, "🔒 [MESH DISCONNECT] Aegis hardware link channels purged safely.")
        super.onDestroy()
    }
}
