package com.example.defaultphoneapp

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telecom.VideoProfile

/**
 * Created by wenjie on 2024/0702.
 */
class NotificationBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "INTENT_HANGUP_CALL_NOTIF_ACTION") {
            PhoneManager.call?.disconnect()
        }else if (intent?.action == "INTENT_ANSWER_CALL_NOTIF_ACTION"){
            PhoneManager.call?.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    companion object{

        const val CHANNEL_ID = "1"

        const val NOTIFICATION_ID = 10001

         fun getCallDeclinePendingIntent(context: Context): PendingIntent {
            val hangupIntent = Intent(context, NotificationBroadcastReceiver::class.java)
            hangupIntent.action = "INTENT_HANGUP_CALL_NOTIF_ACTION"
            hangupIntent.putExtra("INTENT_NOTIF_ID", NOTIFICATION_ID)
            hangupIntent.putExtra("INTENT_REMOTE_ADDRESS", "123456")

            return PendingIntent.getBroadcast(
                context,
                100,
                hangupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

         fun getCallAnswerPendingIntent(context: Context): PendingIntent {
            val answerIntent = Intent(context, NotificationBroadcastReceiver::class.java)
            answerIntent.action = "INTENT_ANSWER_CALL_NOTIF_ACTION"
            answerIntent.putExtra("INTENT_NOTIF_ID", NOTIFICATION_ID)
            answerIntent.putExtra("INTENT_REMOTE_ADDRESS", "654321")

            return PendingIntent.getBroadcast(
                context,
                100,
                answerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}