package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


/**
 * Created by wenjie on 2024/06/27.
 * 这个服务不需要我们启动，系统在接收到电话过来之后，会自动启动这个服务
 */
class MyInCallService : InCallService() {


    override fun onCreate() {
        super.onCreate()
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        PhoneManager.call = call
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(this, MyPhoneCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel("1", "Incoming Calls", NotificationManager.IMPORTANCE_MAX)

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        channel.setSound(
            ringtoneUri, AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )

        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, "1")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentText("新电话")
            .setContentTitle("新电话来了")

        mgr.notify(100, builder.build())

    }


    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
    }

}