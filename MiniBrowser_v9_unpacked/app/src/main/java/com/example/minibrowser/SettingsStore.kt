package com.veestores.minibrowser

import android.content.Context
import android.net.Uri

object SettingsStore {
    private const val PREFS = "minibrowser_settings"
    private const val KEY_SEARCH = "search_template"
    private const val KEY_HOME = "homepage"
    private const val KEY_BLOCK_URL = "block_url"
    private const val KEY_AI_ENDPOINT = "ai_endpoint"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_DATA_SAVER = "data_saver"

    private const val DEFAULT_SEARCH = "https://www.google.com/search?q={query}"
    private const val DEFAULT_HOME = "https://www.google.com"
    private const val DEFAULT_BLOCK_URL = "https://example.com/blocklist.txt"
    private const val DEFAULT_AI_ENDPOINT = ""
    private const val DEFAULT_THEME = "system"
    private const val DEFAULT_DATA_SAVER = false

    fun getSearchTemplate(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_SEARCH, DEFAULT_SEARCH) ?: DEFAULT_SEARCH
    }
    fun setSearchTemplate(ctx: Context, tpl: String) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_SEARCH, tpl).apply()
    }
    fun getHomepage(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_HOME, DEFAULT_HOME) ?: DEFAULT_HOME
    }
    fun setHomepage(ctx: Context, url: String) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_HOME, url).apply() }
    fun getBlocklistUrl(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BLOCK_URL, DEFAULT_BLOCK_URL) ?: DEFAULT_BLOCK_URL
    }
    fun setBlocklistUrl(ctx: Context, url: String) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_BLOCK_URL, url).apply() }
    fun getAiEndpoint(ctx: Context): String { val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE); return prefs.getString(KEY_AI_ENDPOINT, DEFAULT_AI_ENDPOINT) ?: DEFAULT_AI_ENDPOINT }
    fun setAiEndpoint(ctx: Context, url: String) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_AI_ENDPOINT, url).apply() }

    fun getThemeMode(ctx: Context): String { val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE); return prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME }
    fun setThemeMode(ctx: Context, mode: String) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THEME, mode).apply() }

    fun isDataSaverOn(ctx: Context): Boolean { val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE); return prefs.getBoolean(KEY_DATA_SAVER, DEFAULT_DATA_SAVER) }
    fun setDataSaver(ctx: Context, on: Boolean) { ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY_DATA_SAVER, on).apply() }

    fun getSearchEngineQuery(ctx: Context, queryOrUrl: String): String {
        val t = queryOrUrl.trim()
        return if (t.startsWith("http")) t else {
            val tpl = getSearchTemplate(ctx)
            tpl.replace("{query}", Uri.encode(t))
        }
    }
}
