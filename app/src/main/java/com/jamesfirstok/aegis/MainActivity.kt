package com.jamesfirstok.aegis

import android.Manifest
import android.content.Intent
import android.media.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jamesfirstok.aegis.ui.theme.AegisTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) {
            startService(Intent(this, RadarService::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        setContent {
            AegisTheme {
                Surface(color = Color.Black) {
                    TacticalHUD()
                }
            }
        }
    }
}

@Composable
fun TacticalHUD(radarVM: RadarViewModel = viewModel()) {
    val spectrum by radarVM.spectrum.collectAsState()
    val threatLevel by radarVM.threatLevel.collectAsState()
    
    Column(Modifier.fillMaxSize()) {
        // Header
        Text(
            text = "⚔️ AEGIS TACTICAL v2.0",
            color = Color.Green,
            fontSize = 24.sp,
            modifier = Modifier.padding(16.dp)
        )
        
        // Waterfall Spectrum
        WaterfallDisplay(spectrum = spectrum)
        
        // Threat Gauge
        ThreatGauge(level = threatLevel)
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun WaterfallDisplay(spectrum: List<FloatArray>) {
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val heightPerLine = size.height / 100f
        spectrum.takeLast(100).forEachIndexed { index, frame ->
            val intensity = frame.maxOrNull() ?: 0f
            val color = Color(
                red = (intensity * 2f).coerceIn(0f, 1f),
                green = 0f,
                blue = (1f - intensity).coerceIn(0f, 1f),
                alpha = 0.8f
            )
            drawLine(
                color = color,
                start = Offset(index * 3f, size.height),
                end = Offset(index * 3f, size.height - intensity * heightPerLine),
                strokeWidth = 2f
            )
        }
    }
}
