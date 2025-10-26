
package com.veestores.minibrowser

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.Request
import javax.inject.Inject

@HiltWorker
class BlocklistUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BlocklistRepository,
    private val okHttpClient: okhttp3.OkHttpClient
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val urls = listOf(
                "https://easylist.to/easylist/easylist.txt",
                "https://easylist.to/easylist/easyprivacy.txt"
            )
            val allPatterns = mutableListOf<String>()
            for (u in urls) {
                val req = Request.Builder().url(u).get().build()
                val resp = okHttpClient.newCall(req).execute()
                if (resp.isSuccessful) {
                    val body = resp.body?.string() ?: ""
                    val lines = body.lines().map { it.trim() }.filter { it.isNotEmpty() }
                    allPatterns.addAll(lines)
                }
            }
            // store into Room DB
            repository.replaceAll(allPatterns)
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
