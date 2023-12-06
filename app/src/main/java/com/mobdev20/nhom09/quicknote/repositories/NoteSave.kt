package com.mobdev20.nhom09.quicknote.repositories

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface NoteSave {
    fun loadNote(id: String): Flow<NoteState?>
    fun loadListNote(vararg criteria: String): Flow<List<NoteOverview?>>
    suspend fun update(noteState: NoteState)

    suspend fun delete(id: String)
    suspend fun updateFromModel(id: String, model: String)
}