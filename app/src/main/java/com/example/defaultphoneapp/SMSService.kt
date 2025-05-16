package com.example.defaultphoneapp

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.IBinder

class SMSService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}