package com.veestores.minibrowser

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * BlocklistUpdateWorker - Silent background fetcher for EasyList + EasyPrivacy
 * Runs once at first launch. Saves files to context.filesDir.
 */
class BlocklistUpdateWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val urls = listOf(
            "https://easylist.to/easylist/easylist.txt",
            "https://easylist.to/easylist/easyprivacy.txt"
        )
        try {
            for ((i, u) in urls.withIndex()) {
                val name = if (i == 0) "easylist_subset.txt" else "blocklist.txt"
                val file = File(applicationContext.filesDir, name)
                downloadToFile(u, file)
            }
            return Result.success()
        } catch (e: Exception) {
            // Silent fail
            return Result.retry()
        }
    }

    private fun downloadToFile(urlStr: String, dest: File) {
        val url = URL(urlStr)
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.inputStream.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        conn.disconnect()
    }
}
