package com.mobdev20.nhom09.quicknote.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.mobdev20.nhom09.quicknote.state.Attachment
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.views.sheetsContent.Attachments
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
            oldNoteListState: SnapshotStateList<NoteOverview> = mutableStateListOf(),
            onClickNote: (String) -> Unit,
            onClickDelete: () -> Unit,
            onClickAttachment: () -> Unit,
            attachmentList: SnapshotStateList<Attachment>,
            onDeleteAttachment: (Attachment) -> Unit,
            onAddAttachment: () -> Unit,
            onclickBackup: () -> Unit,
            onClickSync: () -> Unit,
            onClickNotification: () -> Unit = {}
        ) {
            when (kindOfBottomSheet) {
                OldNotes -> OldNotesSheet(
                    modifier = modifier,
                    oldNoteListState = oldNoteListState,
                    onClickNote = onClickNote,
                )

                MoreOpts -> MoreOptionSheet(
                    onClickDelete = onClickDelete,
                    onClickAttachments = onClickAttachment,
                    onClickBackup = onclickBackup,
                    onClickSync = onClickSync
                )

                AttachmentTab -> Attachments(
                    attachments = attachmentList,
                    onClickRemove = onDeleteAttachment,
                    onClickAdd = onAddAttachment
                )

                else -> {}
            }
        }
    }
}