package com.example.data

import kotlinx.coroutines.flow.Flow

class BrowserRepository(private val dao: BrowserDao) {

    val bookmarks: Flow<List<BookmarkEntity>> = dao.getAllBookmarks()
    val history: Flow<List<HistoryEntity>> = dao.getRecentHistory()
    val downloads: Flow<List<DownloadEntity>> = dao.getAllDownloads()

    fun isBookmarked(url: String): Flow<Boolean> = dao.isBookmarked(url)

    suspend fun addBookmark(title: String, url: String) {
        if (url.isNotBlank()) {
            dao.insertBookmark(BookmarkEntity(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun removeBookmarkByUrl(url: String) {
        dao.deleteBookmarkByUrl(url)
    }

    suspend fun removeBookmark(bookmark: BookmarkEntity) {
        dao.deleteBookmark(bookmark)
    }

    suspend fun recordHistory(title: String, url: String) {
        if (url.isNotBlank() && !url.startsWith("about:") && !url.startsWith("data:")) {
            dao.insertHistory(HistoryEntity(title = title.ifBlank { url }, url = url))
        }
    }

    suspend fun clearHistory() {
        dao.clearAllHistory()
    }

    suspend fun removeHistoryItem(history: HistoryEntity) {
        dao.deleteHistory(history)
    }

    suspend fun recordDownload(
        downloadId: Long,
        fileName: String,
        fileUri: String,
        mimeType: String,
        fileSize: Long
    ) {
        dao.insertDownload(
            DownloadEntity(
                downloadId = downloadId,
                fileName = fileName,
                fileUri = fileUri,
                mimeType = mimeType,
                fileSize = fileSize
            )
        )
    }

    suspend fun removeDownload(download: DownloadEntity) {
        dao.deleteDownload(download)
    }
}
