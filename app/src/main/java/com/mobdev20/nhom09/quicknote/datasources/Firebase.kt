package com.mobdev20.nhom09.quicknote.datasources

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.tasks.await
import java.time.Instant
import javax.inject.Inject

interface FirebaseNote {
    fun backup(noteState: NoteState)
    suspend fun restore(id: String)
}

class FirebaseImpl @Inject constructor() : FirebaseNote {
    private val db = Firebase.firestore

    @Inject
    lateinit var noteSave: NoteSave

    override fun backup(noteState: NoteState) {
        db.collection("notes").document(noteState.id).set(noteState).addOnSuccessListener {
            Log.d("FIREBASE", "updating note")
        }.addOnFailureListener {
            Log.d("FIREBASE", "failed to update note" + it.message)
        }
    }

    override suspend fun restore(id: String) {
        val docRef = db.collection("notes").document(id)
        var noteState: NoteState? = null
        docRef.get().addOnSuccessListener { docs ->
            noteState = NoteState(
                id = (docs.data?.get("id")).toString(),
                userId = (docs.data?.get("user")).toString(),
                title = (docs.data?.get("title")).toString(),
                content = (docs.data?.get("content")).toString(),
                timeRestore = Instant.now(),
                history = ((docs.data?.get("history")) as List<Map<*, *>>).map {
                    NoteHistory(
                        contentOld = it.get("contentOld").toString(),
                        contentNew = it.get("contentNew").toString(),
                        line = it.get("line").toString().toInt(),
                        type = HistoryType.valueOf(it.get("type").toString()),
                        userId = it.get("userId").toString()
                    )
                }.toMutableList(),
                attachments = ((docs.data?.get("attachments")) as List<String>).toMutableList()
            )
            Log.d("FIREBASE", noteState.toString())
        }.await()
        if (noteState != null) {
            noteSave.update(noteState!!)
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    @Binds
    abstract fun bindFirebaseImpl(firebaseImpl: FirebaseImpl): FirebaseNote
}