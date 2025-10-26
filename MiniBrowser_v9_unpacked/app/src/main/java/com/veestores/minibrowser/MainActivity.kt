package com.veestores.minibrowser

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import com.example.minibrowser.SettingsStore
import com.example.minibrowser.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val viewModel: BrowserViewModel by viewModels()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        setupWebView()

        lifecycleScope.launch {
            viewModel.currentUrl.collect { url ->
                url?.let { webView.loadUrl(it) }
            }
        }

        // Example: load a homepage if none set
        if (viewModel.currentUrl.value == null) {
            viewModel.loadUrl("https://www.google.com")
        }

        // Schedule blocklist update once at first launch
        scheduleBlocklistUpdateOnce()
    }

    private fun scheduleBlocklistUpdateOnce() {
        // enqueue unique work to run once
        val wm = androidx.work.WorkManager.getInstance(applicationContext)
        val request = androidx.work.OneTimeWorkRequestBuilder<com.veestores.minibrowser.BlocklistUpdateWorker>().build()
        wm.enqueueUniqueWork("blocklist_initial_fetch", androidx.work.ExistingWorkPolicy.KEEP, request)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true // toggle as needed; consider per-site controls
            domStorageEnabled = true
            safeBrowsingEnabled = true
            allowFileAccess = false
            allowContentAccess = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            // keep jvmTarget-related settings up to date
        }

        // Remove problematic JS interfaces if present
        try {
            webView.removeJavascriptInterface("searchBoxJavaBridge_")
            webView.removeJavascriptInterface("accessibility")
            webView.removeJavascriptInterface("accessibilityTraversal")
        } catch (e: Exception) {
            // ignore on older devices
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                viewModel.setLoading(true)
                super.onPageStarted(view, url, favicon)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                viewModel.setLoading(false)
                super.onPageFinished(view, url)
            }

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                // Simple host blocking example: check SettingsStore for blocklist
                request?.url?.host?.let { host ->
                    val blockedHosts = SettingsStore.getBlockedHosts(this@MainActivity)
                    if (blockedHosts.any { host.contains(it) }) {
                        return WebResourceResponse("text/plain", "utf-8", null)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Example helper to capture thumbnail for a tab
    private fun captureTabThumbnail(tabId: String) {
        try {
            val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            webView.draw(canvas)
            val fname = "tab_${tabId}.png"
            val f = File(cacheDir, fname)
            val fos = FileOutputStream(f)
            bitmap.compress(Bitmap.CompressFormat.PNG, 70, fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            // ignore capture errors
        }
    }
}
