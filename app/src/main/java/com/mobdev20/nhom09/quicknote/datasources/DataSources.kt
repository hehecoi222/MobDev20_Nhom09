package com.mobdev20.nhom09.quicknote.datasources

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.firebase.firestore.DocumentSnapshot
import org.json.JSONObject
import java.io.File
import java.io.FileWriter


class DataSources {
    private val collection = "Note"
    private val db = FirebaseFirestore.getInstance()
    private val storage = Firebase.storage

    fun upload(jsonPath: String){ //argument userID for collection
        uploadData(jsonToData(getJsonObject(jsonPath)))
    }

    private fun getJsonObject(jsonFilePath: String) : JSONObject {
        val jsonFile = File(jsonFilePath)
        val jsonString = jsonFile.readText()

        return JSONObject(jsonString)
    }

    private fun jsonToData(jsonObject: JSONObject) : Map<String, Any> {
        val dataItem = mutableMapOf<String, Any>()

        for (key in jsonObject.keys()) {
            dataItem[key] = jsonObject.get(key) as Any
        }

        return dataItem
    }

    private fun uploadData(data: Map<String, Any>) {
        val docID = data["noteID"] as String
        val cloudPaths : MutableList<String> = mutableListOf()

        db.collection(collection)
            .document(docID)
            .set(data)
            .addOnSuccessListener {
                val paths = data["attachmentPaths"] as MutableList<*>

                paths.forEach{
                    uploadImg(it as String,  cloudPath(it, docID))
                }
            }
            .addOnFailureListener {
                //do sth when fail
            }
    }

    private fun cloudPath(localPath: String, docID: String) : String{
        return docID + "/" + File(localPath).name // userID + "/" + docID + "/" + file.name
    }

    private fun addCloudPath(cloudPath: MutableList<String>, docID: String) {
        val data: Map<String, Any> = mutableMapOf("cloudPath" to cloudPath)
        db.collection(collection)
            .document(docID)
            .update(data)
            .addOnSuccessListener {
                //do sth when success
            }
            .addOnFailureListener {
                //do sth when failure
            }
    }

    fun uploadImg(localPath: String, cloudPath: String) {
        val storageRef = storage.reference.child(cloudPath)
        val file = Uri.fromFile(File(localPath))
        val uploadTask = storageRef.putFile(file)

        uploadTask.addOnSuccessListener {
            // The file was successfully uploaded
        }.addOnFailureListener {
            // The upload failed
        }
    }

    fun download(noteID: String) {
        downloadData(noteID)
    }

    private fun downloadData(docID: String) {
        db.collection(collection)
            .document(docID)
            .get()
            .addOnSuccessListener {
                val jsonObject = docToJson(it)
                val jsonString = jsonObject.toString()
                val fileWriter = FileWriter("$docID.json")
                fileWriter.write(jsonString)
                fileWriter.close()

                val paths = jsonObject.get("attachmentPaths") as MutableList<*>

                paths.forEach{
                    downloadImg(cloudPath(it as String, docID), it)
                }
            }
            .addOnFailureListener {
                //do sth when failure
            }
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

    private fun downloadImg(cloudPath: String, localPath: String) {
        val storageRef = storage.reference.child(cloudPath)
        val localFile = File(localPath)

        storageRef.getFile(localFile)
            .addOnSuccessListener {
                
            }.addOnFailureListener {
                // The download failed
            }
    }
}