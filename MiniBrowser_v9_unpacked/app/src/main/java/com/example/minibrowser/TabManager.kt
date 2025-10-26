package com.veestores.minibrowser

import android.content.Context
import org.json.JSONArray
import java.util.UUID

object TabManager {
    private const val PREFS = "minibrowser_tabs"
    private const val KEY_TABS = "tabs"
    private const val KEY_ACTIVE = "active"

    fun loadTabs(ctx: Context): MutableList<Tab> {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_TABS, "[]")
        val arr = JSONArray(raw)
        val list = mutableListOf<Tab>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val t = Tab(id = o.optString("id", UUID.randomUUID().toString()), title = o.optString("title",""), url = o.optString("url","about:blank"), isIncognito = o.optBoolean("incognito", false), thumbPath = o.optString("thumb",""))
            list.add(t)
        }
        if (list.isEmpty()) { list.add(Tab(id = UUID.randomUUID().toString(), url = "https://www.google.com")) }
        return list
    }

    fun saveTabs(ctx: Context, tabs: List<Tab>, activeIndex: Int) {
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val arr = JSONArray()
        for (t in tabs) {
            val o = org.json.JSONObject()
            o.put("id", t.id); o.put("title", t.title); o.put("url", t.url); o.put("incognito", t.isIncognito); o.put("thumb", t.thumbPath)
            arr.put(o)
        }
        prefs.edit().putString(KEY_TABS, arr.toString()).putInt(KEY_ACTIVE, activeIndex).apply()
    }

    fun getActiveIndex(ctx: Context): Int = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_ACTIVE, 0)
    fun setActiveIndex(ctx: Context, idx: Int) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY_ACTIVE, idx).apply()
}
