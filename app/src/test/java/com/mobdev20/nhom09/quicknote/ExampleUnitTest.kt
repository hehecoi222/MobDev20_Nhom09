package com.mobdev20.nhom09.quicknote

import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import com.mobdev20.nhom09.quicknote.state.NoteState
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testNotetoJson() {
        println(NoteJson.convertModel(NoteState()))
    }
}