package com.example.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.engine.AppWebChromeClient
import com.example.engine.AppWebViewClient
import com.example.engine.WebViewManager
import com.example.ui.components.AddressBar
import com.example.ui.components.BrowserBottomBar
import com.example.ui.components.TopSearchBar
import com.example.ui.components.BookmarksHistorySheet
import com.example.ui.components.DownloadsSheet
import com.example.ui.components.FindInPageBar
import com.example.ui.components.FullscreenVideoContainer
import com.example.ui.components.GeolocationDialog
import com.example.ui.components.SettingsSheet
import com.example.ui.components.StartPage
import com.example.ui.components.TabSwitcherSheet
import com.example.ui.components.WebPermissionDialog
import com.example.viewmodel.BrowserViewModel
import kotlinx.coroutines.launch

@Composable
fun BrowserScreen(
    viewModel: BrowserViewModel,
    onRequestSystemPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val tabs by viewModel.tabs.collectAsState()
    val currentTabIndex by viewModel.currentTabIndex.collectAsState()
    val currentTab = tabs.getOrNull(currentTabIndex)

    val isBottomBar by viewModel.isBottomBar.collectAsState()
    val fullscreenView by viewModel.fullscreenCustomView.collectAsState()
    val showTabSwitcher by viewModel.showTabSwitcher.collectAsState()
    val showBookmarksHistory by viewModel.showBookmarksHistory.collectAsState()
    val selectedBookmarksHistoryTab by viewModel.selectedBookmarksHistoryTab.collectAsState()
    val showDownloads by viewModel.showDownloads.collectAsState()
    val showSettings by viewModel.showSettings.collectAsState()
    val showFindInPage by viewModel.showFindInPage.collectAsState()
    val findQuery by viewModel.findQuery.collectAsState()

    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.history.collectAsState()
    val downloads by viewModel.downloads.collectAsState()

    val pendingWebPermission by viewModel.pendingWebPermission.collectAsState()
    val pendingGeolocation by viewModel.pendingGeolocation.collectAsState()
    val bannerMessage by viewModel.bannerMessage.collectAsState()

    val isBookmarked = remember(bookmarks, currentTab?.url) {
        val url = currentTab?.url.orEmpty()
        url.isNotBlank() && bookmarks.any { it.url == url }
    }

    LaunchedEffect(bannerMessage) {
        bannerMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissBanner()
        }
    }

    // Fullscreen video takes over the entire screen when active
    if (fullscreenView != null) {
        FullscreenVideoContainer(
            customView = fullscreenView!!,
            onExitFullscreen = { viewModel.hideFullscreenView() }
        )
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopSearchBar(
                tabState = currentTab,
                onLoadUrl = { viewModel.loadUrl(it) },
                onReload = { viewModel.reload() },
                onStopLoading = { viewModel.stopLoading() },
                onProfileClick = {
                    val user = com.example.auth.GoogleAuthManager.currentUser.value
                    if (user != null) {
                        viewModel.showBanner("Signed in as ${user.displayName} (${user.email})")
                    } else {
                        scope.launch {
                            val result = com.example.auth.GoogleAuthManager.signIn(context)
                            if (result.isSuccess) {
                                viewModel.showBanner("Google Account synced: ${result.getOrNull()?.displayName}")
                            } else {
                                viewModel.showBanner("Sign in: ${result.exceptionOrNull()?.message ?: "Select account from settings"}")
                            }
                        }
                    }
                },
                onOpenSettings = {
                    val intent = android.content.Intent(context, com.example.settings.SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                onOpenNewTab = { viewModel.addNewTab() },
                onOpenNewIncognitoTab = { viewModel.openNewIncognitoTab() },
                onClearData = {
                    android.webkit.CookieManager.getInstance().removeAllCookies(null)
                    android.webkit.CookieManager.getInstance().flush()
                    android.webkit.WebStorage.getInstance().deleteAllData()
                    viewModel.showBanner("Browsing data & cookies cleared")
                }
            )
        },
        bottomBar = {
            BrowserBottomBar(
                tabState = currentTab,
                tabCount = tabs.size,
                isBookmarked = isBookmarked,
                onGoBack = { viewModel.goBack() },
                onGoForward = { viewModel.goForward() },
                onGoHome = { viewModel.navigateHome() },
                onToggleBookmark = { viewModel.toggleBookmark() },
                onOpenTabSwitcher = { viewModel.openTabSwitcher() },
                onOpenBookmarks = { viewModel.openBookmarksHistory(0) },
                onOpenHistory = { viewModel.openBookmarksHistory(1) },
                onOpenDownloads = { viewModel.openDownloads() },
                onToggleDesktopMode = { viewModel.toggleDesktopMode() },
                onOpenFindInPage = { viewModel.openFindInPage() },
                onOpenNewTab = { viewModel.addNewTab() },
                onOpenNewIncognitoTab = { viewModel.openNewIncognitoTab() },
                onReload = { viewModel.reload() },
                onOpenSettings = {
                    val intent = android.content.Intent(context, com.example.settings.SettingsActivity::class.java)
                    context.startActivity(intent)
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Find in page bar
                AnimatedVisibility(
                    visible = showFindInPage,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    FindInPageBar(
                        query = findQuery,
                        onQueryChange = { viewModel.setFindQuery(it) },
                        onFindNext = { viewModel.findNext() },
                        onFindPrevious = { viewModel.findPrevious() },
                        onClose = { viewModel.closeFindInPage() }
                    )
                }

                // Main Content: WebView or Start Page or Error View
                val activeTab = currentTab
                val isBlank = activeTab == null || activeTab.url.isBlank() || activeTab.url == "about:blank"

                if (isBlank) {
                    StartPage(
                        recentHistory = history,
                        onSelectUrl = { viewModel.loadUrl(it) },
                        isIncognito = activeTab?.isIncognito == true
                    )
                } else if (activeTab?.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unable to load page",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = activeTab.errorMessage ?: "Network connection error",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.reload() },
                                    modifier = Modifier.testTag("retry_load_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Text("Retry")
                                }
                            }
                        }
                    }
                } else {
                    // Render WebView for the current tab
                    key(currentTabIndex) {
                        WebViewHost(
                            index = currentTabIndex,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    // Tab Switcher Bottom Sheet
    if (showTabSwitcher) {
        TabSwitcherSheet(
            tabs = tabs,
            currentTabIndex = currentTabIndex,
            onSelectTab = { viewModel.selectTab(it) },
            onCloseTab = { viewModel.closeTab(it) },
            onNewTab = { viewModel.addNewTab() },
            onNewIncognitoTab = { viewModel.openNewIncognitoTab() },
            onCloseAll = { incognitoOnly -> viewModel.closeAllTabs(incognitoOnly) },
            onDismiss = { viewModel.closeTabSwitcher() }
        )
    }

    // Bookmarks & History Bottom Sheet
    if (showBookmarksHistory) {
        BookmarksHistorySheet(
            initialTab = selectedBookmarksHistoryTab,
            bookmarks = bookmarks,
            history = history,
            onSelectUrl = { viewModel.loadUrl(it) },
            onRemoveBookmark = { viewModel.removeBookmark(it) },
            onRemoveHistoryItem = { viewModel.removeHistoryItem(it) },
            onClearHistory = { viewModel.clearHistory() },
            onDismiss = { viewModel.closeBookmarksHistory() }
        )
    }

    // Downloads Bottom Sheet
    if (showDownloads) {
        DownloadsSheet(
            downloads = downloads,
            onRemoveDownload = { viewModel.removeDownload(it) },
            onDismiss = { viewModel.closeDownloads() }
        )
    }

    // Settings Bottom Sheet
    if (showSettings) {
        SettingsSheet(
            isBottomBar = isBottomBar,
            onToggleBarPosition = { viewModel.toggleBarPosition() },
            onRequestPermissions = onRequestSystemPermissions,
            onClearBrowserData = {
                CookieManager.getInstance().removeAllCookies(null)
                WebStorage.getInstance().deleteAllData()
                viewModel.showBanner("Cookies & cache cleared")
            },
            onDismiss = { viewModel.closeSettings() }
        )
    }

    // WebRTC Permission Dialog
    pendingWebPermission?.let { request ->
        WebPermissionDialog(
            request = request,
            onGrant = { resources -> viewModel.grantWebPermission(resources) },
            onDeny = { viewModel.denyWebPermission() }
        )
    }

    // HTML5 Geolocation Dialog
    pendingGeolocation?.let { (origin, _) ->
        GeolocationDialog(
            origin = origin,
            onAllow = { viewModel.grantGeolocation(true) },
            onBlock = { viewModel.denyGeolocation(false) }
        )
    }

    // Multiple Windows / Google OAuth Popup Dialog
    val popupWebView by viewModel.popupWebView.collectAsState()
    popupWebView?.let { pWv ->
        Dialog(
            onDismissRequest = { viewModel.dismissPopupWebView() },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Authentication / Sign In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { viewModel.dismissPopupWebView() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close window")
                        }
                    }
                    AndroidView(
                        factory = {
                            pWv.apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun WebViewHost(
    index: Int,
    viewModel: BrowserViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tabs by viewModel.tabs.collectAsState()
    val tab = tabs.getOrNull(index)

    val webView = remember(index) {
        val wv = tab?.webView ?: WebViewManager.createWebView(context, isIncognito = tab?.isIncognito == true) { downloadReq ->
            viewModel.handleDownloadRequest(downloadReq)
        }

        wv.webChromeClient = AppWebChromeClient(
            onProgressUpdate = { progress -> viewModel.updateProgress(index, progress) },
            onTitleUpdate = { title -> viewModel.updateTitle(index, title) },
            onIconUpdate = { /* Favicon bitmap updated */ },
            onShowFullscreenView = { view, callback ->
                viewModel.showFullscreenView(view, callback)
            },
            onHideFullscreenView = {
                viewModel.hideFullscreenView()
            },
            onWebPermissionRequest = { req ->
                viewModel.setPendingWebPermission(req)
            },
            onGeolocationRequest = { origin, callback ->
                viewModel.setPendingGeolocation(origin, callback)
            },
            onFileChooser = { callback, _ ->
                viewModel.filePathCallback = callback
                true
            },
            onCreateWindowRequested = { _, _, _, resultMsg ->
                val popup = WebViewManager.createWebView(context, isIncognito = tab?.isIncognito == true) { downloadReq ->
                    viewModel.handleDownloadRequest(downloadReq)
                }
                popup.webChromeClient = AppWebChromeClient(
                    onProgressUpdate = {},
                    onTitleUpdate = {},
                    onIconUpdate = {},
                    onShowFullscreenView = { _, _ -> },
                    onHideFullscreenView = {},
                    onWebPermissionRequest = { viewModel.setPendingWebPermission(it) },
                    onGeolocationRequest = { o, c -> viewModel.setPendingGeolocation(o, c) },
                    onFileChooser = { cb, _ ->
                        viewModel.filePathCallback = cb
                        true
                    },
                    onCloseWindowRequested = {
                        viewModel.dismissPopupWebView()
                    }
                )
                popup.webViewClient = AppWebViewClient(
                    context = context,
                    onPageStartedCallback = {},
                    onPageFinishedCallback = { _, _, _, _, _ -> },
                    onErrorReceived = {}
                )
                val transport = resultMsg?.obj as? WebView.WebViewTransport
                transport?.webView = popup
                resultMsg?.sendToTarget()
                viewModel.setPopupWebView(popup)
                true
            },
            onCloseWindowRequested = {
                viewModel.dismissPopupWebView()
            }
        )

        wv.webViewClient = AppWebViewClient(
            context = context,
            onPageStartedCallback = { url -> viewModel.onPageStarted(index, url) },
            onPageFinishedCallback = { url, title, canBack, canFwd, isSecure ->
                viewModel.onPageFinished(index, url, title, canBack, canFwd, isSecure)
            },
            onErrorReceived = { err -> viewModel.onPageError(index, err) }
        )

        viewModel.attachWebView(index, wv)
        wv
    }

    AndroidView(
        factory = {
            webView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        update = { wv ->
            if (tab != null && tab.url.isNotBlank() && wv.url != tab.url && !wv.url.isNullOrEmpty() == false) {
                wv.loadUrl(tab.url)
            }
        },
        modifier = modifier.testTag("webview_canvas")
    )
}
