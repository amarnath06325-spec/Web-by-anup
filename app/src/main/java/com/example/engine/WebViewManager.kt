package com.example.engine

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

object WebViewManager {

    private const val DESKTOP_USER_AGENT =
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"

    @SuppressLint("SetJavaScriptEnabled")
    fun createWebView(
        context: Context,
        isIncognito: Boolean = false,
        onDownloadRequested: (DownloadRequest) -> Unit
    ): WebView {
        return WebView(context).apply {
            // Hardware acceleration
            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            settings.apply {
                // Performance and web capabilities
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = !isIncognito
                cacheMode = if (isIncognito) WebSettings.LOAD_NO_CACHE else WebSettings.LOAD_DEFAULT
                saveFormData = !isIncognito

                // Multimedia and Video Playback (HTML5, MP4, HLS, YouTube)
                mediaPlaybackRequiresUserGesture = false

                // Viewport and scaling
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // Resource and file access
                allowFileAccess = !isIncognito
                allowContentAccess = true

                // Security and mixed content
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                // Text encoding
                defaultTextEncodingName = "utf-8"
            }

            // Cookie Manager setup
            val webViewInstance = this
            CookieManager.getInstance().apply {
                if (isIncognito) {
                    setAcceptThirdPartyCookies(webViewInstance, false)
                } else {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webViewInstance, true)
                }
            }

            // Download Listener
            setDownloadListener { url, userAgent, contentDisposition, mimetype, contentLength ->
                onDownloadRequested(
                    DownloadRequest(
                        url = url,
                        userAgent = userAgent,
                        contentDisposition = contentDisposition,
                        mimeType = mimetype,
                        contentLength = contentLength
                    )
                )
            }
        }
    }

    fun destroyWebView(webView: WebView, isIncognito: Boolean = false) {
        try {
            if (isIncognito) {
                webView.clearCache(true)
                webView.clearHistory()
                webView.clearFormData()
                webView.clearSslPreferences()
            }
            webView.destroy()
        } catch (_: Exception) {}
    }

    fun applyDesktopMode(webView: WebView, isDesktop: Boolean) {
        if (isDesktop) {
            webView.settings.userAgentString = DESKTOP_USER_AGENT
            webView.settings.useWideViewPort = true
            webView.settings.loadWithOverviewMode = true
        } else {
            webView.settings.userAgentString = null // Reset to default mobile user agent
        }
    }
}
