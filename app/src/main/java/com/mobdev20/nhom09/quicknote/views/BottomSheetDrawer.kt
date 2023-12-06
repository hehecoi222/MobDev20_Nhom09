package com.mobdev20.nhom09.quicknote.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.state.Attachment
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.views.sheetsContent.CustomDatePicker
import com.mobdev20.nhom09.quicknote.views.sheetsContent.CustomTimePicker
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDrawer(
    isKeyboardActive: MutableState<Boolean>,
    kindOfBottomSheet: MutableState<KindOfBottomSheet>,
    expanded: MutableState<Boolean>,
    noteList: SnapshotStateList<NoteOverview> = mutableStateListOf(),
    onClickNote: (String) -> Unit = {},
    onDeleteNote: () -> Unit = {},
    onExpandNote: () -> Unit = {},
    onClickAttachment: () -> Unit = {},
    attachmentList: SnapshotStateList<Attachment> = mutableStateListOf(),
    onDeleteAttachment: (Attachment) -> Unit = {},
    onClickBackup: () -> Unit = {},
    onClickSync: () -> Unit = {},
    onClickBold: () -> Unit = {},
    onClickItalic: () -> Unit = {},
    onClickUnderline: () -> Unit = {},
    onClickOpen: (Attachment) -> Unit,
    onSetDate: (Instant) -> Unit = { _ -> },
    onSetTime: (Int, Int) -> Unit = { _, _ -> },
    onSetRemove: () -> Unit = {},
    onSetAdd: () -> Unit = {},
    onClickShare: () -> Unit = {},
    time: Instant = Instant.now(),
    isNotiOn: Boolean = false,
) {
    val isTimeShow = remember {
        mutableStateOf(false)
    }
    val isDateShow = remember {
        mutableStateOf(false)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = if (expanded.value) Color.Black.copy(alpha = 0.3f) else Color.Transparent
            )

    ) {
        Column(verticalArrangement = Arrangement.SpaceBetween) {
            Spacer(modifier = if (expanded.value) Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable(enabled = expanded.value) {
                    expanded.value = false
                    kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                } else Modifier
                .fillMaxWidth()
                .weight(1f))
            Box {
                BottomSheet(
                    isKeyboardActive = isKeyboardActive.value,
                    kindOfBottomSheet = kindOfBottomSheet,
                    expanded = expanded,
                    onExpandNote = onExpandNote
                ) {
                    KindOfBottomSheet.GetContent(
                        kindOfBottomSheet = kindOfBottomSheet.value,
                        oldNoteListState = noteList,
                        onClickNote = onClickNote,
                        onClickDelete = onDeleteNote,
                        onClickAttachment = {
                            kindOfBottomSheet.value = KindOfBottomSheet.AttachmentTab
                        },
                        attachmentList = attachmentList,
                        onAddAttachment = onClickAttachment,
                        onDeleteAttachment = onDeleteAttachment,
                        onclickBackup = onClickBackup,
                        onClickSync = onClickSync,
                        onClickOpen = onClickOpen,
                        time = time,
                        onSetRemove = onSetRemove,
                        onClickNoti = {
                            kindOfBottomSheet.value = KindOfBottomSheet.NotiTab
                        },
                        isNotiOn = isNotiOn,
                        isShowTime = isTimeShow,
                        isShowDate = isDateShow,
                        onClickShare = onClickShare,
                        onSetAdd = onSetAdd,
                    )
                }
                FormatBar(
                    kindOfBottomSheet = kindOfBottomSheet,
                    onClickUnderline = onClickUnderline,
                    onClickItalic = onClickItalic,
                    onClickBold = onClickBold
                )
            }
        }
        if (isTimeShow.value) {
            CustomTimePicker(
                onSetTime = onSetTime,
                isShowTime = isTimeShow,
                hourOfDay = time.atZone(ZoneId.systemDefault()).hour,
                minute = time.atZone(ZoneId.systemDefault()).minute
            )
        } else if (isDateShow.value) {
            CustomDatePicker(
                onSetDate = onSetDate,
                isShowDate = isDateShow,
                time = time
            )
        }
    }
}

@Composable
fun FormatBar(
    modifier: Modifier = Modifier, kindOfBottomSheet: MutableState<KindOfBottomSheet>,
    onClickBold: () -> Unit, onClickItalic: () -> Unit, onClickUnderline: () -> Unit
) {
    AnimatedVisibility(
        visible = kindOfBottomSheet.value == KindOfBottomSheet.FormatBar,
        enter = fadeIn() + slideInVertically(initialOffsetY = { full -> full }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClickBold, modifier = Modifier.padding(start = 12.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_format_bold_24),
                        contentDescription = null, // TODO: Add string description
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClickItalic) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_format_italic_24),
                        contentDescription = null, // TODO: Add string description,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onClickUnderline) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_format_underlined_24),
                        contentDescription = null, // TODO: Add string description,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
//                IconButton(onClick = { }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.baseline_format_color_fill_24),
//                        contentDescription = null, // TODO: Add string description,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//                Divider(
//                    modifier = Modifier
//                        .padding(horizontal = 8.dp)
//                        .fillMaxHeight(.5f)
//                        .width(2.dp),
//                    color = MaterialTheme.colorScheme.outline
//                )
//                IconButton(onClick = { }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.outline_check_box_24),
//                        contentDescription = null, // TODO: Add string description,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
            }
        }
    }
}

@Composable
fun BottomSheet(
    modifier: Modifier = Modifier,
    isKeyboardActive: Boolean = false,
    kindOfBottomSheet: MutableState<KindOfBottomSheet>,
    expanded: MutableState<Boolean>,
    onExpandNote: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val expandSize = when (kindOfBottomSheet.value) {
        KindOfBottomSheet.OldNotes -> {
            expanded.value = false
            screenHeight - 16.dp
        }

        KindOfBottomSheet.MoreOpts -> {
            expanded.value = true
            screenHeight / 5
        }

        KindOfBottomSheet.AttachmentTab -> {
            expanded.value = true
            screenHeight / 2
        }

        KindOfBottomSheet.NotiTab -> {
            expanded.value = true
            screenHeight / 4
        }

        else -> {
            0.dp
        }
    }
    AnimatedVisibility(
        visible = !isKeyboardActive,
        enter = fadeIn() + slideInVertically(initialOffsetY = { full -> full }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
    ) {
        Surface(
            modifier = modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp, topEnd = 20.dp
                    )
                )
                .animateContentSize()
                .size(
                    screenWidth, if (expanded.value) expandSize else 48.dp
                )
                .offset(), color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(modifier = Modifier
                    .clickable(
                        indication = null, interactionSource = interactionSource
                    ) {
                        kindOfBottomSheet.value = KindOfBottomSheet.OldNotes
                        onExpandNote()
                        expanded.value = !expanded.value
                    }
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(
                        vertical = dimensionResource(
                            id = R.dimen.padding_around
                        )
                    ), horizontalArrangement = Arrangement.Center) {
                    Surface(
                        modifier = Modifier
                            .size(
                                width = dimensionResource(id = R.dimen.trailing_icon_size),
                                height = dimensionResource(
                                    id = R.dimen.spacing
                                )
                            )
                            .clip(RoundedCornerShape(dimensionResource(id = R.dimen.spacing))),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
                AnimatedVisibility(
                    visible = expanded.value,
                    modifier = Modifier.fillMaxSize(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { full -> full }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { fullHeight -> fullHeight })
                ) {
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
fun BottomSheetDrawerPreview() {
//    BottomSheetDrawer(
//        expanded = mutableStateOf(false),
//        isKeyboardActive = mutableStateOf(false),
//        kindOfBottomSheet = mutableStateOf(KindOfBottomSheet.FormatBar),
//        settings =
//    )
}
