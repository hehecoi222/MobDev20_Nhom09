package com.mobdev20.nhom09.quicknote.datasources

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.DocumentSnapshot
import org.json.JSONObject
import java.io.File
import android.util.Log
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.StorageException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.state.NoteState


class DataSources {
    private val collection = "Notes"
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage

    fun upload(jsonString: String){ //argument userID for collection
        uploadData(jsonToData(jsonString))
    }


    private fun jsonToData(jsonString: String): Map<String, Any> {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(jsonString)
    }

    private fun uploadData(data: Map<String, Any>) {
        val docID = data["id"] as String

        db.collection(collection)
            .document(docID)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                val paths = data["attachmentPaths"] as MutableList<*>

                paths.forEach{
                    uploadImg(it as String,  cloudPath(it, docID))
                }

                Log.i("UploadComplete", "Success upload")
            }
            .addOnFailureListener {
                Log.e("!UploadComplete", it.toString())
            }
    }

    private fun cloudPath(localPath: String, docID: String) : String{
        return docID + "/" + File(localPath).name // userID + "/" + docID + "/" + file.name
    }

    fun uploadImg(localPath: String, cloudPath: String) {
        val storageRef = storage.reference.child(cloudPath)
        val file = Uri.fromFile(File(localPath))
        val uploadTask = storageRef.putFile(file)

        uploadTask.addOnSuccessListener {
            Log.i("UploadComplete", "Success upload image")
        }.addOnFailureListener {
                exception ->
            Log.e("!UploadComplete", exception.message.toString())
            Log.e("!UploadComplete", "localStorage: $localPath")
            Log.e("!UploadComplete", "cloudStorage: $cloudPath")
            when (exception) {
                is StorageException -> {
                    Log.e("!UploadComplete",
                        "HTTP result code:" + exception.httpResultCode)
                    exception.cause.let { cause ->
                        Log.e("!UploadComplete", "Inner exception: ", cause)
                    }
                }
                else -> {
                }
            }
        }
    }

    fun download(noteID: String) {
        downloadData(noteID)
    }

    private fun downloadData(docID: String) {
        db.collection(collection)
            .document(docID)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    val jsonObject = docToJson(documentSnapshot)
                    val paths = jsonObject.get("attachmentPaths") as MutableList<*>

                    paths.forEach{
                        downloadImg(cloudPath(it as String, docID), it)
                    }

                    createNoteState(docToJsonString(documentSnapshot))
                    Log.i("DownloadSuccess", docToJsonString(documentSnapshot))
                } else {
                    Log.e("!DownloadSuccess", "No such document")
                }
            }
            .addOnFailureListener {
                Log.e("!DownloadSuccess", it.toString())
            }
    }

    private fun createNoteState(jsonString: String) : NoteState {
        return NoteJson.convertJson(jsonString)
    }

    private fun docToJson(docSnapshot: DocumentSnapshot) : JSONObject {
        val data = docSnapshot.data
        val jsonObject = JSONObject()
        if (data != null) {
            for (entry in data.entries) {
                jsonObject.put(entry.key, entry.value)
            }
        }
        return jsonObject
    }

    private fun docToJsonString(docSnapshot: DocumentSnapshot?) : String {
        val data = docSnapshot!!.data
        val mapper = jacksonObjectMapper()
        return mapper.writeValueAsString(data)
    }

    private fun downloadImg(cloudPath: String, localPath: String) {
        val storageRef = storage.reference.child(cloudPath)
        val localFile = File(localPath)

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                Log.d("DownloadSuccess", "Download Image success")
                Log.d("DownloadSuccess", "localStorage: $localPath")
            }.addOnFailureListener { exception ->
                Log.e("!DownloadSuccess", exception.message.toString())
                Log.e("!DownloadSuccess", "localStorage: $localPath")
                Log.e("!DownloadSuccess", "cloudStorage: $cloudPath")
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
            }
    }
}