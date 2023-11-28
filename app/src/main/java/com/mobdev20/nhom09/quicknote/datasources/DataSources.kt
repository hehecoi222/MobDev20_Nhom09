package com.mobdev20.nhom09.quicknote.datasources

import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File

class DataSources {
    private val storage = Firebase.storage

    fun uploadFile(path: String) {
        val storageRef = storage.reference.child(path)
        val file = Uri.fromFile(File(path))
        val uploadTask = storageRef.putFile(file)

        uploadTask.addOnSuccessListener {
            // The file was successfully uploaded
        }.addOnFailureListener {
            // The upload failed
        }
    }

    fun downloadFile(path: String) {
        val storageRef = storage.reference.child(path)
        val localPath = "path/to/local/storage/location"
        val localFile = File.createTempFile(localPath, "extension") // replace "extension" with your file's extension

        storageRef.getFile(localFile).addOnSuccessListener {
            // The file was successfully downloaded
        }.addOnFailureListener {
            // The download failed
        }
    }
}