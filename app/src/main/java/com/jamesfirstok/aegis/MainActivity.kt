package com.jamesfirstok.aegis

import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // إعداد واجهة الرادار والتحكم السيادي كما حدد المعماري علي العماري
        webView = WebView(this)
        setContentView(webView)
        
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()
        
        // ربط المصفوفة الصوتية v2.3.0 بالنواة السيادية
        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onVoiceCommand(command: String) {
                // معالجة الأوامر التكتيكية الصادرة من العقيد
            }
        }, "AegisCore")

        webView.loadUrl("file:///android_asset/code.html")
    }
}
