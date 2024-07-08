package com.example.defaultphoneapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.defaultphoneapp.NotificationBroadcastReceiver.Companion.CHANNEL_ID


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
                    stopService(Intent(this@MyInCallService, MyService::class.java))
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
                    stopService(Intent(this@MyInCallService, MyService::class.java))
                }
            }
        }
    }


    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        //来电号码 去电号码
        val phoneNumber = call.details?.handle?.schemeSpecificPart ?: ""

        //保存call对象用语 电话操作 接听 挂断等等操作
        PhoneManager.call = call

        call.registerCallback(callback)

        //发送一个全屏通知的强提醒，可以点击进入通话页面
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setClass(this, MyPhoneCallActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE)

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val channel =
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_MAX)
                .setName("来电通知").setSound(
                    ringtoneUri,
                    AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                ).build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.createNotificationChannel(channel)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (call.details.state == Call.STATE_RINGING) {
                //incoming 来电
                ContextCompat.startForegroundService(this,
                    Intent(this, MyService::class.java).also {
                        it.putExtra("call_type", 1)
                    })
            } else if (call.details.state == Call.STATE_CONNECTING) {
                //outgoing 去电
                ContextCompat.startForegroundService(this,
                    Intent(this, MyService::class.java).also {
                        it.putExtra("call_type", 2)
                    })
            }
        } else {
            if (call.state == Call.STATE_RINGING) {
                //incoming 来电
                ContextCompat.startForegroundService(this,
                    Intent(this, MyService::class.java).also {
                        it.putExtra("call_type", 1)
                    })
            } else if (call.state == Call.STATE_CONNECTING) {
                //outgoing 去电
                ContextCompat.startForegroundService(this,
                    Intent(this, MyService::class.java).also {
                        it.putExtra("call_type", 2)
                    })
            }
        }

//        //发送全屏通知 兼容低版本的来电通知，可以接听电话和拒绝电话
//        val builder = NotificationCompat.Builder(this, "1").setOngoing(true)
//            .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent)
//            .setFullScreenIntent(pendingIntent, true).setCategory(NotificationCompat.CATEGORY_CALL)
//            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setWhen(System.currentTimeMillis())
//            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentText("新电话")
//            .setContentTitle("新电话来了")
//            .addAction(
//                NotificationCompat.Action.Builder(
//                    R.drawable.ic_call_answer,
//                    "接听",
//                    NotificationBroadcastReceiver.getCallAnswerPendingIntent(this)
//                ).setShowsUserInterface(false).build()
//            ).addAction(
//                NotificationCompat.Action.Builder(
//                    R.drawable.ic_call_end,
//                    "拒绝",
//                    NotificationBroadcastReceiver.getCallAnswerPendingIntent(this)
//                ).setShowsUserInterface(false).build()
//            )
//
//        //判断权限 有权限才发通知
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) == PackageManager.PERMISSION_GRANTED
//        ) {
//            notificationManager.notify(100, builder.build())
//            return
//        }
    }


    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callback)
        PhoneManager.call = null
    }

}