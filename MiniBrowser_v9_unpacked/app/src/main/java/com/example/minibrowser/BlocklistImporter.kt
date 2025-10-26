package com.veestores.minibrowser

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

object BlocklistImporter {
    fun importFromAssets(ctx: Context, assetName: String = "easylist_subset.txt"): List<String> {
        val results = mutableSetOf<String>()
        try {
            val ins = ctx.assets.open(assetName)
            val br = BufferedReader(InputStreamReader(ins))
            br.useLines { lines ->
                lines.forEach { line ->
                    val l = line.trim()
                    if (l.isEmpty() || l.startsWith("!")) return@forEach
                    if (l.startsWith("||")) {
                        val raw = l.removePrefix("||")
                        val clean = raw.substringBefore("^").substringBefore("/").trim()
                        if (clean.isNotEmpty()) results.add(clean)
                    }
                }
            }
        } catch (e: Exception) {}
        return results.toList()
    }
    fun mergeIntoBlocklist(ctx: Context, hosts: List<String>) {
        try {
            val f = ctx.getFileStreamPath("blocklist.txt")
            val existing = if (f.exists()) f.readText().lines().map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet() else mutableSetOf()
            existing.addAll(hosts)
            f.writeText(existing.joinToString("\n"))
        } catch (e: Exception) {}
    }
}


// TODO: MOVE_NETWORK_IO_TO_BACKGROUND - ensure downloads are performed off the UI thread (use suspend functions/CoroutineWorker)
