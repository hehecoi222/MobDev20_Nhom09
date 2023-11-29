package com.mobdev20.nhom09.quicknote.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import com.mobdev20.nhom09.quicknote.repositories.AttachmentSave
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Logger
import javax.inject.Inject

class EditorDrawModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    @Inject
    lateinit var datasource: StorageDatasource

    fun saveAsBitmap(bitmap: Bitmap) {
        val myFile: File = File(
            (context.filesDir
                .absolutePath + "/" + Uuid.generateType1UUID()) + ".jpg"
        )
        val created = myFile.createNewFile()
        val fos = FileOutputStream(myFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.flush()
        fos.close()
        var file_Path : String = datasource.compressAndSaveFile(myFile)
        if (file_Path == null) {
            val logger = Logger.getLogger("MyLogger")
            logger.info("Error")
        } else {
            val logger = Logger.getLogger("MyLogger")
            logger.info(file_Path)
        }
    }

}