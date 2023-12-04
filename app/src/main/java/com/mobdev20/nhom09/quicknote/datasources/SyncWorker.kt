package com.mobdev20.nhom09.quicknote.datasources

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import javax.inject.Inject

class SyncWorker @Inject constructor(private val firebaseNote: FirebaseNote, appContext: Context, workerParameters: WorkerParameters) :
    Worker(appContext, workerParameters) {

    override fun doWork(): Result {
        val noteState = inputData.getString("notestate") ?: return Result.failure()
        firebaseNote.backup(NoteJson.convertJson(noteState))
        return Result.success()
    }
}