package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


/**
 * Created by wenjie on 2024/06/27.
 * 这个服务不需要我们启动，系统在接收到电话过来之后，会自动启动这个服务
 */
class MyInCallService : InCallService() {


    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d("phone", "onStateChanged $state")
            when (state) {
                Call.STATE_NEW -> {
                    Log.d("phone", "==STATE_NEW")
                }

                Call.STATE_DIALING -> {
                    Log.d("phone", "==STATE_DIALING")
                }

                Call.STATE_RINGING -> {
                    Log.d("phone", "==STATE_RINGING")
                }

                Call.STATE_HOLDING -> {
                    Log.d("phone", "==STATE_HOLDING")
                }

                Call.STATE_SELECT_PHONE_ACCOUNT -> {
                    Log.d("phone", "==STATE_SELECT_PHONE_ACCOUNT")
                }

                Call.STATE_PULLING_CALL -> {
                    Log.d("phone", "==STATE_PULLING_CALL")
                }

                Call.STATE_AUDIO_PROCESSING -> {
                    Log.d("phone", "==STATE_AUDIO_PROCESSING")
                }

                Call.STATE_SIMULATED_RINGING -> {
                    Log.d("phone", "==STATE_SIMULATED_RINGING")
                }

                Call.STATE_ACTIVE -> {
                    //电话接通
                    Log.d("phone", "==STATE_ACTIVE")
                }

                Call.STATE_CONNECTING -> {
                    //拨打中 未接通
                    Log.d("phone", "STATE_CONNECTING")
                }

                Call.STATE_DISCONNECTING -> {
                    Log.d("phone", "STATE_DISCONNECTING")
                }

                Call.STATE_DISCONNECTED -> {
                    //挂断
                    Log.d("phone", "STATE_DISCONNECTED")
                }
            }
        }
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callback)

        if (call.details.state == Call.STATE_RINGING) {
            //incoming 来电
        } else if (call.details.state == Call.STATE_CONNECTING) {
            //outgoing 去电
        }

        //保存call对象用语 电话操作 接听 挂断等等操作
        PhoneManager.call = call


        //发送一个全屏通知的强提醒，可以点击进入通话页面
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(this, MyPhoneCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel("1", "Incoming Calls", NotificationManager.IMPORTANCE_MAX)

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        channel.setSound(
            ringtoneUri,
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        )

        val mgr = getSystemService(NotificationManager::class.java)
        mgr.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, "1").setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentText("新电话")
            .setContentTitle("新电话来了")

        mgr.notify(100, builder.build())

    }


    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
        PhoneManager.call = null
    }

}