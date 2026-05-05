package com.jamesfirstok.aegis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jamesfirstok.aegis.core.OperationalTruthVerifier
import com.jamesfirstok.aegis.ui.SovereigntyDashboard

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val verifier = OperationalTruthVerifier(this)
        
        setContent {
            // تفعيل الواجهة التكتيكية
            SovereigntyDashboard(verifier = verifier)
        }
    }
}
