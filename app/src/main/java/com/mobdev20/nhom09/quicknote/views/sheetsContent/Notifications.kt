package com.mobdev20.nhom09.quicknote.views.sheetsContent

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobdev20.nhom09.quicknote.R
import java.time.Instant

@Composable
fun Notifications(
    modifier: Modifier = Modifier,
    isShowTime: MutableState<Boolean>,
    isShowDate: MutableState<Boolean>,
    hourOfDay: Int,
    minute: Int,
    year: Int,
    month: Int,
    dayOfMonth: Int,
    onClickRemove: () -> Unit = {},
    onClickAdd: () -> Unit = {},
    isExist: Boolean
) {
    val context = LocalContext.current
    Column(modifier = modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(bottom = 16.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isExist)
                IconButton(onClick = onClickRemove) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_delete_24),
                        contentDescription = null, // TODO: Add string description
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            else IconButton(onClick = onClickAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null, // TODO: Add string description
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row {
            OutlinedButton(onClick = {
                isShowTime.value = true
            }) {
                val text =
                    if (DateFormat.is24HourFormat(context))
                        "$hourOfDay:${if (minute >= 10) minute else "0$minute"}"
                    else if (hourOfDay >= 12)
                        "${if (hourOfDay == 12) 12 else hourOfDay - 12}:${if (minute >= 10) minute else "0$minute"} PM"
                    else "$hourOfDay:${if (minute >= 10) minute else "0$minute"} AM"
                Text(text = text)
            }
            Spacer(modifier = Modifier.padding(4.dp))
            OutlinedButton(onClick = {
                isShowDate.value = true
            }) {
                Text(text = "$dayOfMonth-$month-$year")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePicker(
    onSetTime: (hourOfDay: Int, minute: Int) -> Unit,
    isShowTime: MutableState<Boolean>,
    hourOfDay: Int,
    minute: Int
) {
    val timePicker = rememberTimePickerState(initialHour = hourOfDay, initialMinute = minute)
    AlertDialog(onDismissRequest = { isShowTime.value = false }, confirmButton = {
        TextButton(onClick = {
            onSetTime(timePicker.hour, timePicker.minute)
            isShowTime.value = false
        }) {
            Text(text = "Done")
        }
    }, text = {
        TimePicker(state = timePicker)
    }, title = {
        Text(text = "Select Time")
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    onSetDate: (time: Instant) -> Unit,
    isShowDate: MutableState<Boolean>,
    time: Instant
) {
    val datePicker = rememberDatePickerState(initialSelectedDateMillis = time.toEpochMilli())
    DatePickerDialog(onDismissRequest = { isShowDate.value = false }, confirmButton = {
        TextButton(onClick = {
            onSetDate(Instant.ofEpochMilli(datePicker.selectedDateMillis ?: 0))
            isShowDate.value = false
        }) {
            Text(text = "Done")
        }
    }, content = {
        DatePicker(state = datePicker)
    }, modifier = Modifier.fillMaxWidth())
}

@Preview
@Composable
fun PreviewNotificationTab() {
    Notifications(
        hourOfDay = 2,
        minute = 5,
        year = 2023,
        month = 1,
        dayOfMonth = 6,
        isShowTime = mutableStateOf(false),
        isShowDate = mutableStateOf(false),
        isExist = false
    )
}