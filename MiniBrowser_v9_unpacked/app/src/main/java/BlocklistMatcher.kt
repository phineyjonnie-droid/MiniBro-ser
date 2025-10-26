package com.veestores.minibrowser

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

/**
 * BlocklistMatcher - Silent mode
 * Lightweight pattern matcher for ad/block lists.
 * Meant to be fast and safe for shouldInterceptRequest.
 */
class BlocklistMatcher private constructor(private val context: Context) {

    private val compiled = mutableListOf<Pattern>()
    private val loaded = AtomicBoolean(false)

    companion object {
        @Volatile private var INSTANCE: BlocklistMatcher? = null
        fun get(context: Context): BlocklistMatcher =
            INSTANCE ?: synchronized(this) { INSTANCE ?: BlocklistMatcher(context.applicationContext).also { INSTANCE = it } }
    }

    fun load(force: Boolean = false) {
        if (loaded.get() && !force) return
        compiled.clear()
        val files = mutableListOf<File>()
        try {
            val fd = context.filesDir
            files.add(File(fd, "easylist_subset.txt"))
            files.add(File(fd, "blocklist.txt"))
        } catch (e: Exception) { }

        // fallback to assets if present
        try {
            val assetNames = listOf("easylist_subset.txt", "blocklist.txt")
            for (name in assetNames) {
                try {
                    val stream = context.assets.open(name)
                    val tmp = File.createTempFile(name.replace('.', '_'), null, context.cacheDir)
                    tmp.writeBytes(stream.readBytes())
                    files.add(tmp)
                    stream.close()
                } catch (e: Exception) { }
            }
        } catch (e: Exception) { }

        for (f in files) {
            if (!f.exists()) continue
            try {
                val lines = f.readLines()
                for (raw in lines) {
                    val line = raw.trim()
                    if (line.isEmpty()) continue
                    if (line.startsWith("!") || line.startsWith("#")) continue
                    val pattern = convertLineToRegex(line) ?: continue
                    try {
                        compiled.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
                    } catch (e: Exception) {
                        // skip bad regex
                    }
                }
            } catch (e: Exception) {
                // ignore file read issues
            }
        }
        loaded.set(true)
    }

    private fun convertLineToRegex(line: String): String? {
        // regex literal /.../
        if (line.length >= 2 && line.startsWith("/") && line.endsWith("/")) {
            val inner = line.substring(1, line.length - 1)
            return inner
        }
        // domain anchor ||example.com
        if (line.startsWith("||")) {
            val host = Pattern.quote(line.substring(2))
            return "https?://([\\w\\-]+\\.)*" + host + "(/.*)?"
        }
        // anchor at start |
        if (line.startsWith("|")) {
            val rest = Pattern.quote(line.substring(1)).replace("\\*", ".*")
            return "^" + rest
        }
        // wildcard *
        if (line.contains("*")) {
            val escaped = Pattern.quote(line).replace("\\*", ".*")
            return ".*" + escaped + ".*"
        }
        val esc = Pattern.quote(line)
        return ".*" + esc + ".*"
    }

    fun matches(url: String?): Boolean {
        if (url == null) return false
        if (!loaded.get()) load(false)
        for (p in compiled) {
            try {
                if (p.matcher(url).find()) return true
            } catch (e: Exception) { }
        }
        return false
    }
}
