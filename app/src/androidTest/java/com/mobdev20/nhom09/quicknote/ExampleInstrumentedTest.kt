package com.mobdev20.nhom09.quicknote

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert.*


import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdev20.nhom09.quicknote.datasources.DataSources
import com.mobdev20.nhom09.quicknote.helpers.Encoder
import kotlinx.coroutines.delay


import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_MEDIA_IMAGES,
        //android.Manifest.permission.INTERNET,
        //android.Manifest.permission_group.STORAGE,
        //android.Manifest.permission_group.READ_MEDIA_VISUAL,
        //android.Manifest.permission_group.READ_MEDIA_AURAL,
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

        assert(hasReadPermission)
        assert(hasWritePermission)
    }

    /*@Test
    fun uploadTest() {
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



        DataSources().upload(model)
        Thread.sleep(50000)
    }*/


    @Test
    fun testDownload() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)

        DataSources().download("-3")
        Thread.sleep(50000) // Adjust this delay based on your network speed
    }


}