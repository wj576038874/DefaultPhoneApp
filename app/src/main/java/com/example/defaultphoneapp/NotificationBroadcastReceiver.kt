package com.example.defaultphoneapp

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
}