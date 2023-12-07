package com.mobdev20.nhom09.quicknote.datasources

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject


class SaveWorker @Inject constructor(
    private val noteSave: NoteSave,
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val noteState = inputData.getString("note") ?: return Result.failure()
        val noteId = inputData.getString("noteId") ?: return Result.failure()
        var ret = ""

        try {
            val inputStream: InputStream = applicationContext.openFileInput(noteState)
            val inputStreamReader = InputStreamReader(inputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            var receiveString: String? = ""
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { receiveString = it } != null) {
                stringBuilder.append("\n").append(receiveString)
            }
            withContext(Dispatchers.IO) {
                inputStream.close()
            }
            ret = stringBuilder.toString()
        } catch (e: FileNotFoundException) {
            Log.e("login activity", "File not found: $e")
        } catch (e: IOException) {
            Log.e("login activity", "Can not read file: $e")
        }

        setProgress(workDataOf(
            "phase" to "delay"
        ))
        delay(5000)
        setProgress(
            workDataOf(
                "phase" to "save"
            )
        )
        withContext(Dispatchers.IO) {
            noteSave.updateFromModel(noteId, ret)
        }
        return Result.success()
    }
}