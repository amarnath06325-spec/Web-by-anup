package com.example.settings

import android.content.Context
import android.content.SharedPreferences

object BrowserSettingsManager {

    private const val PREFS_NAME = "anup_web_settings"

    const val KEY_SEARCH_ENGINE = "search_engine"
    const val KEY_THEME_MODE = "theme_mode" // "system", "light", "dark"
    const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
    const val KEY_THIRD_PARTY_COOKIES = "third_party_cookies"
    const val KEY_AUTO_LOGIN_GOOGLE = "auto_login_google"

    const val SEARCH_ENGINE_GOOGLE = "Google"
    const val SEARCH_ENGINE_BING = "Bing"
    const val SEARCH_ENGINE_DUCKDUCKGO = "DuckDuckGo"
    const val SEARCH_ENGINE_YAHOO = "Yahoo"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSearchEngine(context: Context): String {
        return getPrefs(context).getString(KEY_SEARCH_ENGINE, SEARCH_ENGINE_GOOGLE) ?: SEARCH_ENGINE_GOOGLE
    }

    fun setSearchEngine(context: Context, engine: String) {
        getPrefs(context).edit().putString(KEY_SEARCH_ENGINE, engine).apply()
    }

    fun getSearchUrl(context: Context, query: String): String {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        return when (getSearchEngine(context)) {
            SEARCH_ENGINE_BING -> "https://www.bing.com/search?q=$encodedQuery"
            SEARCH_ENGINE_DUCKDUCKGO -> "https://duckduckgo.com/?q=$encodedQuery"
            SEARCH_ENGINE_YAHOO -> "https://search.yahoo.com/search?p=$encodedQuery"
            else -> "https://www.google.com/search?q=$encodedQuery"
        }
    }

    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, "system") ?: "system"
    }

    fun setThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun isAutoLoginGoogleEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTO_LOGIN_GOOGLE, true)
    }

    fun setAutoLoginGoogleEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTO_LOGIN_GOOGLE, enabled).apply()
    }
}
