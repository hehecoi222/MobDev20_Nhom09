package com.mobdev20.nhom09.quicknote

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.mockito.Mockito.*

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.mobdev20.nhom09.quicknote.datasources.DataSources
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
        //android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
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

    @Test
    fun uploadTest() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)

        DataSources().upload("/storage/emulated/0/Documents/datafortest/dataTest.json")
    }


    /*@Test
    fun testDownload() = runBlockingTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        FirebaseApp.initializeApp(context)

        DataSources().download("-1")
        Thread.sleep(50000) // Adjust this delay based on your network speed
    }*/


}