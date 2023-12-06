package com.mobdev20.nhom09.quicknote.datasources

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ConflictWorker @Inject constructor(
    private val firebaseNote: FirebaseNote,
    private val noteSave: NoteSave,
    appContext: Context,
    workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {
    // Note to launch in coroutine
    override fun doWork(): Result {
        val noteId = inputData.getString("cloudId") ?: return Result.failure()
        val currentId = inputData.getString("currentId") ?: return Result.failure()
        var noteState: NoteState?
        var currentState: NoteState?
        var result: Result = Result.failure()
        val data = Data.Builder().put("historyAdd", emptyList<NoteHistory>())
        runBlocking {
            noteState = firebaseNote.restore(id = noteId)
            noteSave.loadNote(currentId).collect {
                currentState = it
                if (currentState != null && noteState != null) {
                    if (currentState!!.timeRestore.isAfter(noteState!!.timeUpdate)) {
                        result = Result.success()
                        return@collect
                    } else {
                        var index = 0
                        for (i in 0 until noteState!!.history.size) {
                            if (currentState!!.history[i].timestamp != currentState!!.history[i].timestamp
                                && currentState!!.history[i].contentNew != noteState!!.history[i].contentNew
                            ) {
                                index = i
                                break
                            }
                        }

                        var add = 0
                        val histories = mutableListOf<NoteHistory>()
                        for (i in index until noteState!!.history.size) {
                            histories.add(noteState!!.history[i])
                            if (noteState!!.history[i].type == HistoryType.ADD) {
                                add++
                            } else if (noteState!!.history[i].type == HistoryType.DELETE) {
                                add--
                            }
                        }
                        for (i in index until currentState!!.history.size) {
                            val line = currentState!!.history[i].line
                            histories.add(currentState!!.history[i].copy(line = line + add))
                        }
                        data.put("historyAdd", histories)
                        result = Result.success(data.build())
                        return@collect
                    }
                } else {
                    return@collect
                }
            }
        }
        return result
    }

}