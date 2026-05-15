package com.jamesfirstok.aegis

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.jamesfirstok.aegis.bridge.AegisBridge
import com.jamesfirstok.aegis.core.AegisSystemOrchestrator
import com.jamesfirstok.aegis.service.AlertManager
import com.jamesfirstok.aegis.service.LiveRadarService

class MainActivity : ComponentActivity() {
    private lateinit var orchestrator: AegisSystemOrchestrator
    private lateinit var alertManager: AlertManager
    private lateinit var usbManager: UsbManager
    private var hardwareStatus by mutableStateOf("وضع الهاتف (رصد سلبي محلي)")

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.values.all { it }) {
            orchestrator.initializeTacticalCore()
            checkExternalHardware()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orchestrator = AegisSystemOrchestrator(this)
        alertManager = AlertManager(this)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        startService(Intent(this, LiveRadarService::class.java))
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        setContent {
            AegisTheme {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        HardwareStatusBar(status = hardwareStatus)
                        Box(modifier = Modifier.weight(1f)) { TacticalHUD(orchestrator, alertManager) }
                    }
                }
            }
        }
    }

    override fun onResume() { super.onResume(); checkExternalHardware() }

    private fun checkExternalHardware() {
        val deviceList: HashMap<String, UsbDevice> = usbManager.deviceList
        if (deviceList.isEmpty()) {
            hardwareStatus = "وضع الهاتف (رصد سلبي محلي)"
            orchestrator.setOperationMode(isExternal = false)
        } else {
            for (device in deviceList.values) {
                if (device.vendorId == 0x0bda || device.vendorId == 0x1d50) {
                    hardwareStatus = "تكتيكي: متصل عبر العتاد [SDR Array Active]"
                    Toast.makeText(this, "تم رصد مصفوفة الهوائيات الخارجية", Toast.LENGTH_SHORT).show()
                    orchestrator.setOperationMode(isExternal = true, usbDevice = device)
                    return
                }
            }
            hardwareStatus = "جهاز USB متصل - غير معرّف كـ SDR"
        }
    }
}

@Composable
fun HardwareStatusBar(status: String) {
    Surface(color = if (status.contains("تكتيكي")) Color(0xFF003300) else Color(0xFF330000), modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Text(text = "حالة المنظومة: $status", color = if (status.contains("تكتيكي")) Color.Green else Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
    }
}

@Composable
fun TacticalHUD(orchestrator: AegisSystemOrchestrator, alertManager: AlertManager) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        AndroidView(factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true; webViewClient = WebViewClient()
                addJavascriptInterface(AegisBridge(alertManager), "AegisBridge")
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface fun executeManualOverride() { orchestrator.executeManualOverride() }
                    @android.webkit.JavascriptInterface fun getLatestSignalData(): String { return com.jamesfirstok.aegis.LiveRadarBridgeHolder.latestData }
                }, "AegisBridge")
                loadUrl("file:///android_asset/aegis_hud.html")
            }
        }, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { orchestrator.executeManualOverride() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("FORCE LANDING / NEUTRALIZE", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
