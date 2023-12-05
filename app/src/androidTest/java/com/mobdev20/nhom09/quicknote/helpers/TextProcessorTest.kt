package com.mobdev20.nhom09.quicknote.helpers

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.widget.EditText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextProcessorTest {
    @Test
    fun renderFormat() {
//      EDIT TEXT : Hello
        var noteBody = EditText(ApplicationProvider.getApplicationContext())
        noteBody.setText("Hello")

//      Case Bold & Italic
        TextProcessor.renderFormat(noteBody, Typeface.BOLD)
        println("Note Body : ${noteBody.text}")

        var span = SpannableString(noteBody.text)
        span.getSpans(0, span.length, StyleSpan::class.java)
        if (span.isNotEmpty()) {
            println(span.toString())
        }
//      Case Underline
        TextProcessor.renderFormat(noteBody, 3)
        println("Note Body : ${noteBody.text}")

        var span_ = SpannableString(noteBody.text)
        span_.getSpans(0, span_.length, UnderlineSpan::class.java)
        if (span.isNotEmpty()) {
            println(span.toString())
        }
    }
}