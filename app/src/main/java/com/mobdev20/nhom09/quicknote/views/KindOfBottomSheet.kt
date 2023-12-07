package com.mobdev20.nhom09.quicknote.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.mobdev20.nhom09.quicknote.state.Attachment
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.views.sheetsContent.Attachments
import com.mobdev20.nhom09.quicknote.views.sheetsContent.MoreOptionSheet
import com.mobdev20.nhom09.quicknote.views.sheetsContent.Notifications
import com.mobdev20.nhom09.quicknote.views.sheetsContent.OldNotesSheet
import java.time.Instant
import java.time.ZoneId

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
            onClickOpen: (Attachment) -> Unit,
            onClickNoti: () -> Unit,
            onSetRemove: () -> Unit,
            onSetAdd: () -> Unit,
            onClickShare: () -> Unit,
            time: Instant,
            isNotiOn:Boolean,
            isShowTime: MutableState<Boolean>,
            isShowDate: MutableState<Boolean>,
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
                    onClickSync = onClickSync,
                    onClickNoti = onClickNoti,
                    onClickShare = onClickShare,
                    isNotiOn = isNotiOn
                )

                AttachmentTab -> Attachments(
                    attachments = attachmentList,
                    onClickRemove = onDeleteAttachment,
                    onClickAdd = onAddAttachment,
                    onClickOpen = onClickOpen,
                )

                NotiTab -> Notifications(
                    hourOfDay = time.atZone(ZoneId.systemDefault()).hour,
                    minute = time.atZone(ZoneId.systemDefault()).minute,
                    year = time.atZone(ZoneId.systemDefault()).year,
                    month = time.atZone(ZoneId.systemDefault()).monthValue,
                    dayOfMonth = time.atZone(ZoneId.systemDefault()).dayOfMonth,
                    onClickRemove = onSetRemove,
                    isShowTime = isShowTime,
                    isShowDate = isShowDate,
                    onClickAdd = onSetAdd,
                    isExist = isNotiOn
                )

                else -> {}
            }
        }
    }
}