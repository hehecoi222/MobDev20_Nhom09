package com.mobdev20.nhom09.quicknote.repositories.implementations

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mobdev20.nhom09.quicknote.datasources.FirebaseNote
import com.mobdev20.nhom09.quicknote.datasources.NoteDataStore
import com.mobdev20.nhom09.quicknote.datasources.SyncWorker
import com.mobdev20.nhom09.quicknote.repositories.BackupNote
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NoteBackupImpl @Inject constructor(@ApplicationContext private val context: Context) : BackupNote {
//    @Inject
//    lateinit var noteDataStore: NoteDataStore

    @Inject
    lateinit var firebaseNote: FirebaseNote

    @Inject
    lateinit var noteSave: NoteSave

    override suspend fun backup(id: String) {
//        noteDataStore.readFrom(id).collect {
//            if (it != null) {
//                val myUploadWork = OneTimeWorkRequestBuilder<SyncWorker>().setInputData(workDataOf(
//                    "notestate" to it
//                )).build()
//                WorkManager.getInstance(context).enqueue(myUploadWork)
//            }
//
//        }
        noteSave.loadNote(id).collect { it ->
            if (it != null) {
                val status = firebaseNote.backup(it)
                status.cancellable().collect { status ->
                    if (status) {
                        Log.d("FIREBASE", "backup success")
                    } else {
                        Log.d("FIREBASE", "backup failed")
                    }
                }
                return@collect
            }
        }
    }

    override suspend fun restore(id: String): Flow<Pair<Int, List<NoteHistory>>?> = flow {
        var noteState: NoteState?
        var currentState: NoteState?
        with(Dispatchers.IO) {
            noteState = firebaseNote.restore(id = id)
            noteSave.loadNote(id).collect {
                currentState = it
                if (currentState != null && noteState != null) {
                    if (currentState!!.timeRestore.isAfter(noteState!!.timeUpdate)) {
                        emit(null)
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
                        emit(Pair(index, histories))
                        return@collect
                    }
                } else if (noteState != null && currentState == null) {
                    Log.d("FIREBASE", "Downloaded")
                    noteSave.update(noteState!!)
                    emit(Pair(-1, emptyList()))
                }
                else {
                    emit(null)
                    return@collect
                }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteBackupModule {
    @Binds
    abstract fun bindNoteBackup(noteBackupImpl: NoteBackupImpl): BackupNote
}