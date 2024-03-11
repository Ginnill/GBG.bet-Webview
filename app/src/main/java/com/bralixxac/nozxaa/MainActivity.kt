package com.bralixxac.nozxaa

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.appsflyer.AppsFlyerLib

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
                                val value = pair[1]
                                eventValue[key] = value // Adiciona o parâmetro ao mapa de valores do evento
                            }
                        }

                        // Verifica o nome do evento para os eventos específicos que você deseja rastrear
                        eventName = when (eventValue["eventName"]) {
                            "af_login" -> "af_login"
                            "af_complete_registration" -> "af_complete_registration"
                            "af_first_purchase" -> "af_first_purchase"
                            "af_purchase" -> "af_purchase"
                            else -> null
                        }

                        // Se o evento é um dos eventos que você deseja rastrear, envie-o para o AppsFlyer
                        if (eventName != null) {
                            AppsFlyerLib.getInstance().logEvent(applicationContext, eventName, eventValue)
                        }
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
