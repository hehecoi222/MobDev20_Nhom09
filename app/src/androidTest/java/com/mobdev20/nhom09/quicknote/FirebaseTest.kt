package com.mobdev20.nhom09.quicknote

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.mobdev20.nhom09.quicknote.datasources.DataSources
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import kotlinx.coroutines.DelicateCoroutinesApi

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 *
 * Run all test (from class) to make sure that all the permission rule is granted
 *
 * Delete image (attachments) in test device storage before run download test to avoid
 * the error about permission or something. This error, however, does not effect
 * the real app logically.
 *
 * Comment to run only upload/download test at once to avoid the same error as above
 */
@RunWith(AndroidJUnit4::class)
class FirebaseTest {


    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        //android.Manifest.permission.INTERNET,
        //android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
    )

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.mobdev20.nhom09.quicknote", appContext.packageName)
    }

    @Test
    fun permission() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val hasReadPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasWritePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val hasMediaPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED

        assert(hasReadPermission)
        assert(hasWritePermission)
        assert(hasMediaPermission)
    }

    @Test
    fun oldUploadTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)

        val model = """
            {
                "id": "-3",
                "userId": "-65565",
                "title": "Upload Test Title",
                "content": "Upload Test Content",
                "attachmentPaths": 	["storage/emulated/0/Documents/data4Test/downloadData/Try.jpg", "storage/emulated/0/Documents/data4Test/downloadData/Logo.png"]
            }
        """.trimIndent()
        //Logcat tag: tag:UploadRecordComplete tag:UploadComplete
        DataSources().upload(model)
        Thread.sleep(15000)
    }


    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun oldTestDownload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)

        GlobalScope.launch {
            val testResult = DataSources().download("-3")
            Log.i("TestLog", "Result: $testResult")
            //Logcat tag: tag:TestLog tag:DownloadRecordSuccess tag:DownloadSuccess
        }
        Thread.sleep(15000) // Adjust this delay based on your network speed
    }
}