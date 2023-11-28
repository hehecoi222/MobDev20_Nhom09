package com.mobdev20.nhom09.quicknote.views.sheetsContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun OldNotesSheet(modifier: Modifier = Modifier, oldNoteListState: SnapshotStateList<Pair<String, String>>) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Old Notes",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(oldNoteListState) {
                OldNotesCard(modifier = Modifier.padding(bottom = 8.dp), oldNoteItem = it)
            }
        }
    }
}

@Composable
private fun OldNotesCard(modifier: Modifier = Modifier, oldNoteItem: Pair<String, String>?) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = oldNoteItem?.first.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.W600,
                overflow = TextOverflow.Ellipsis,
                maxLines = 3,
            )
            Text(
                text = oldNoteItem?.second.toString(),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 12,
                overflow = TextOverflow.Clip
            )
        }
    }
}