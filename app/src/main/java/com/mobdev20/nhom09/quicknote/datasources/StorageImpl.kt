package com.mobdev20.nhom09.quicknote.datasources

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import com.mobdev20.nhom09.quicknote.DrawActivity
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.helpers.Uuid
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


interface StorageDatasource {
    fun compressAndSaveFile(file: File): File?
    fun getFileFromResult(
        context: Context?, resultCode: Int,
        imageReturnedIntent: Intent?
    ): File?

    fun getFileFromInternal(path: String): Flow<File?>

    suspend fun deloadFile(path: String)
    suspend fun deleteFile(path: String)
    fun saveFromBitmap(bitmap: Bitmap): File
}

class StorageDatasourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
    StorageDatasource {

    private val TAG: String = "Note"


    //      Nhan file -> nen file -> ghi vao internal -> tra path cho repository (String)
//      dst : String = dia chi luu file
//      Call this func : compressAsZip(myFile ,MainActivity.this.getFilesDir().getAbsolutePath() + "/compress.zip");
    override fun compressAndSaveFile(file: File): File? {
//  A file has name like this : 0000018c-0000-1000-0e1a-b609392c67ef
        val dst: String = file.absolutePath + ".zip"

        val dstFile = File(dst)
        //make dirs if necessary
        dstFile.parentFile?.mkdirs();
        try {
            val parameters = ZipParameters()
            parameters.isEncryptFiles = false
            parameters.isIncludeRootFolder = false
            parameters.compressionMethod = CompressionMethod.DEFLATE
            parameters.compressionLevel = CompressionLevel.ULTRA
            val zipFile = ZipFile(dstFile.absoluteFile)
            zipFile.isRunInThread = true
            zipFile.addFile(file, parameters)
            while (!zipFile.progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
            }
            return File(dst)
        } catch (exception: Exception) {
            Log.d("ZIP_FAIL", exception.stackTrace.toString())
            return null
        }
    }

    override fun getFileFromResult(
        context: Context?, resultCode: Int,
        imageReturnedIntent: Intent?
    ): File? {
        Log.d(TAG, "getImageFromResult, resultCode: $resultCode")
        if (resultCode == Activity.RESULT_OK) {
            val isFile = imageReturnedIntent?.extras?.get("data") == null
            val internal = imageReturnedIntent?.extras?.get("internal") == null
            try {
                val selectedFile =
                    if (isFile) saveFromURI(imageReturnedIntent!!.data!!)
                        else if (internal) saveFromBitmap(
                        imageReturnedIntent!!.extras!!.get("data") as Bitmap
                    ) else {
                        val byteArray = imageReturnedIntent!!.extras!!.get("data") as ByteArray
                        saveFromBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size))
                    }
                return selectedFile
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    override fun saveFromBitmap(bitmap: Bitmap): File {
        val myFile = File(
            (context.filesDir.absolutePath + "/image." + Uuid.generateType1UUID()) + ".png"
        )
        val fos = FileOutputStream(myFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos)
        fos.flush()
        fos.close()
        val file = compressAndSaveFile(myFile)
        myFile.delete()
        return file!!
    }

    fun saveFromURI(uri: Uri): File? {
        val filename = NoteJson.getFileNewName(getFileName(uri), Uuid.generateType1UUID())
        (context as Context).contentResolver.openInputStream(uri)?.use {
            val myFile = File(
                ((context as Context).filesDir.absolutePath + "/" + filename
                        )
            )
            val fos = FileOutputStream(myFile)
            it.copyTo(fos)
            fos.flush()
            fos.close()
            val file = compressAndSaveFile(myFile)
            myFile.delete()
            return file!!
        }
        return null
    }

    //  call : File newFile = getFileFromInternal(filePath);
//    filePath  = from compressandsavefile-> String
    override fun getFileFromInternal(path: String): Flow<File?> = flow {

//    Get file name from zip file path
        var fileName_zip = path.substring(path.lastIndexOf("/") + 1).split(".")
        fileName_zip = fileName_zip.subList(0, fileName_zip.size - 1)
        val fileName = fileName_zip.joinToString(".")

// Put it inside /unzippedFile folder
        val fileStorage =
            File(context.filesDir.absolutePath)
        if (!fileStorage.exists()) {
            fileStorage.mkdir()
        }
        try {
        ZipFile(path).extractAll(fileStorage.absolutePath + "/")
        val filesList = fileStorage.listFiles()
        for (file in filesList) {
            val file_name = file.name;
            if (file_name.equals(fileName, ignoreCase = true)) {
                emit(file)
            }
        }} catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
            Log.d(TAG, e.stackTrace.toString())
        }
    }

    override suspend fun deloadFile(path: String) {
        val newPath = path.split(".")
        File(newPath.subList(0, newPath.size - 1).joinToString(".")).delete()
    }

    override suspend fun deleteFile(path: String) {
        deloadFile(path)
        File(path).delete()
    }

    fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

}


class ChooseAttachment @Inject constructor(
    private val context: Context,
    private val storageDatasource: StorageDatasource
) :
    ActivityResultContract<Unit, Any?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        val intentList = mutableListOf<Intent>()
        intentList.add(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        })
        intentList.add(
            ActivityResultContracts.PickVisualMedia()
                .createIntent(context, PickVisualMediaRequest.Builder().build())
        )
        intentList.add(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        intentList.add(Intent(context, DrawActivity::class.java))
        val chooserIntent =
            Intent.createChooser(intentList.removeAt(intentList.size - 1), "Get Attachments")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
        return chooserIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Any? {
        if (resultCode == Activity.RESULT_OK) {
            return storageDatasource.getFileFromResult(context, resultCode, intent)
        } else {
            return null
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageDatasourceModule {
    @Binds
    abstract fun bindStorageDatasource(storageDatasourceImpl: StorageDatasourceImpl): StorageDatasource
}