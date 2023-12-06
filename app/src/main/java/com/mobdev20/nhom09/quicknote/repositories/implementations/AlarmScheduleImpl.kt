package com.mobdev20.nhom09.quicknote.repositories.implementations

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.mobdev20.nhom09.quicknote.MainActivity
import com.mobdev20.nhom09.quicknote.datasources.AlarmReceiver
import com.mobdev20.nhom09.quicknote.repositories.AlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class AlarmScheduleImpl @Inject constructor(@ApplicationContext private val context: Context) : AlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    override fun schedule(time: Instant, title: String, content: String, id: String): Int {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TITLE", title)
            putExtra("CONTENT", content)
        }
        val alarmTime = time.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(
                alarmTime,
                PendingIntent.getActivities(
                    context,
                    time.toString().hashCode(),
                    arrayOf(Intent(context, MainActivity::class.java).apply { putExtra("ID", id) }),
                    PendingIntent.FLAG_IMMUTABLE
                )
            ),
            PendingIntent.getBroadcast(
                context,
                (time.toString() + id).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        Log.d("NOTI_LOG", "Created alarm")
        return (time.toString() + id).hashCode()
    }

    override fun cancel(id: Int) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                id,
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }
}

@InstallIn(SingletonComponent::class)
@Module
abstract class AlarmScheduleModule {

    @Binds
    abstract fun bindAlarmScheduler(alarmScheduleImpl: AlarmScheduleImpl): AlarmScheduler
}