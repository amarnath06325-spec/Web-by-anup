package com.example.viewmodel

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BookmarkEntity
import com.example.data.BrowserDatabase
import com.example.data.BrowserRepository
import com.example.data.DownloadEntity
import com.example.data.HistoryEntity
import com.example.engine.BrowserTabState
import com.example.engine.DownloadRequest
import com.example.engine.WebViewManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BrowserRepository

    init {
        val database = BrowserDatabase.getDatabase(application)
        repository = BrowserRepository(database.browserDao())
    }

    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.bookmarks
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.history
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val downloads: StateFlow<List<DownloadEntity>> = repository.downloads
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tabs Management
    private val _tabs = MutableStateFlow<List<BrowserTabState>>(listOf(BrowserTabState()))
    val tabs: StateFlow<List<BrowserTabState>> = _tabs.asStateFlow()

    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    // Address bar positioning (true = bottom, false = top)
    private val _isBottomBar = MutableStateFlow(true)
    val isBottomBar: StateFlow<Boolean> = _isBottomBar.asStateFlow()

    // Fullscreen Video playback state
    private val _fullscreenCustomView = MutableStateFlow<View?>(null)
    val fullscreenCustomView: StateFlow<View?> = _fullscreenCustomView.asStateFlow()
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    // Sheets & Dialogs visibility
    private val _showTabSwitcher = MutableStateFlow(false)
    val showTabSwitcher: StateFlow<Boolean> = _showTabSwitcher.asStateFlow()

    private val _showBookmarksHistory = MutableStateFlow(false)
    val showBookmarksHistory: StateFlow<Boolean> = _showBookmarksHistory.asStateFlow()

    private val _selectedBookmarksHistoryTab = MutableStateFlow(0) // 0: Bookmarks, 1: History
    val selectedBookmarksHistoryTab: StateFlow<Int> = _selectedBookmarksHistoryTab.asStateFlow()

    private val _showDownloads = MutableStateFlow(false)
    val showDownloads: StateFlow<Boolean> = _showDownloads.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings: StateFlow<Boolean> = _showSettings.asStateFlow()

    // Find in Page state
    private val _showFindInPage = MutableStateFlow(false)
    val showFindInPage: StateFlow<Boolean> = _showFindInPage.asStateFlow()
    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    // Web Permissions & Geolocation pending requests
    private val _pendingWebPermission = MutableStateFlow<PermissionRequest?>(null)
    val pendingWebPermission: StateFlow<PermissionRequest?> = _pendingWebPermission.asStateFlow()

    private val _pendingGeolocation = MutableStateFlow<Pair<String, GeolocationPermissions.Callback>?>(null)
    val pendingGeolocation: StateFlow<Pair<String, GeolocationPermissions.Callback>?> = _pendingGeolocation.asStateFlow()

    // File Chooser for HTML forms
    var filePathCallback: ValueCallback<Array<Uri>>? = null

    // User prompt message banner (for permissions, downloads, errors)
    private val _bannerMessage = MutableStateFlow<String?>(null)
    val bannerMessage: StateFlow<String?> = _bannerMessage.asStateFlow()

    fun dismissBanner() {
        _bannerMessage.value = null
    }

    fun showBanner(message: String) {
        _bannerMessage.value = message
    }

    val currentTab: BrowserTabState?
        get() = _tabs.value.getOrNull(_currentTabIndex.value)

    fun toggleBarPosition() {
        _isBottomBar.update { !it }
    }

    fun openTabSwitcher() {
        _showTabSwitcher.value = true
    }

    fun closeTabSwitcher() {
        _showTabSwitcher.value = false
    }

    fun openBookmarksHistory(tabIndex: Int = 0) {
        _selectedBookmarksHistoryTab.value = tabIndex
        _showBookmarksHistory.value = true
    }

    fun closeBookmarksHistory() {
        _showBookmarksHistory.value = false
    }

    fun openDownloads() {
        _showDownloads.value = true
    }

    fun closeDownloads() {
        _showDownloads.value = false
    }

    fun openSettings() {
        _showSettings.value = true
    }

    fun closeSettings() {
        _showSettings.value = false
    }

    // Tabs Operations
    fun addNewTab(initialUrl: String = "", isIncognito: Boolean = false) {
        val newTab = BrowserTabState(
            url = initialUrl,
            displayUrl = initialUrl,
            title = if (initialUrl.isNotBlank()) initialUrl else if (isIncognito) "Incognito Tab" else "New Tab",
            isIncognito = isIncognito
        )
        _tabs.update { it + newTab }
        _currentTabIndex.value = _tabs.value.lastIndex
        _showTabSwitcher.value = false
        if (isIncognito) {
            showBanner("Opened in Incognito Mode")
        }
    }

    fun openNewIncognitoTab(initialUrl: String = "") {
        addNewTab(initialUrl = initialUrl, isIncognito = true)
    }

    fun selectTab(index: Int) {
        if (index in _tabs.value.indices) {
            _currentTabIndex.value = index
            _showTabSwitcher.value = false
        }
    }

    fun closeTab(index: Int) {
        val currentList = _tabs.value
        if (currentList.size <= 1) {
            val wasIncognito = currentList.firstOrNull()?.isIncognito == true
            currentList.firstOrNull()?.webView?.let {
                WebViewManager.destroyWebView(it, wasIncognito)
            }
            _tabs.value = listOf(BrowserTabState())
            _currentTabIndex.value = 0
            return
        }

        val updatedList = currentList.toMutableList()
        val removed = updatedList.removeAt(index)
        removed.webView?.let {
            WebViewManager.destroyWebView(it, removed.isIncognito)
        }

        _tabs.value = updatedList
        if (_currentTabIndex.value >= updatedList.size) {
            _currentTabIndex.value = updatedList.lastIndex
        } else if (index < _currentTabIndex.value) {
            _currentTabIndex.value = _currentTabIndex.value - 1
        }
    }

    fun closeAllTabs(incognitoOnly: Boolean = false) {
        if (incognitoOnly) {
            val regularTabs = _tabs.value.filter { !it.isIncognito }
            val incognitoTabs = _tabs.value.filter { it.isIncognito }
            incognitoTabs.forEach { tab ->
                tab.webView?.let { WebViewManager.destroyWebView(it, true) }
            }
            if (regularTabs.isEmpty()) {
                _tabs.value = listOf(BrowserTabState())
                _currentTabIndex.value = 0
            } else {
                _tabs.value = regularTabs
                _currentTabIndex.value = regularTabs.lastIndex
            }
            showBanner("Closed all incognito tabs")
        } else {
            _tabs.value.forEach { tab ->
                tab.webView?.let { WebViewManager.destroyWebView(it, tab.isIncognito) }
            }
            _tabs.value = listOf(BrowserTabState())
            _currentTabIndex.value = 0
        }
        _showTabSwitcher.value = false
    }

    // Tab Updates from WebView
    fun attachWebView(index: Int, webView: WebView) {
        updateTab(index) { it.copy(webView = webView) }
    }

    fun updateProgress(index: Int, progress: Int) {
        updateTab(index) {
            it.copy(
                progress = progress / 100f,
                isLoading = progress < 100
            )
        }
    }

    fun updateTitle(index: Int, title: String) {
        updateTab(index) { it.copy(title = title) }
    }

    fun onPageStarted(index: Int, url: String) {
        updateTab(index) {
            it.copy(
                url = url,
                displayUrl = url,
                isLoading = true,
                errorMessage = null
            )
        }
    }

    fun onPageFinished(
        index: Int,
        url: String,
        title: String?,
        canGoBack: Boolean,
        canGoForward: Boolean,
        isSecure: Boolean
    ) {
        val pageTitle = title ?: url
        updateTab(index) {
            it.copy(
                url = url,
                displayUrl = url,
                title = if (pageTitle.isNotBlank()) pageTitle else it.title,
                isLoading = false,
                canGoBack = canGoBack,
                canGoForward = canGoForward,
                isSecure = isSecure
            )
        }

        // Record history only for standard browsing
        val tab = _tabs.value.getOrNull(index)
        if (tab?.isIncognito != true) {
            viewModelScope.launch {
                repository.recordHistory(pageTitle, url)
            }
        }
    }

    fun onPageError(index: Int, description: String) {
        updateTab(index) {
            it.copy(
                isLoading = false,
                errorMessage = description
            )
        }
    }

    private fun updateTab(index: Int, transform: (BrowserTabState) -> BrowserTabState) {
        _tabs.update { list ->
            if (index in list.indices) {
                list.toMutableList().also { it[index] = transform(it[index]) }
            } else {
                list
            }
        }
    }

    // URL Loading and Formatting
    fun loadUrl(input: String) {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return

        val formattedUrl = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("about:", ignoreCase = true) ||
            trimmed.startsWith("file:", ignoreCase = true) -> trimmed

            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"

            else -> "https://www.google.com/search?q=" + Uri.encode(trimmed)
        }

        currentTab?.let { tab ->
            tab.webView?.loadUrl(formattedUrl)
            updateTab(_currentTabIndex.value) {
                it.copy(url = formattedUrl, displayUrl = formattedUrl, isLoading = true, errorMessage = null)
            }
        }
    }

    fun goBack(): Boolean {
        val webView = currentTab?.webView
        return if (webView?.canGoBack() == true) {
            webView.goBack()
            true
        } else {
            false
        }
    }

    fun goForward(): Boolean {
        val webView = currentTab?.webView
        return if (webView?.canGoForward() == true) {
            webView.goForward()
            true
        } else {
            false
        }
    }

    fun reload() {
        currentTab?.webView?.reload()
    }

    fun stopLoading() {
        currentTab?.webView?.stopLoading()
        updateTab(_currentTabIndex.value) { it.copy(isLoading = false) }
    }

    fun navigateHome() {
        updateTab(_currentTabIndex.value) {
            it.copy(
                url = "",
                displayUrl = "",
                title = "New Tab",
                isLoading = false,
                errorMessage = null
            )
        }
        currentTab?.webView?.loadUrl("about:blank")
    }

    // Desktop Site toggle
    fun toggleDesktopMode() {
        val tab = currentTab ?: return
        val newMode = !tab.isDesktopMode
        updateTab(_currentTabIndex.value) { it.copy(isDesktopMode = newMode) }
        tab.webView?.let { webView ->
            WebViewManager.applyDesktopMode(webView, newMode)
            webView.reload()
        }
    }

    // Bookmarks
    fun toggleBookmark() {
        val tab = currentTab ?: return
        if (tab.url.isBlank()) return

        viewModelScope.launch {
            val isBookmarkedNow = bookmarks.value.any { it.url == tab.url }
            if (isBookmarkedNow) {
                repository.removeBookmarkByUrl(tab.url)
                showBanner("Bookmark removed")
            } else {
                repository.addBookmark(tab.title, tab.url)
                showBanner("Bookmark added")
            }
        }
    }

    fun removeBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            repository.removeBookmark(bookmark)
        }
    }

    // History
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showBanner("History cleared")
        }
    }

    fun removeHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            repository.removeHistoryItem(item)
        }
    }

    // Fullscreen Video Handling
    fun showFullscreenView(view: View, callback: WebChromeClient.CustomViewCallback) {
        customViewCallback = callback
        _fullscreenCustomView.value = view
    }

    fun hideFullscreenView() {
        customViewCallback?.onCustomViewHidden()
        customViewCallback = null
        _fullscreenCustomView.value = null
    }

    // WebRTC Permission handling
    fun setPendingWebPermission(request: PermissionRequest) {
        _pendingWebPermission.value = request
    }

    fun grantWebPermission(resources: Array<String>) {
        _pendingWebPermission.value?.grant(resources)
        _pendingWebPermission.value = null
    }

    fun denyWebPermission() {
        _pendingWebPermission.value?.deny()
        _pendingWebPermission.value = null
    }

    // Geolocation Handling
    fun setPendingGeolocation(origin: String, callback: GeolocationPermissions.Callback) {
        _pendingGeolocation.value = Pair(origin, callback)
    }

    fun grantGeolocation(remember: Boolean = true) {
        _pendingGeolocation.value?.let { (origin, callback) ->
            callback.invoke(origin, true, remember)
        }
        _pendingGeolocation.value = null
    }

    fun denyGeolocation(remember: Boolean = false) {
        _pendingGeolocation.value?.let { (origin, callback) ->
            callback.invoke(origin, false, remember)
        }
        _pendingGeolocation.value = null
    }

    // Downloads
    fun handleDownloadRequest(request: DownloadRequest) {
        val context = getApplication<Application>()
        try {
            val fileName = URLUtil.guessFileName(request.url, request.contentDisposition, request.mimeType)
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager

            if (downloadManager == null) {
                showBanner("Download Manager unavailable")
                return
            }

            val downloadUri = Uri.parse(request.url)
            val dmRequest = DownloadManager.Request(downloadUri).apply {
                setTitle(fileName)
                setDescription("Downloading $fileName")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setMimeType(request.mimeType)
                addRequestHeader("User-Agent", request.userAgent)
            }

            val downloadId = downloadManager.enqueue(dmRequest)

            viewModelScope.launch {
                val fileUri = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                ).toURI().toString()

                repository.recordDownload(
                    downloadId = downloadId,
                    fileName = fileName,
                    fileUri = fileUri,
                    mimeType = request.mimeType,
                    fileSize = request.contentLength
                )
            }

            showBanner("Download started: $fileName")
        } catch (e: Exception) {
            showBanner("Download failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun removeDownload(download: DownloadEntity) {
        viewModelScope.launch {
            repository.removeDownload(download)
        }
    }

    // Find in Page
    fun openFindInPage() {
        _showFindInPage.value = true
    }

    fun closeFindInPage() {
        _showFindInPage.value = false
        _findQuery.value = ""
        currentTab?.webView?.clearMatches()
    }

    fun setFindQuery(query: String) {
        _findQuery.value = query
        if (query.isNotBlank()) {
            currentTab?.webView?.findAllAsync(query)
        } else {
            currentTab?.webView?.clearMatches()
        }
    }

    fun findNext() {
        currentTab?.webView?.findNext(true)
    }

    fun findPrevious() {
        currentTab?.webView?.findNext(false)
    }

    override fun onCleared() {
        super.onCleared()
        _tabs.value.forEach { it.webView?.destroy() }
    }
}
