package com.mobdev20.nhom09.quicknote.repositories.implementations

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.mobdev20.nhom09.quicknote.datasources.FirebaseNote
import com.mobdev20.nhom09.quicknote.datasources.NoteDataStore
import com.mobdev20.nhom09.quicknote.datasources.SyncWorker
import com.mobdev20.nhom09.quicknote.repositories.BackupNote
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        noteSave.loadNote(id).collect {
            if (it != null) {
                firebaseNote.backup(it)
                return@collect
            }
        }
    }

    override suspend fun restore(id: String): NoteState? {
        return firebaseNote.restore(id)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NoteBackupModule {
    @Binds
    abstract fun bindNoteBackup(noteBackupImpl: NoteBackupImpl): BackupNote
}