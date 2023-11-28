package com.mobdev20.nhom09.quicknote.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.mobdev20.nhom09.quicknote.views.sheetsContent.MoreOptionSheet
import com.mobdev20.nhom09.quicknote.views.sheetsContent.OldNotesSheet

enum class KindOfBottomSheet(val value: Int) {
    OldNotes(0),
    MoreOpts(1),
    FormatBar(2),
    AttachmentTab(3),
    NotiTab(4);

    companion object {
        @Composable
        fun GetContent(
            kindOfBottomSheet: KindOfBottomSheet,
            modifier: Modifier = Modifier,
            oldNoteListState: SnapshotStateList<Pair<String, String>> = mutableStateListOf(
                Pair("Hello", "Hi"),
                Pair("What", "A long context with wtf is going on just for sure, ...."),
                Pair("Another test suit that make it usable", "For real what is going on, Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum"),
            )
        ) {
            when (kindOfBottomSheet) {
                OldNotes -> OldNotesSheet(modifier = modifier, oldNoteListState = oldNoteListState)
                MoreOpts -> MoreOptionSheet()
                else -> {}
            }
        }
    }
}