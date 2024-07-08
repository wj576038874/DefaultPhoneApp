package com.example.defaultphoneapp

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Created by wenjie on 2024/0702.
 */
class MyService : Service() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val callType = intent?.getIntExtra("call_type", 0) ?: 1

        val phoneNumber = PhoneManager.call?.details?.handle?.schemeSpecificPart ?: ""

        val caller =
            Person.Builder().setName("电话")
                .setImportant(false)
                .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_call))
                .build()
        val declineIntent = NotificationBroadcastReceiver.getCallDeclinePendingIntent(this)
        val answerIntent = NotificationBroadcastReceiver.getCallAnswerPendingIntent(this)

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(this, MyPhoneCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = Notification.Builder(this, "1").apply {
            try {
                style = if (callType == 1) {
                    Notification.CallStyle.forIncomingCall(
                        caller, declineIntent, answerIntent
                    )
                } else {
                    Notification.CallStyle.forOngoingCall(
                        caller,
                        declineIntent,
                    )
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            setContentText(phoneNumber)
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setCategory(Notification.CATEGORY_CALL)
            setVisibility(Notification.VISIBILITY_PUBLIC)
            setWhen(System.currentTimeMillis())
            setAutoCancel(false)
            setShowWhen(true)
            setOngoing(true)
            setFullScreenIntent(pendingIntent, true)
            setContentIntent(pendingIntent)
        }

        val notification = builder.build()
//        val mgr = getSystemService(NotificationManager::class.java)
//        mgr.notify(100 , notification)
        startForeground(100, notification)
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("asd", "MyService onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}