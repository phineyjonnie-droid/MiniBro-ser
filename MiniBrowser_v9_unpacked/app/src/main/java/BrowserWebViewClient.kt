package com.veestores.minibrowser

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * BrowserWebViewClient - integrates BlocklistMatcher in silent mode.
 *
 * This implementation preserves existing default behaviors and blocks matching URLs silently
 * by returning an empty WebResourceResponse. It avoids logging or noisy output.
 */
class BrowserWebViewClient(private val context: Context) : WebViewClient() {

    private val matcher: BlocklistMatcher by lazy { BlocklistMatcher.get(context) }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        // Allow external schemes to be handled by other apps
        val url = request?.url?.toString() ?: return false
        if (!(url.startsWith("http://") || url.startsWith("https://"))) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (e: Exception) {
                // silent fallback
                return true
            }
        }
        return false
    }

    override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
        try {
            val url = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
            // Use BlocklistMatcher to decide whether to block
            if (matcher.matches(url)) {
                // silently block by returning empty response
                return WebResourceResponse("text/plain", "utf-8", java.io.ByteArrayInputStream(byteArrayOf()))
            }
        } catch (e: Exception) {
            // Silent: do not log to avoid noisy output
        }
        return super.shouldInterceptRequest(view, request)
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceResponse?) {
        super.onReceivedError(view, request, error)
    }
}
