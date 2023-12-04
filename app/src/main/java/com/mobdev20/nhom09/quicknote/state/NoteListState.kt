package com.mobdev20.nhom09.quicknote.state
data class NoteOverview(val id: String = "", val title: String = "", val content: String = "") {
    val combinedContent: String = "$title:$content"
}