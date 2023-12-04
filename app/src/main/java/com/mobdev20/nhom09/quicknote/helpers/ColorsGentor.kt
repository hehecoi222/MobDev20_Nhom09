package com.mobdev20.nhom09.quicknote.helpers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.SchemeTonalSpot

fun ColorsGentor(color: Color, isDark: Boolean): DynamicScheme {
    return SchemeTonalSpot(Hct.fromInt(color.toArgb()), isDark, 0.0)
}