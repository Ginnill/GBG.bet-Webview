package com.gbgbet.myapplication

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerLib
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        // Inicializa o SDK do AppsFlyer
        AppsFlyerLib.getInstance().init("35M4o5s9HMXZd3SFiLsr6J", null, this)
        AppsFlyerLib.getInstance().start(this)

        // Referencia o WebView do layout
        webView = findViewById(R.id.webview)

        // Configura o WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true // Habilita o armazenamento local para manipulação do DOM pelo Vue.js

        // Configura o DownloadListener para o WebView
        webView.setDownloadListener { url, _, _, _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Define um WebViewClient para carregar páginas dentro do WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                if (url.startsWith("af-event://")) {
                    val urlParts = url.split("\\?")
                    if (urlParts.size > 1) {
                        val query = urlParts[1]
                        var eventName: String? = null
                        val eventValue = HashMap<String, Any>()

                        for (param in query.split("&")) {
                            val pair = param.split("=")
                            val key = pair[0]
                            if (pair.size > 1) {
                                if ("eventName" == key) {
                                    eventName = pair[1]
                                } else if ("eventValue" == key) {
                                    try {
                                        val event = JSONObject(pair[1])
                                        val keys: JSONArray = event.names()
                                        for (i in 0 until keys.length()) {
                                            val keyName = keys.getString(i)
                                            eventValue[keyName] = event.getString(keyName)
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                        AppsFlyerLib.getInstance().logEvent(applicationContext, eventName, eventValue)
                    }
                    return true
                }
                return false
            }
        }

        // Carrega a página desejada dentro do WebView
        webView.loadUrl("https://www.gbgbet.com/?pid=9101")
    }

    // Intercepta o evento de pressionar o botão de volta do Android para navegar pelo histórico do WebView
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
