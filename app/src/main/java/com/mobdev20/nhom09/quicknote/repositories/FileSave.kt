package com.mobdev20.nhom09.quicknote.repositories

import java.io.File

interface FileSave {
    suspend fun saveFile(file: File)
}