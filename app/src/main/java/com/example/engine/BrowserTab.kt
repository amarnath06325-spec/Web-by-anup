package com.example.engine

import android.graphics.Bitmap
import android.webkit.WebView
import java.util.UUID

data class BrowserTabState(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "New Tab",
    val url: String = "",
    val displayUrl: String = "",
    val favicon: Bitmap? = null,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isSecure: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isIncognito: Boolean = false,
    val errorMessage: String? = null,
    val webView: WebView? = null
)

data class DownloadRequest(
    val url: String,
    val userAgent: String,
    val contentDisposition: String,
    val mimeType: String,
    val contentLength: Long
)
