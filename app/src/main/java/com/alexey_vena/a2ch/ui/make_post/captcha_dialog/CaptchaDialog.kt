package com.alexey_vena.a2ch.ui.make_post.captcha_dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alexey_vena.a2ch.R
import com.alexey_vena.a2ch.util.CAPTCHA_URL
import com.alexey_vena.a2ch.util.PARSE_HTML
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup


class CaptchaDialog(
    private val ctx: Context,
    private val captchaKey: String,
    private val listener: CaptchaListener
) : Dialog(ctx) {
    private lateinit var webView: WebView
    private val headerMap = hashMapOf("Referer" to "https://2ch.hk")
    private var firstRun = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_captcha)
        setupWebView()
    }

    private fun setupWebView() {
        webView = findViewById(R.id.webView)
        webView.apply {
            settings.javaScriptEnabled = true
            addJavascriptInterface(LoadListener(), "HTMLOUT")
            loadUrl(CAPTCHA_URL + captchaKey, headerMap)
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String?) {
                    view.loadUrl(PARSE_HTML);
                }
            }
        }


    }

    inner class LoadListener {
        @JavascriptInterface
        fun processHTML(html: String) {
            if(html.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    val document = Jsoup.parse(html)
                    if(document!=null){
                        val captchaResponse = document.select("div[class=fbc-verification-token]").text()
                        if (captchaResponse != null && captchaResponse.isNotEmpty()) {
                            listener.captchaPassed(captchaResponse)
                            this@CaptchaDialog.dismiss()
                        }

                        if (document.select("div[class=rc-imageselect-desc-no-canonical]").text().isNullOrEmpty()) {
                            this@CaptchaDialog.dismiss()
                        }
                    }

                }
            }

        }
    }
}


