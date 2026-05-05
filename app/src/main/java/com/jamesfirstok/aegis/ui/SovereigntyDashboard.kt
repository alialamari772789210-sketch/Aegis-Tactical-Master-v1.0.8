package com.jamesfirstok.aegis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jamesfirstok.aegis.core.OperationalTruthVerifier

@Composable
fun SovereigntyDashboard(verifier: OperationalTruthVerifier) {
    val report = remember { verifier.verifySovereigntyTruth() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "🛡️ Aegis Tactical: مركز السيادة",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FF00) // أخضر تكتيكي
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {
            items(report.toList()) { (key, value) ->
                StatusCard(title = key, status = value)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (report.values.all { it.contains("✅") }) {
            Text(
                text = "⚔️ الحالة: النظام في وضع العمليات القتالية الكاملة",
                color = Color.Green,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(8.dp)
            )
        } else {
            Text(
                text = "⚠️ تنبيه: النظام يكتشف مكونات غير مكتملة (وضع المحاكاة)",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatusCard(title: String, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, color = Color.LightGray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                color = if (status.contains("✅")) Color.Green else Color.Yellow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
