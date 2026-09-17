package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.ui.BrowserScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.BrowserViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BrowserViewModel by viewModels()

    // File Chooser for HTML forms (<input type="file">)
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        val callback = viewModel.filePathCallback
        if (callback != null) {
            if (uris.isNotEmpty()) {
                callback.onReceiveValue(uris.toTypedArray())
            } else {
                callback.onReceiveValue(null)
            }
            viewModel.filePathCallback = null
        }
    }

    // System Permissions Launcher (WebRTC Camera, Microphone, Geolocation, Notifications)
    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        // Respond to pending WebRTC permission request if active
        val pendingWeb = viewModel.pendingWebPermission.value
        if (pendingWeb != null) {
            val resourcesToGrant = mutableListOf<String>()
            if (cameraGranted) {
                resourcesToGrant.add(android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE)
            }
            if (audioGranted) {
                resourcesToGrant.add(android.webkit.PermissionRequest.RESOURCE_AUDIO_CAPTURE)
            }

            if (resourcesToGrant.isNotEmpty()) {
                viewModel.grantWebPermission(resourcesToGrant.toTypedArray())
            } else {
                viewModel.denyWebPermission()
            }
        }

        // Respond to pending HTML5 geolocation request if active
        val pendingGeo = viewModel.pendingGeolocation.value
        if (pendingGeo != null) {
            if (locationGranted) {
                viewModel.grantGeolocation(true)
            } else {
                viewModel.denyGeolocation(false)
            }
        }

        viewModel.showBanner("Permissions updated")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle URL from incoming Intent (e.g. opened from another app or default browser link)
        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                val fullscreenView by viewModel.fullscreenCustomView.collectAsState()
                val currentTab = viewModel.currentTab

                // Dynamic Video Fullscreen and Orientation Handling
                LaunchedEffect(fullscreenView) {
                    val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
                    if (fullscreenView != null) {
                        // Switch to landscape mode exclusively for fullscreen video
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        windowInsetsController.systemBarsBehavior =
                            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
                    } else {
                        // Restore natural orientation across app
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
                    }
                }

                // Handle Back Press Gracefully (Checks WebView history before exiting)
                BackHandler(enabled = true) {
                    when {
                        viewModel.popupWebView.value != null -> {
                            viewModel.dismissPopupWebView()
                        }
                        viewModel.fullscreenCustomView.value != null -> {
                            viewModel.hideFullscreenView()
                        }
                        viewModel.showTabSwitcher.value -> {
                            viewModel.closeTabSwitcher()
                        }
                        viewModel.showBookmarksHistory.value -> {
                            viewModel.closeBookmarksHistory()
                        }
                        viewModel.showDownloads.value -> {
                            viewModel.closeDownloads()
                        }
                        viewModel.showSettings.value -> {
                            viewModel.closeSettings()
                        }
                        viewModel.showFindInPage.value -> {
                            viewModel.closeFindInPage()
                        }
                        currentTab?.canGoBack == true || currentTab?.webView?.canGoBack() == true -> {
                            viewModel.goBack()
                        }
                        else -> {
                            finish()
                        }
                    }
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    BrowserScreen(
                        viewModel = viewModel,
                        onRequestSystemPermissions = { requestAllBrowserPermissions() }
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.currentTab?.let { tab ->
            if (tab.url.isNotBlank() && !tab.isIncognito) {
                outState.putString("SAVED_ACTIVE_URL", tab.url)
                viewModel.saveActiveUrlManually(tab.url)
            }
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val savedUrl = savedInstanceState.getString("SAVED_ACTIVE_URL")
        if (!savedUrl.isNullOrBlank() && savedUrl != "about:blank") {
            viewModel.loadUrl(savedUrl)
        }
    }

    override fun onPause() {
        super.onPause()
        android.webkit.CookieManager.getInstance().flush()
        viewModel.currentTab?.let { tab ->
            if (tab.url.isNotBlank() && !tab.isIncognito) {
                viewModel.saveActiveUrlManually(tab.url)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val data: Uri? = intent?.data
        if (intent?.action == Intent.ACTION_VIEW && data != null) {
            val urlString = data.toString()
            if (urlString.isNotBlank()) {
                viewModel.loadUrl(urlString)
            }
        }
    }

    fun requestAllBrowserPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    fun launchFilePicker(mimeType: String = "*/*") {
        filePickerLauncher.launch(mimeType)
    }
}
