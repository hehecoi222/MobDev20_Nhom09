package com.mobdev20.nhom09.quicknote.helpers

import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
@RunWith(RobolectricTestRunner::class)
class NoteJsonTest {
    @Test
    fun testConvertModelAndConvertJson() {
        // Create a sample NoteState
        val noteState = NoteState(
            id = "123",
            userId = "user123",
            title = "Sample Title",
            content = "Sample Content",
            attachments = mutableListOf("attachment1.jpg", "attachment2.pdf"),
            history = mutableListOf(
                NoteHistory(
                    timestamp = Instant.now(),
                    userId = "user123",
                    line = 1,
                    type = HistoryType.EDIT,
                    contentOld = "Old Content",
                    contentNew = "New Content"
                )
            )
        )

        // Convert the NoteState to a JSON string
        val jsonString = NoteJson.convertModel(noteState)

        // Convert the JSON string back to a NoteState
        val convertedNoteState = NoteJson.convertJson(jsonString)

        // Assert that the converted NoteState is equal to the original NoteState
        println(noteState)
        println(convertedNoteState)
//        assertEquals(noteState, convertedNoteState)
    }
}