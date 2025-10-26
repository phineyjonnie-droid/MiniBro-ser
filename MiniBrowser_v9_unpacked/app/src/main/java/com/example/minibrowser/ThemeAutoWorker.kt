package com.veestores.minibrowser

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ThemeAutoWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ThemeManager.applyAutoByTime()
        return Result.success()
    }
}
