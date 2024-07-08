package com.example.defaultphoneapp

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.defaultphoneapp.NotificationBroadcastReceiver.Companion.CHANNEL_ID
import com.example.defaultphoneapp.NotificationBroadcastReceiver.Companion.NOTIFICATION_ID
import java.lang.StringBuilder

/**
 * Created by wenjie on 2024/0702.
 */
class MyService : Service() {

    companion object {
        const val STOP_FOREGROUND = "stop_foreground"
        const val CALL_TYPE = "call_type"
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val callType = intent?.getIntExtra(CALL_TYPE, 0)

        //如果是停止服务
        if (intent?.action == STOP_FOREGROUND) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val phoneNumber = PhoneManager.call?.details?.handle?.schemeSpecificPart ?: "电话"

        val declineIntent = NotificationBroadcastReceiver.getCallDeclinePendingIntent(this)
        val answerIntent = NotificationBroadcastReceiver.getCallAnswerPendingIntent(this)

        val callIntent = Intent(Intent.ACTION_MAIN, null)
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
        callIntent.setClass(this, MyPhoneCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, callIntent, PendingIntent.FLAG_IMMUTABLE)

        val caller = Person.Builder().setName(phoneNumber).setImportant(false)
            .setIcon(IconCompat.createWithResource(this, android.R.drawable.ic_menu_call)).build()
        val builder = NotificationCompat.Builder(this, CHANNEL_ID).apply {
            try {
                setStyle(
                    if (callType == 1) {
                        NotificationCompat.CallStyle.forIncomingCall(
                            caller, declineIntent, answerIntent
                        )
                    } else {
                        NotificationCompat.CallStyle.forOngoingCall(
                            caller,
                            declineIntent,
                        )
                    }
                )
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                //如果失败的话 可以使用自定义通知栏
                if (callType == 1) {
                    //来电
                    createIncomingCallNotification()
                } else {
                    //去电
                    createCallNotification()
                }
            }
            setSmallIcon(R.drawable.ic_launcher_foreground)
            setCategory(NotificationCompat.CATEGORY_CALL)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setWhen(System.currentTimeMillis())
            setAutoCancel(false)
            setShowWhen(true)
            setOngoing(true)
            setFullScreenIntent(pendingIntent, true)
            setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }


    /**
     * 去电通知
     */
    private fun NotificationCompat.Builder.createCallNotification(): NotificationCompat.Builder {
        this.setContentTitle("")
        setContentText("正在拨打电话")
        addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_call_end,
                "挂断",
                NotificationBroadcastReceiver.getCallAnswerPendingIntent(this@MyService)
            ).setShowsUserInterface(false).build()
        )
        return this
    }

    /**
     * 自定义来电通话通知
     */
    private fun NotificationCompat.Builder.createIncomingCallNotification(): NotificationCompat.Builder {
        val notificationLayoutHeadsUp = RemoteViews(
            packageName, R.layout.call_incoming_notification_heads_up
        )
        notificationLayoutHeadsUp.setTextViewText(R.id.caller, "displayName")
        notificationLayoutHeadsUp.setTextViewText(R.id.sip_uri, "phone number")
        notificationLayoutHeadsUp.setTextViewText(R.id.incoming_call_info, "info")

        this.setStyle(NotificationCompat.DecoratedCustomViewStyle()).setContentTitle("phoneNumber")
            .setContentText("来新电话了").addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_answer,
                    "接听",
                    NotificationBroadcastReceiver.getCallAnswerPendingIntent(this@MyService)
                ).setShowsUserInterface(false).build()
            ).addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_call_end,
                    "拒绝",
                    NotificationBroadcastReceiver.getCallAnswerPendingIntent(this@MyService)
                ).setShowsUserInterface(false).build()
            ).setCustomHeadsUpContentView(notificationLayoutHeadsUp)
        return this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("asd", "MyService onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}