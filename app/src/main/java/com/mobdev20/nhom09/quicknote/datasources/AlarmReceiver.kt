package com.mobdev20.nhom09.quicknote.datasources

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mobdev20.nhom09.quicknote.R

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("TITLE") ?: return
        val content = intent.getStringExtra("CONTENT") ?: return
        val channelId = "note_noti"
        context?.let { ctx ->
            val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(Notification.DEFAULT_ALL)
            notificationManager.notify(1, builder.build())
        }
    }
}