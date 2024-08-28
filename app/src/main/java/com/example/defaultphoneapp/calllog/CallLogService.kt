package com.example.defaultphoneapp.calllog

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.CallLog

/**
 * Created by wenjie on 2024/08/28.
 */
class CallLogService : Service() {

    override fun onCreate() {
        super.onCreate()
        //监听系统通话记录变化
        val contentObserver = CallLogContentObserver(Handler(Looper.getMainLooper()))
        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI , true , contentObserver)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}