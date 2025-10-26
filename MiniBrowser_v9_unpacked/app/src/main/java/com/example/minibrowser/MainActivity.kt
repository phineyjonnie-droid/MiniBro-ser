package com.veestores.minibrowser

import android.annotation.SuppressLint
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import com.veestores.minibrowser.BlocklistUpdateWorker
import com.veestores.minibrowser.BrowserWebViewClient
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.CopyOnWriteArraySet

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var urlEdit: EditText
    private lateinit var swipe: SwipeRefreshLayout
    private var incognito = false
    private var tabs = mutableListOf<Tab>()
    private var activeIndex = 0

    private val blockedHosts = CopyOnWriteArraySet<String>()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enqueue one-time blocklist update
        
        super.onCreate(savedInstanceState)
        // Enqueue one-time blocklist update
        val request = OneTimeWorkRequestBuilder<com.veestores.minibrowser.BlocklistUpdateWorker>().build()
        WorkManager.getInstance(this).enqueueUniqueWork("blocklist_update_once", ExistingWorkPolicy.KEEP, request)

        // apply theme before setContentView
        ThemeManager.applyThemeMode(this, SettingsStore.getThemeMode(this))
        ThemeManager.scheduleAutoCheck(this)

        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        swipe = findViewById(R.id.swipe_refresh)
        webView = findViewById(R.id.webview)
        urlEdit = findViewById(R.id.url_edit)

        loadBlocklist()

        // Load tabs
        tabs = TabManager.loadTabs(this)
        activeIndex = TabManager.getActiveIndex(this)
        if (intent?.hasExtra("open_url") == true) {
            val url = intent.getStringExtra("open_url") ?: ""
            tabs.add(Tab(id = java.util.UUID.randomUUID().toString(), url = url))
            activeIndex = tabs.size - 1
        }

        applyTabToWebView()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = !SettingsStore.isDataSaverOn(this@MainActivity)
        }

        swipe.setOnRefreshListener {
            webView.reload()
        }

        webView.webViewClient = BrowserWebViewClient(this)
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                request?.url?.host?.let { host ->
                    if (blockedHosts.any { host.contains(it) }) {
                        return WebResourceResponse("text/plain", "utf-8", null)
                    }
                    // data saver: block images and large resources heuristically
                    if (SettingsStore.isDataSaverOn(this@MainActivity)) {
                        val path = request.url.encodedPath ?: ""
                        if (path.matches(Regex(".*\.(png|jpg|jpeg|gif|webp)$", RegexOption.IGNORE_CASE))) {
                            return WebResourceResponse("image/png", "utf-8", null)
                        }
                        // block common video types
                        if (path.matches(Regex(".*\.(mp4|webm|ogg)$", RegexOption.IGNORE_CASE))) {
                            return WebResourceResponse("text/plain", "utf-8", null)
                        }
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                swipe.isRefreshing = false
                url?.let {
                    urlEdit.setText(it)
                    tabs[activeIndex].url = it
                    captureThumbnailForActiveTab()
                    // inject JS to stop video/audio autoplay when data saver is on
                    if (SettingsStore.isDataSaverOn(this@MainActivity)) {
                        try {
                            view?.evaluateJavascript("""(function(){var els=document.querySelectorAll('video, audio'); for(var i=0;i<els.length;i++){els[i].pause(); els[i].preload='none';}})();""", null)
                        } catch (e: Exception) { }
                    }
                }
            }
        }

        webView.setDownloadListener { url, _, _, _, _ ->
            try {
                val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val request = DownloadManager.Request(Uri.parse(url))
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                dm.enqueue(request)
                Toast.makeText(this, "Download started", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        urlEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                loadUrl(urlEdit.text.toString())
                true
            } else false
        }

        if (tabs.isEmpty()) {
            tabs.add(Tab(id = java.util.UUID.randomUUID().toString(), url = SettingsStore.getHomepage(this)))
            activeIndex = 0
        }

        applyTabToWebView()
    }

    override fun onPause() {
        super.onPause()
        TabManager.saveTabs(this, tabs, activeIndex)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val inc = menu?.findItem(R.id.action_incognito)
        inc?.isChecked = incognito
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_bookmarks -> { startActivity(Intent(this, BookmarkActivity::class.java)); true }
            R.id.action_incognito -> {
                val new = !item.isChecked
                item.isChecked = new
                incognito = new
                if (incognito) { webView.clearHistory(); webView.clearCache(true); Toast.makeText(this, "Incognito ON", Toast.LENGTH_SHORT).show() }
                else Toast.makeText(this, "Incognito OFF", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_add_bookmark -> { BookmarkActivity.addBookmark(this, webView.url ?: ""); Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show(); true }
            R.id.action_tabs -> { startActivity(Intent(this, TabSwitcherActivity::class.java)); overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); true }
            R.id.action_settings -> { startActivity(Intent(this, SettingsActivity::class.java)); overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); true }
            R.id.action_ai -> { startActivity(Intent(this, AIActivity::class.java)); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadBlocklist() {
        try {
            val f = File(filesDir, "blocklist.txt")
            if (f.exists()) {
                f.readLines().forEach { val line = it.trim(); if (line.isNotEmpty() && !line.startsWith("#")) blockedHosts.add(line) }
                return
            }
            val ins = assets.open("blocklist.txt")
            val br = BufferedReader(InputStreamReader(ins))
            br.useLines { lines -> lines.forEach { val line = it.trim(); if (line.isNotEmpty() && !line.startsWith("#")) blockedHosts.add(line) } }
        } catch (e: Exception) {}
    }

    private fun applyTabToWebView() {
        if (activeIndex < 0 || activeIndex >= tabs.size) activeIndex = 0
        val t = tabs[activeIndex]
        urlEdit.setText(t.url)
        webView.loadUrl(t.url)
    }

    private fun loadUrl(input: String) {
        var url = input.trim()
        if (url.isEmpty()) return
        if (!url.startsWith("http")) {
            val search = SettingsStore.getSearchEngineQuery(this, url)
            url = search
        }
        webView.loadUrl(url)
        tabs[activeIndex].url = url
    }

    private fun captureThumbnailForActiveTab() {
        try {
            val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)
            val fname = "tab_${tabs[activeIndex].id}.png"
            val f = File(cacheDir, fname)
            val fos = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos)
            fos.flush()
            fos.close()
            tabs[activeIndex].thumbPath = f.absolutePath
        } catch (e: Exception) {}
    }
}
