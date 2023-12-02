package com.mobdev20.nhom09.quicknote.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.common.SignInButton
import com.mobdev20.nhom09.quicknote.R

@Composable
fun SignInButton(
    modifier: Modifier = Modifier,
    visible: Boolean = false,
    onClickSignIn: () -> Unit = {},
    onClickCopy: () -> Unit = {},
) {
    AnimatedVisibility(visible = !visible) {
        Button(
            onClick = onClickSignIn,
            modifier = Modifier
                .width(240.dp)
                .height(45.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            elevation = ButtonDefaults.elevatedButtonElevation(),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google icon",
                    tint = Color.Unspecified,
                )
                Text(
                    text = stringResource(R.string.signin_btn),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
    AnimatedVisibility(visible = visible) {
        Button(
            onClick = onClickCopy,
            modifier = Modifier
                .width(240.dp)
                .height(45.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
            elevation = ButtonDefaults.elevatedButtonElevation(),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_content_copy_24),
                    contentDescription = "Copy user ID",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.copy_uid_btn),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun SignInButtonPreview() {
    SignInButton(visible = true)
}
