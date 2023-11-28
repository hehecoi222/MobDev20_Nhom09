package com.mobdev20.nhom09.quicknote.views

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization

@Composable
fun NoteTitleTextField(
    value: String,
    onValueChange: (String) -> Unit = {},
    moveFocus: () -> Unit = {}
) {
    val focusRequester = remember {
        FocusRequester()
    }
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .focusRequester(focusRequester = focusRequester)
            .fillMaxWidth()
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
        })
    )

    LaunchedEffect((Unit)) {
        focusRequester.requestFocus()
    }
}