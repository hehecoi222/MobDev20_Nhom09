package com.mobdev20.nhom09.quicknote.helpers

import android.util.Base64
import android.util.Log

class Encoder {
    companion object {
        fun encode(value: String): String {
            return Base64.encodeToString(value.toByteArray(), Base64.NO_WRAP)
        }

        fun decode(value: String): String {
            Log.d("DECODER", value)
            return Base64.decode(value, Base64.NO_PADDING).decodeToString()
        }
    }
}