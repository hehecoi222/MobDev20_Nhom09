package com.mobdev20.nhom09.quicknote.helpers

import java.util.Random
import java.util.UUID


class Uuid {
    companion object {
        private fun get64LeastSignificantBitsForVersion1(): Long {
            val random = Random()
            val random63BitLong: Long = random.nextLong() and 0x3FFFFFFFFFFFFFFFL
            val variant3BitFlag: Long = 0x800000000000000L
            return random63BitLong or variant3BitFlag
        }

        private fun get64MostSignificantBitsForVersion1(): Long {
            val currentTimeMillis = System.currentTimeMillis()
            val time_low = currentTimeMillis and 0x00000000FFFFFFFFL shl 32
            val time_mid = currentTimeMillis shr 32 and 0xFFFFL shl 16
            val version = (1 shl 12).toLong()
            val time_hi = currentTimeMillis shr 48 and 0x0FFFL
            return time_low or time_mid or version or time_hi
        }

        fun generateType1UUID(): String {
            val most64SigBits = get64MostSignificantBitsForVersion1()
            val least64SigBits = get64LeastSignificantBitsForVersion1()
            return UUID(most64SigBits, least64SigBits).toString()
        }
    }
}