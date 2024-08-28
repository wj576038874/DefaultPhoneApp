package com.example.defaultphoneapp.calllog

import android.database.ContentObserver
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager

/**
 * Created by wenjie on 2024/08/28.
 */
class CallLogContentObserver(private val context: AppCompatActivity, handler: Handler) :
    ContentObserver(handler) {

    private val callLogLoadCallback = CallLogLoadCallback(context)

    override fun deliverSelfNotifications(): Boolean {
        return true
    }

    override fun onChange(selfChange: Boolean) {
        super.onChange(selfChange)
        Log.e("asd", "通话结束之后 会监听到 通话记录变化$selfChange")
        LoaderManager.getInstance(context).initLoader(1, null, callLogLoadCallback)
    }
}