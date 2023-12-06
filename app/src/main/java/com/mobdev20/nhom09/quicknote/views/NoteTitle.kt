package com.mobdev20.nhom09.quicknote.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.state.NoteOverview

@Composable
fun NoteTitleTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    noteList: List<NoteOverview>,
    createNote: () -> Unit = {},
    onSelectNote: (String) -> Unit = {},
    clearState: () -> Unit = {},
    isClearAvailable: Boolean = false,
    moveFocus: () -> Unit = {}
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val expanded = remember {
        mutableStateOf(false)
    }
    Box {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = {
                    onValueChange(it)
                    expanded.value = it.length > 3
                },
                modifier = Modifier
                    .focusRequester(focusRequester = focusRequester)
                    .weight(.85f)
                    .height(IntrinsicSize.Min),
                textStyle = MaterialTheme.typography.displaySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Note title",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                    innerTextField()
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next, capitalization = KeyboardCapitalization.Sentences
                ),
                keyboardActions = KeyboardActions(onNext = {
                    moveFocus()
                }),
                cursorBrush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            )
            AnimatedVisibility(visible = isClearAvailable, modifier = Modifier.weight(.15f)) {
                IconButton(onClick = clearState, modifier = Modifier
                    .size(24.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_remove_circle_outline_24),
                        contentDescription = null, // TODO: Add string description
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = {
                expanded.value = false
            },
            modifier = Modifier.align(Alignment.BottomStart),
            properties = PopupProperties(focusable = false)
        ) {
            noteList.filter {
                val valueSplit = value.split(" ")
                var check = true
                valueSplit.forEach { searchTerm ->
                    if (searchTerm.isEmpty())
                    else if (searchTerm.isNotEmpty() && !it.combinedContent
                            .contains(searchTerm, true)
                    ) {
                        check = false
                        return@forEach
                    }
                }
                return@filter check
            }.forEach {
                DropdownMenuItem(text = {
                    Text(
                        text = it.title,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                }, onClick = {
                    onSelectNote(it.id)
                    expanded.value = false
                })
            }
            DropdownMenuItem(text = {
                Row {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.padding(4.dp))
                    Text(text = "Create new note")
                }
            }, onClick = {
                expanded.value = false
                createNote()
            })
        }
    }

    LaunchedEffect((Unit)) {
        focusRequester.requestFocus()
    }
}