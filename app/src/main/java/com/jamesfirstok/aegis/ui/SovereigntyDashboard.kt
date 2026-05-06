package com.jamesfirstok.aegis.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamesfirstok.aegis.core.SystemIntegrityReport

@Composable
fun SovereigntyDashboard(integrityReport: SystemIntegrityReport) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "🛡️ AEGIS TACTICAL DASHBOARD v2.0",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Integrity Gauge
        IntegrityGauge(score = integrityReport.overallScore)
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Status Grid
        StatusGrid(integrityReport)
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Threat Level Indicator
        ThreatLevelBar(integrityReport)
    }
}

@Composable
fun IntegrityGauge(score: Float) {
    Canvas(modifier = Modifier.size(200.dp)) {
        val sweepAngle = 2.8f * score // 0-100% → 0-280°
        drawArc(
            color = Color.Green,
            startAngle = 220f,
            sweepAngle = sweepAngle,
            useCenter = true,
            topLeft = Offset(25f, 25f),
            size = size - Offset(50f, 50f)
        )
    }
    Text(
        text = "${(score * 100).toInt()}%",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.offset(y = (-20).dp)
    )
}
