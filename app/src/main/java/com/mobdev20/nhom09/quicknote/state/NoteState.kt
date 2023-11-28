package com.mobdev20.nhom09.quicknote.state

import java.time.Instant

data class NoteState(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timeUpdate: Instant = Instant.now(),
    val timeRestore: Instant = Instant.now(),
    val history: MutableList<NoteHistory> = mutableListOf(),
) {
}

data class NoteHistory(
    val timestamp: Instant = Instant.now(),
    val line: Int = 0,
    val type: HistoryType = HistoryType.ADD,
    val contentOld: String = "",
    val contentNew: String = ""
)

enum class HistoryType(val value: Int) {
    ADD(0),
    EDIT(1),
    DELETE(2);
}