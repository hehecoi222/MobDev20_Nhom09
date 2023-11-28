package com.mobdev20.nhom09.quicknote.datasources

import android.R.attr.data
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject


interface StorageDatasource {

    fun getIntentForAttachment() : Intent?
    fun addIntentToList(context: Context, list: MutableList<Intent>, intent: Intent) : List<Intent>
    fun handleActivityResult(requestCode: Int, resultCode: Int, data : Intent?) : String?
    fun compressAndSaveFile(fileUri : Uri) : String
    fun getTempFile(context: Context) : File
    fun getImageFromResult(
        context: Context?, resultCode: Int,
        imageReturnedIntent: Intent?
    ): Bitmap?
}

class StorageDatasourceImpl @Inject constructor(@ApplicationContext private val context: Context) :
        StorageDatasource {

        private val TAG : String = "Note"
        private val TEMP_IMAGE_NAME = "tempImage"
        // Add all intent for users + intent for getimage
        override fun getIntentForAttachment(): Intent? {
//      chooserIntent co kieu du lieu la Intent / ? means co the la null
            var chooserIntent: Intent? = null;
            val intentList = ArrayList<Intent>();

//      Intent chon anh tu thu vien (Gallery)
            val pickIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//      Intent chon anh tu camera
            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//      Tra ve du lieu truc tiep , khong luu vao file
            takePhotoIntent.putExtra("return-data", true);
//      Xac dinh vi tri de luu giu anh chup
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)))

            intentList.addAll(addIntentToList(context, ArrayList(), pickIntent))
            intentList.addAll(addIntentToList(context, ArrayList(), takePhotoIntent))

            if (intentList.size > 0) {
                chooserIntent = Intent.createChooser(
                    intentList.removeAt(intentList.size - 1),
                    "Select Image"
                )
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toTypedArray())
            }
            return chooserIntent;
        }

        override fun addIntentToList(
            context: Context,
            list: MutableList<Intent>,
            intent: Intent
        ): List<Intent> {
//      Lay thong tin ve cac thanh phan he thong co the xu ly intent nay
            val resInfo: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, 0)

            for (resolveInfo in resInfo) {
//            Lay ten goi cua thanh phan xu ly intent
                val packageName: String = resolveInfo.activityInfo.packageName
//          Tao mot intent moi duoc sao chep tu intent co so
                val targetedIntent = Intent(intent)
//            Thiet lap package cua targetedIntent de chi gui den thanh phan xu ly cu the
                targetedIntent.`package` = packageName
                list.add(targetedIntent);
                Log.d(TAG, "Intent : ${intent.action} package : $packageName")
            }

            return list;
        }

        override fun getTempFile(context: Context): File {
            val imageFile = File(context.externalCacheDir, TEMP_IMAGE_NAME)
            imageFile.parentFile?.mkdirs()
            return imageFile
        }

        override fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): String? {
        TODO("Not yet implemented")
        }

        override fun compressAndSaveFile(fileUri: Uri): String {
        TODO("Not yet implemented")
        }

    override fun getImageFromResult(
        context: Context?, resultCode: Int,
        imageReturnedIntent: Intent?
    ): Bitmap? {
        Log.d(TAG, "getImageFromResult, resultCode: $resultCode")
        var bm: Bitmap? = null
        val imageFile = getTempFile(context!!)
        if (resultCode == Activity.RESULT_OK) {
            val selectedImage: Uri?
            val isCamera = imageReturnedIntent == null || imageReturnedIntent.data == null ||
                    imageReturnedIntent.data.toString().contains(imageFile.toString())
            selectedImage = if (isCamera) {
                /** CAMERA  */
                Uri.fromFile(imageFile)
            } else {
                /** ALBUM  */
                imageReturnedIntent!!.data
            }
            Log.d(TAG, "selectedImage: $selectedImage")
//            bm = getImageResized(context, selectedImage)
//            val rotation = getRotation(context, selectedImage, isCamera).toInt()
//            bm = rotate(bm, rotation)
        }
        return bm
    }

    fun <I, O> prepareCall(
        activityResultContract: ActivityResultContract<I, O>,
        activityResultCallback: ActivityResultCallback<O>
    ): ActivityResultLauncher<I> {

        return prepareCall(object : ActivityResultContract<I, O>() {
            override fun createIntent(context: Context, input: I): Intent {
                return activityResultContract.createIntent(context, input)
            }

            override fun parseResult(resultCode: Int, intent: Intent?): O =
                activityResultContract.parseResult(resultCode, intent)
        }, activityResultCallback)
    }

}


class ChooseAttachment @Inject constructor(private val context: Context) :
    ActivityResultContract<Unit, Bitmap?>() {

    @Inject
    lateinit var storageDatasource : StorageDatasource

    override fun createIntent(context: Context, input: Unit): Intent {
        if (storageDatasource.getIntentForAttachment() != null) {
            return storageDatasource.getIntentForAttachment()!!
        } else {
            return Intent()
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Bitmap? {
        if (resultCode == Activity.RESULT_OK) {
            return storageDatasource.getImageFromResult(context, resultCode, intent)
        } else {
            return null
        }
    }
}