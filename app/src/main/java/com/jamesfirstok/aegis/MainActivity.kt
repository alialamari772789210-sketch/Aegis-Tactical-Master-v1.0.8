package com.jamesfirstok.aegis

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            orchestrator.initializeTacticalCore()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orchestrator = AegisSystemOrchestrator(this)
        alertManager = AlertManager(this)

        // بدء خدمة الرادار الحي
        startService(Intent(this, LiveRadarService::class.java))

        // طلب الصلاحيات
        permissionLauncher.launch(arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))

        setContent {
            AegisTheme {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    TacticalHUD(orchestrator, alertManager)
                }
            }
        }
    }
}

@Composable
fun TacticalHUD(orchestrator: AegisSystemOrchestrator, alertManager: AlertManager) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // WebView يعرض واجهة HUD
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = WebViewClient()

                    // حقن جسر AegisBridge
                    val bridge = AegisBridge(alertManager)
                    addJavascriptInterface(bridge, "AegisBridge")

                    // ربط زر manual override
                    addJavascriptInterface(object {
                        @android.webkit.JavascriptInterface
                        fun executeManualOverride() {
                            orchestrator.executeManualOverride()
                        }

                        @android.webkit.JavascriptInterface
                        fun getLatestSignalData(): String {
                            return com.jamesfirstok.aegis.LiveRadarBridgeHolder.latestData
                        }
                    }, "AegisBridge")

                    loadUrl("file:///android_asset/aegis_hud.html")
                }
            },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // زر التحييد القسري
        Button(
            onClick = { orchestrator.executeManualOverride() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("FORCE LANDING / NEUTRALIZE", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
