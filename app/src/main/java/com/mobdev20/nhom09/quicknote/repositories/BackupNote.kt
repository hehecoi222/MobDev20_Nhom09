package com.mobdev20.nhom09.quicknote.repositories

import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.coroutines.flow.Flow

interface BackupNote {
    suspend fun backup(id: String)
    suspend fun restore(id: String, noteFirebase: NoteState?): Flow<Pair<Int, List<NoteHistory>>?>
}