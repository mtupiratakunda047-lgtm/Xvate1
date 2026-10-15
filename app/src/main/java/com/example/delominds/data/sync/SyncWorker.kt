package com.example.delominds.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Periodic background sync, scheduled from MainActivity only while the phone has a connection.
 * Sends queued changes and refreshes the offline copy. Does nothing until a teacher has signed in to the server.
 */
class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
  override suspend fun doWork(): ListenableWorker.Result = withContext(Dispatchers.IO) {
    val client = ServerClient(applicationContext)
    if (!client.isSignedIn()) return@withContext ListenableWorker.Result.success()
    val report = SyncEngine(client, OfflineStore(applicationContext)).syncNow()
    if (report.online) ListenableWorker.Result.success() else ListenableWorker.Result.retry()
  }
}
