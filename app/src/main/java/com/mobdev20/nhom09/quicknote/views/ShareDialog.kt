package com.mobdev20.nhom09.quicknote.views

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.state.UserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDialog(
    modifier: Modifier = Modifier,
    owner: UserState,
    usersRead: SnapshotStateList<UserState>,
    usersEdit: SnapshotStateList<UserState>,
    onClickEdit: (UserState) -> Unit,
    onClickRemove: (UserState) -> Unit,
    onCopyLink: () -> Unit,
    onCloseButton: () -> Unit,
) {
    val value = remember {
        mutableStateOf("")
    }
    AlertDialog(onDismissRequest = {

    }, confirmButton = {
        TextButton(onClick = onCopyLink) {
            Text(text = "Copy link")
        }
        TextButton(onClick = onCloseButton) {
            Text(text = "Close")
        }
    }, modifier = modifier, dismissButton = {

    }, title = {
        Text(
            text = "Share",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }, icon = {
        Icon(
            painter = painterResource(id = R.drawable.outline_share_24),
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.secondary
        )
    }, text = {
        Column {
            Text(text = "Share to people you choose, paste their ids in here, then send them a link")
            Spacer(modifier = Modifier.padding(8.dp))
            Divider()
            Spacer(modifier = Modifier.padding(8.dp))
            OutlinedTextField(value = value.value, onValueChange = {
                value.value = it
            }, label = {
                Text(text = "User ID")
            }, trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.outline_add_circle_outline_24),
                    contentDescription = "Add"
                )
            })
            Spacer(modifier = Modifier.padding(8.dp))
            LazyListIds(
                owner = owner,
                usersRead = usersRead,
                usersEdit = usersEdit,
                onClickEdit = onClickEdit,
                onClickRemove = onClickRemove,
            )
        }
    })
}

@Composable
fun LazyListIds(
    modifier: Modifier = Modifier,
    owner: UserState,
    onClickEdit: (UserState) -> Unit,
    onClickRemove: (UserState) -> Unit,
    usersRead: SnapshotStateList<UserState> = mutableStateListOf(),
    usersEdit: SnapshotStateList<UserState> = mutableStateListOf()
) {
    val localContext = LocalContext.current
    LazyColumn(modifier = modifier) {
        item {
            ListCards(user = owner, onClickRead = {
                Toast.makeText(localContext, "You're the owner", Toast.LENGTH_SHORT).show()
            }, onClickEdit = {}, isOwner = true) {
            }
        }
        items(usersRead) {
            ListCards(
                user = it,
                isOwner = false,
                isEdit = false,
                onClickRead = {
                    Toast.makeText(
                        localContext,
                        "People were added will have View permission",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onClickEdit = { onClickEdit(it) }) {
                onClickRemove(it)
            }
        }
        items(usersEdit) {
            ListCards(
                user = it,
                isOwner = false,
                isEdit = true,
                onClickRead = {
                    Toast.makeText(
                        localContext,
                        "People were added will have View permission",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onClickEdit = { onClickEdit(it) }) {
                onClickRemove(it)
            }
        }
    }
}

@Composable
fun ListCards(
    modifier: Modifier = Modifier,
    user: UserState,
    isEdit: Boolean = true,
    isOwner: Boolean = true,
    onClickRead: () -> Unit,
    onClickEdit: () -> Unit,
    onClickRemove: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserHead(
                id = user.id,
                username = user.username,
                textStyle = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.padding(8.dp))
            Text(text = user.username, style = MaterialTheme.typography.bodyLarge)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isOwner) {
                Text(text = "Owner", style = MaterialTheme.typography.labelSmall)
                IconButton(onClick = onClickRead) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_person_24),
                        contentDescription = null // TODO: Add string description
                    )
                }
            } else {
                IconButton(onClick = onClickRead) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_visibility_24),
                        contentDescription = null // TODO: Add string description
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }

                IconButton(onClick = onClickEdit) {
                    Icon(
                        painter = painterResource(id = if (isEdit) R.drawable.outline_edit_24 else R.drawable.outline_edit_off_24),
                        contentDescription = null // TODO: Add string description
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }

                IconButton(onClick = onClickRemove) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_delete_24),
                        contentDescription = null // TODO: Add string description
                    )
                    Spacer(modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}


@Preview
@Composable
fun ShareDialogPreview() {
//    ShareDialog()
}

@Preview(showBackground = true)
@Composable
fun ListCardsPreview() {
    ListCards(
        user = UserState("hi", "Username"),
        onClickEdit = {},
        onClickRead = {},
        onClickRemove = {})
}