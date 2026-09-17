package com.example.engine

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient

class AppWebViewClient(
    private val context: Context,
    private val onPageStartedCallback: (url: String) -> Unit,
    private val onPageFinishedCallback: (url: String, title: String?, canGoBack: Boolean, canGoForward: Boolean, isSecure: Boolean) -> Unit,
    private val onErrorReceived: (description: String) -> Unit
) : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val uri = request?.url ?: return false
        val scheme = uri.scheme?.lowercase() ?: ""

        // Process standard web schemes internally
        if (scheme == "http" || scheme == "https" || scheme == "about" || scheme == "data") {
            return false
        }

        // Handle external application schemes (tel, mailto, maps, intents)
        try {
            if (scheme == "intent") {
                val parsedIntent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (parsedIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(parsedIntent)
                    return true
                }
                // Handle fallback URL if app is not installed
                val fallbackUrl = parsedIntent.getStringExtra("browser_fallback_url")
                if (!fallbackUrl.isNullOrBlank()) {
                    view?.loadUrl(fallbackUrl)
                    return true
                }
                return true
            }

            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return true
            }
            return false
        } catch (_: Exception) {
            return false
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url != null) {
            onPageStartedCallback(url)
        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        val finalUrl = url ?: view?.url ?: ""
        val title = view?.title
        val canGoBack = view?.canGoBack() ?: false
        val canGoForward = view?.canGoForward() ?: false
        val isSecure = finalUrl.startsWith("https://", ignoreCase = true)

        onPageFinishedCallback(finalUrl, title, canGoBack, canGoForward, isSecure)

        // Inject seamless Google auto-login helper if account is synced
        if (view != null && finalUrl.isNotBlank() && !finalUrl.startsWith("chrome://")) {
            com.example.auth.GoogleAutoLoginHelper.injectAutoLoginScript(context, view, finalUrl)
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        if (request?.isForMainFrame == true) {
            val errorDescription = error?.description?.toString() ?: "Failed to load page"
            onErrorReceived(errorDescription)
        }
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        // By default, do not automatically ignore SSL errors for security, but cancel or handle safely
        handler?.cancel()
    }
}
