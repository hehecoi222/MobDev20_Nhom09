package com.mobdev20.nhom09.quicknote.state

import android.graphics.Bitmap
import com.mobdev20.nhom09.quicknote.helpers.NoteJson
import java.time.Instant

data class NoteState(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val timeUpdate: Instant = Instant.now(),
    val timeRestore: Instant = Instant.now(),
    val history: MutableList<NoteHistory> = mutableListOf(),
    val attachmentCount: Int = 0,
    val attachments: MutableList<String> = mutableListOf(),
    val notificationTime: Instant = Instant.now(),
    val notificationId: String = "",
) {
}

data class NoteHistory(
    val timestamp: Instant = Instant.now(),
    val userId: String,
    val line: Int = 0,
    val type: HistoryType = HistoryType.ADD,
    val contentOld: String = "",
    val contentNew: String = ""
)

data class Attachment(
    val filepath: String = "",
    val thumbnail: Bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
) {
    val filename = NoteJson.getFilenameFromAttachPath(filepath)
}

enum class HistoryType(val value: Int) {
    ADD(0),
    EDIT(1),
    DELETE(2);
}