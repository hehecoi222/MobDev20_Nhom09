package com.mobdev20.nhom09.quicknote.views.sheetsContent

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R

@Composable
fun MoreOptionSheet(modifier: Modifier = Modifier,
                    onClickDelete: () -> Unit,
                    onClickAttachments: () -> Unit,
                    onClickBackup: () -> Unit,
                    onClickSync: () -> Unit,
                    onClickNoti: () -> Unit,
                    onClickShare: () -> Unit,
                    isNotiOn: Boolean,
                    ) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceAround) {
        MoreOption(
            icon = R.drawable.outline_cloud_upload_24,
            value = R.string.more_opt_label_backup,
            visible = true,
            onClick = onClickBackup
        )
        MoreOption(
            icon = R.drawable.outline_delete_24,
            value = R.string.more_opt_label_delete,
            visible = true,
            onClick = onClickDelete
        )
        MoreOption(
            icon = R.drawable.outline_cloud_sync_24,
            value = R.string.sync_btn,
            visible = false,
            onClick = onClickSync
        )
        MoreOption(
            icon = R.drawable.outline_share_24,
            value = R.string.more_opt_label_share,
            visible = true,
            onClick = onClickShare
        )
        MoreOption(
            icon = R.drawable.outline_attach_file_24,
            value = R.string.more_opt_label_attach,
            visible = true,
            onClick = onClickAttachments
        )
        MoreOption(
            icon = if (isNotiOn) R.drawable.outline_notifications_active_24 else R.drawable.outline_notification_add_24,
            value = if (isNotiOn) R.string.more_opt_label_noti_edit else R.string.more_opt_label_noti_add,
            visible = true,
            onClick = onClickNoti
        )
    }
}

@Composable
private fun MoreOption(
    onClick: () -> Unit = {},
    @DrawableRes icon: Int,
    @StringRes value: Int,
    visible: Boolean = false
) {
    AnimatedVisibility(visible = visible) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(5.dp)
                .wrapContentHeight()
                .clickable(onClick = onClick)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = stringResource(id = value),
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = stringResource(id = value),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .width(68.dp)
                    .wrapContentHeight(),
                textAlign = TextAlign.Center
            )
        }
    }
}