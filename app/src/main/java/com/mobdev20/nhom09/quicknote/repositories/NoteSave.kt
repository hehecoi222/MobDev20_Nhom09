package com.mobdev20.nhom09.quicknote.repositories

import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface NoteSave {
    fun loadNote(id: String): Flow<NoteState?>
    fun loadListNote(vararg criteria: String): StateFlow<List<NoteState>>
    suspend fun update(noteState: NoteState)

    // nhan file tu ViewModel result -> tra path
//    fun createFile(file : File) : String
}