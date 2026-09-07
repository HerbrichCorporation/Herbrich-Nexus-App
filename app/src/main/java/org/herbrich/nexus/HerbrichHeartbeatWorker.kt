package org.herbrich.nexus

import android.accounts.AccountManager
import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import androidx.work.ListenableWorker

class HerbrichHeartbeatWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): ListenableWorker.Result {
        val am = AccountManager.get(applicationContext)
        val account = am.getAccountsByType("org.herbrich.accounts").firstOrNull()
            ?: return ListenableWorker.Result.failure()

        val token = am.peekAuthToken(account, "FullAccess")
            ?: return ListenableWorker.Result.failure()

        return try {
            val resp = RetrofitClient.instance.heartbeat("Bearer $token")

            val delay = resp.NextHeartbeatIn - 60
            val next = OneTimeWorkRequestBuilder<HerbrichHeartbeatWorker>()
                .setInitialDelay(if (delay < 60) 60 else delay, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(next)

            ListenableWorker.Result.success()
        } catch (e: Exception) {
            if (e is retrofit2.HttpException && e.code() == 401) {
                am.invalidateAuthToken(account.type, token)
            }
            ListenableWorker.Result.retry()
        }
    }
}