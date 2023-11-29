package com.mobdev20.nhom09.quicknote.views

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.mobdev20.nhom09.quicknote.ui.theme.NavigationBarColor
import com.mobdev20.nhom09.quicknote.ui.theme.StatusBarColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopSigninBar(modifier: Modifier = Modifier, onBackClick: () -> Unit = {}) {
    NavigationBarColor(
        color = MaterialTheme.colorScheme.surface,
        isSystemInDarkTheme()
    )
    StatusBarColor(color = MaterialTheme.colorScheme.surface, isSystemInDarkTheme())
    TopAppBar(modifier = modifier, title = {

    }, navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null, // TODO: Add string description
                tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
    }, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent
    ))
}

@Preview
@Composable
fun TopSigninBarPreview() {
    TopSigninBar()
}