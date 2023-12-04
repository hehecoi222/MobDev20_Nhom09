package com.mobdev20.nhom09.quicknote.repositories

interface BackupNote {
    suspend fun backup(id: String)
    suspend fun restore(id: String)
}