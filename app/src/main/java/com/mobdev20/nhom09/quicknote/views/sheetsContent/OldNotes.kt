package com.mobdev20.nhom09.quicknote.views.sheetsContent

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.state.NoteOverview

@Composable
fun OldNotesSheet(
    modifier: Modifier = Modifier,
    oldNoteListState: SnapshotStateList<NoteOverview>,
    onClickNote: (String) -> Unit,
) {
    val isList = remember {
        mutableStateOf(false)
    }
    val selected = remember {
        mutableStateListOf<NoteOverview>()
    }
    val isOnSelected = remember {
        mutableStateOf(false)
    }
    Column(modifier = modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (selected.isEmpty()) "Old Notes" else "Selected ${selected.size} Notes",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = {
                isList.value = !isList.value
            }) {
                Icon(
                    painter = painterResource(id = if (isList.value) R.drawable.outline_view_list_24 else R.drawable.baseline_grid_view_24),
                    contentDescription = null, // TODO: Add string description
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        AnimatedVisibility(visible = isList.value) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(oldNoteListState) {
                    OldNotesCard(
                        modifier = Modifier.fillMaxWidth(),
                        oldNoteItem = it,
                        onClickNote = {
                            if (isOnSelected.value)
                                if (selected.contains(it)) {
                                    selected.remove(it)
                                    if (selected.isEmpty())
                                        isOnSelected.value = false
                                } else selected.add(it)
                            else {
                                onClickNote(it.id)
                            }
                        },
                        selected = selected.toList(),
                        onLongClick = {
                            if (!isOnSelected.value) {
                                isOnSelected.value = true
                                selected.clear()
                                selected.add(it)
                            } else {
                                if (selected.contains(it)) {
                                    selected.remove(it)
                                    if (selected.isEmpty())
                                        isOnSelected.value = false
                                } else selected.add(it)
                            }

                        }
                    )
                }
            }
        }
        AnimatedVisibility(visible = !isList.value) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(oldNoteListState) {
                    OldNotesCard(
                        modifier = Modifier.padding(bottom = 8.dp),
                        oldNoteItem = it,
                        onClickNote = {
                            if (isOnSelected.value)
                                if (selected.contains(it)) {
                                    selected.remove(it)
                                    if (selected.isEmpty())
                                        isOnSelected.value = false
                                }
                                else selected.add(it)
                            else {
                                onClickNote(it.id)
                            }
                        },
                        selected = selected.toList(),
                        onLongClick = {
                            if (!isOnSelected.value) {
                                isOnSelected.value = true
                                selected.clear()
                                selected.add(it)
                            } else {
                                if (selected.contains(it)) {
                                    selected.remove(it)
                                    if (selected.isEmpty())
                                        isOnSelected.value = false
                                }
                                else selected.add(it)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun OldNotesCard(
    modifier: Modifier = Modifier,
    oldNoteItem: NoteOverview,
    onClickNote: (NoteOverview) -> Unit,
    onLongClick: (NoteOverview) -> Unit,
    selected: List<NoteOverview>
) {
    OutlinedCard(
        modifier = modifier.combinedClickable(onLongClick = {
            onLongClick(oldNoteItem)
        }) {
            onClickNote(oldNoteItem)
        },
        border = if (selected.contains(oldNoteItem)) BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.primary
        ) else CardDefaults.outlinedCardBorder(),
        colors = if (selected.contains(oldNoteItem)) CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                2.dp
            )
        ) else CardDefaults.outlinedCardColors()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = oldNoteItem.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W600,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
            )
            Text(
                text = oldNoteItem.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 12,
                overflow = TextOverflow.Clip
            )
        }
    }
}