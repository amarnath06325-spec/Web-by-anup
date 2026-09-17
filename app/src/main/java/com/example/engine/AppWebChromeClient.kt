package com.example.engine

import android.graphics.Bitmap
import android.net.Uri
import android.os.Message
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView

class AppWebChromeClient(
    private val onProgressUpdate: (Int) -> Unit,
    private val onTitleUpdate: (String) -> Unit,
    private val onIconUpdate: (Bitmap?) -> Unit,
    private val onShowFullscreenView: (View, WebChromeClient.CustomViewCallback) -> Unit,
    private val onHideFullscreenView: () -> Unit,
    private val onWebPermissionRequest: (PermissionRequest) -> Unit,
    private val onGeolocationRequest: (String, GeolocationPermissions.Callback) -> Unit,
    private val onFileChooser: (ValueCallback<Array<Uri>>?, FileChooserParams?) -> Boolean,
    private val onCreateWindowRequested: ((WebView?, Boolean, Boolean, Message?) -> Boolean)? = null,
    private val onCloseWindowRequested: ((WebView?) -> Unit)? = null
) : WebChromeClient() {

    private var customViewCallback: CustomViewCallback? = null

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        onProgressUpdate(newProgress)
    }

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        if (!title.isNullOrBlank()) {
            onTitleUpdate(title)
        }
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        onIconUpdate(icon)
    }

    override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
        if (view != null && callback != null) {
            customViewCallback = callback
            onShowFullscreenView(view, callback)
        }
    }

    override fun onHideCustomView() {
        onHideFullscreenView()
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
    }

    override fun onPermissionRequest(request: PermissionRequest?) {
        if (request != null) {
            onWebPermissionRequest(request)
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String?,
        callback: GeolocationPermissions.Callback?
    ) {
        if (origin != null && callback != null) {
            onGeolocationRequest(origin, callback)
        } else {
            callback?.invoke(origin, false, false)
        }
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return onFileChooser(filePathCallback, fileChooserParams)
    }

    override fun onCreateWindow(
        view: WebView?,
        isDialog: Boolean,
        isUserGesture: Boolean,
        resultMsg: Message?
    ): Boolean {
        if (onCreateWindowRequested != null && resultMsg != null) {
            return onCreateWindowRequested.invoke(view, isDialog, isUserGesture, resultMsg)
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onCloseWindow(window: WebView?) {
        super.onCloseWindow(window)
        onCloseWindowRequested?.invoke(window)
    }
}
