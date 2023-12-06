package com.mobdev20.nhom09.quicknote.repositories

import java.time.Instant

interface AlarmScheduler {
    fun cancel(alarm: Int)
    fun schedule(time: Instant, title: String, content: String, id: String): Int
}