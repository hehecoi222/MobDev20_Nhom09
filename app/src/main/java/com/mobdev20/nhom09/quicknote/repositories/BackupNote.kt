package com.mobdev20.nhom09.quicknote.repositories

import com.mobdev20.nhom09.quicknote.state.NoteState

interface BackupNote {
    suspend fun backup(id: String)
    suspend fun restore(id: String): NoteState?
}