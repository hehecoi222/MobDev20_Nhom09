package com.mobdev20.nhom09.quicknote.views

import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.ui.theme.NavigationBarColor
import com.mobdev20.nhom09.quicknote.ui.theme.StatusBarColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier,
    onClickMore: () -> Unit = {},
    onClickFormat: () -> Unit = {},
    onClickAccount: () -> Unit = {},
    onClickUndo: () -> Unit = {},
    onClickRedo: () -> Unit = {},
    redoEnable: MutableState<Boolean> = mutableStateOf(true),
    offset: MutableState<Boolean> = mutableStateOf(false)
) {
    if (offset.value) {
        StatusBarColor(
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            isSystemInDarkTheme()
        )
    } else {
        StatusBarColor(color = MaterialTheme.colorScheme.surface, isSystemInDarkTheme())
    }
    NavigationBarColor(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        isSystemInDarkTheme()
    )

    Surface(
        modifier = modifier,
        color = if (offset.value) MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp) else MaterialTheme.colorScheme.surface
    ) {
        TopAppBar(modifier = modifier, title = {
            Row {
                IconButton(onClick = onClickUndo) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_undo_24),
                        contentDescription = null // TODO: Add string description
                    )
                }
                IconButton(onClick = onClickRedo, enabled = redoEnable.value) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_redo_24),
                        contentDescription = null // TODO: Add string description
                    )
                }
            }
        }, actions = {
            IconButton(onClick = onClickMore) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_more_horiz_24),
                    contentDescription = null // TODO: Add string description
                )
            }
            IconButton(onClick = onClickFormat) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_text_format_24),
                    contentDescription = null, // TODO: Add string description,
                )
            }
            IconButton(onClick = onClickAccount) {
                Icon(
                    imageVector = Icons.Default.AccountCircle, // TODO: Replace user image if sign in
                    contentDescription = null // TODO: Add string description
                )
            }
        }, colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
        )
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun CustomTopAppBarPreview() {
    CustomTopAppBar(offset = mutableStateOf(true),)
}