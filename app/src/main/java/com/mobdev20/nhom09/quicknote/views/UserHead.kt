package com.mobdev20.nhom09.quicknote.views


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import com.mobdev20.nhom09.quicknote.ui.theme.toHslColor

@Composable
fun UserHead(
    id: String,
    username: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        val color = remember(id, username) {
            Color("$id / $username".toHslColor())
        }
        val initials = (username.replace(" ","").take(2)).uppercase()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = textStyle, color = Color.White)
    }
}

@Composable
fun UserInfo(modifier: Modifier = Modifier, id: String, username: String, visible: Boolean = false) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        UserHead(id = id, username = username, size = 120.dp, textStyle = MaterialTheme.typography.displaySmall)
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = username, style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
        AnimatedVisibility(visible = visible) {
            Column {
                Spacer(modifier = Modifier.padding(4.dp))
                Text(text = "${stringResource(id = R.string.user_id)} $id", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
@Preview
@Composable
fun UserHeadPreview() {
    UserHead(id = "15", username = "Hi Username", size = 60.dp,)
}

@Preview
@Composable
fun UserInfoPreview() {
    UserInfo(id = "15", username = "H Username", visible = true)
}
