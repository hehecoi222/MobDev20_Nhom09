package com.mobdev20.nhom09.quicknote.repositories.implementations

import com.mobdev20.nhom09.quicknote.datasources.StorageDatasource
import com.mobdev20.nhom09.quicknote.repositories.AttachmentSave
import java.io.File
import javax.inject.Inject

class AttachmentSaveImpl @Inject constructor() : AttachmentSave {

    @Inject
    lateinit var storageDataSource : StorageDatasource


    override fun createFile(file: File): String {
        return storageDataSource.compressAndSaveFile(file);
    }

}