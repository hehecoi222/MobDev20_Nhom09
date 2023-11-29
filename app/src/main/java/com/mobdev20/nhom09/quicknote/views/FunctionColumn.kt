package com.mobdev20.nhom09.quicknote.views

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobdev20.nhom09.quicknote.R

@Composable
fun ButtonColumn(
    modifier: Modifier = Modifier,
    onClickSignOut: () -> Unit = {},
    onClickkRecover: () -> Unit = {}
) {
    Column(modifier = modifier) {
        ButtonItem(
            onClick = onClickSignOut,
            icon = R.drawable.outline_exit_to_app_24,
            value = R.string.signout_btn,
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        )
        Divider(modifier = Modifier.height(2.dp))
        ButtonItem(
            onClick = onClickkRecover,
            icon = R.drawable.outline_cloud_sync_24,
            value = R.string.sync_btn,
            modifier = Modifier.padding(20.dp).fillMaxWidth())
    }
}

@Composable
@Preview
fun ButtonColumnPreview() {
    ButtonColumn()
}

@Composable
fun ButtonItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    @StringRes value: Int
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = stringResource(id = value),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(id = value),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
@Preview
fun ButtonItemPreview() {
    ButtonItem(onClick = {}, icon = R.drawable.outline_exit_to_app_24, value = R.string.signout_btn)
}