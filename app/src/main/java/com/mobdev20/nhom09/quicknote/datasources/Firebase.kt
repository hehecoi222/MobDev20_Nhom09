package com.mobdev20.nhom09.quicknote.datasources

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.mobdev20.nhom09.quicknote.repositories.NoteSave
import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.Instant
import javax.inject.Inject

interface FirebaseNote {
    fun backup(noteState: NoteState): Flow<Boolean>
    suspend fun restore(id: String): NoteState?
    suspend fun uploadFile(filePath: String, noteId: String): String

    suspend fun downloadFile(filePath: String, noteId: String)
}

class FirebaseImpl @Inject constructor() : FirebaseNote {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    @Inject
    lateinit var noteSave: NoteSave

    override fun backup(noteState: NoteState): Flow<Boolean> = flow {
        noteState.attachments.forEach {
            val job = with(Dispatchers.IO) {
                val uri = uploadFile(it, noteState.id)
                Log.d("FIREBASE", "file uploaded")
                noteState.attachments.add(uri)
            }
        }

        db.collection("notes").document(noteState.id).set(noteState).addOnSuccessListener {
            Log.d("FIREBASE", "updating note")
        }.addOnFailureListener {
            Log.d("FIREBASE", "failed to update note" + it.message)
        }
        emit(true)
    }

    override suspend fun restore(id: String): NoteState? {
        val docRef = db.collection("notes").document(id)
        var noteState: NoteState? = null
        var attachments: MutableList<String>? = null

        docRef.get().addOnSuccessListener { docs ->
            Log.d("FIREBASE", docs.data.toString())
            if(docs.data == null) {
                Log.d("FIREBASE", "No note Found")
                return@addOnSuccessListener
            }
            noteState = NoteState(
                id = (docs.data?.get("id")).toString(),
                userId = (docs.data?.get("user")).toString(),
                title = (docs.data?.get("title")).toString(),
                content = (docs.data?.get("content")).toString(),
                timeUpdate = Instant.ofEpochSecond(
                    (docs.data?.get("timeUpdate") as Map<*, *>).get("epochSecond").toString()
                        .toLong(),
                    (docs.data?.get("timeUpdate") as Map<*, *>).get("nano").toString().toLong()
                ),
                timeRestore = Instant.now(),
                history = ((docs.data?.get("history")) as List<Map<*, *>>).map {
                    NoteHistory(
                        contentOld = it.get("contentOld").toString(),
                        contentNew = it.get("contentNew").toString(),
                        line = it.get("line").toString().toInt(),
                        type = HistoryType.valueOf(it.get("type").toString()),
                        userId = it.get("userId").toString(),
                        timestamp = Instant.ofEpochSecond(
                            (it.get("timestamp") as Map<*, *>).get("epochSecond").toString()
                                .toLong(),
                            (it.get("timestamp") as Map<*, *>).get("nano").toString()
                                .toLong()
                        )
                    )
                }.toMutableList(),
                attachments = ((docs.data?.get("attachments")) as List<String>).toMutableList(),
                attachmentCount = ((docs.data?.get("attachmentCount")) as Long)
            )
            attachments = ((docs.data?.get("attachments")) as List<String>).toMutableList()
            Log.d("FIREBASE", noteState.toString())
        }.await()

        if(attachments != null) {
            attachments?.forEach{
                downloadFile(it, id)
            }
        }

        return if (noteState != null) {
            noteState
        } else {
            null
        }
    }

    override suspend fun uploadFile(filePath: String, noteId: String): String {
        val file = Uri.fromFile(File("$filePath"))
        val storageRef = storage.reference.child(noteId + "/" + file.lastPathSegment)
        val upload = storageRef.putFile(file)
        var downloadUri: Uri = Uri.EMPTY
        val uriTask = upload.continueWithTask {
            if (!upload.isSuccessful) {
                upload.exception?.let {
                    Log.wtf("FIREBASE", it.message + it.stackTrace)
                }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener {
            Log.d("FIREBASE", "upload success")
            if (upload.isSuccessful) {
                downloadUri = it.result
            }
        }.addOnFailureListener {
            Log.d("FIREBASE", "upload failed" + it.message)
        }.await()
        return uriTask.toString()
    }

    private fun cloudPath(localPath: String, docID: String) : String{
        return docID + "/" + File(localPath).name // userID + "/" + docID + "/" + file.name
    }

    override suspend fun downloadFile(filePath: String, noteId: String) {
        val storageRef = storage.reference.child(cloudPath(filePath, noteId))
        val localFile = File(filePath)

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                Log.d("DownloadSuccess", "Download Image success")
                Log.d("DownloadSuccess", "localStorage: $filePath")
            }.addOnFailureListener { exception ->
                Log.e("!DownloadSuccess", exception.message.toString())
                Log.e("!DownloadSuccess", "localStorage: $filePath")
                Log.e("!DownloadSuccess", "cloudStorage: " + cloudPath(filePath, noteId))

                when (exception) {
                    is StorageException -> {
                        Log.e("!DownloadSuccess",
                            "HTTP result code:" + exception.httpResultCode)
                        exception.cause.let { cause ->
                            Log.e("!DownloadSuccess", "Inner exception: ", cause)
                        }
                    }
                    else -> {
                    }
                }
            }.await()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseModule {
    @Binds
    abstract fun bindFirebaseImpl(firebaseImpl: FirebaseImpl): FirebaseNote
}