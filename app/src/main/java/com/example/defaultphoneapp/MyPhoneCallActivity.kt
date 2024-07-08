package com.example.defaultphoneapp

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.telecom.Call
import android.telecom.VideoProfile
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible

/**
 * Created by wenjie on 2024/06/27.
 */
class MyPhoneCallActivity : AppCompatActivity() {

    private lateinit var tvDuration: Chronometer
    private lateinit var btnHangup: ImageButton
    private lateinit var btnAnswer: ImageButton
    private lateinit var tvPhoneNumber: TextView

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
                    btnAnswer.visibility = View.GONE
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
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvDuration = findViewById(R.id.tv_duration)
        tvPhoneNumber = findViewById(R.id.tv_phone)
        btnAnswer = findViewById(R.id.btn_start)
        btnHangup = findViewById(R.id.btn_end)


        val phoneNumber = PhoneManager.call?.details?.handle?.schemeSpecificPart ?: "电话"
        tvPhoneNumber.text = phoneNumber

        PhoneManager.call?.registerCallback(callback)

        if (PhoneManager.call?.state == Call.STATE_ACTIVE) {
            //已经接通
            tvDuration.visibility = View.VISIBLE
            tvDuration.base = SystemClock.elapsedRealtime()
            tvDuration.start()
            btnAnswer.visibility = View.GONE
        } else {
            //正在链接
            btnAnswer.visibility = View.VISIBLE
        }

        btnAnswer.setOnClickListener {
            PhoneManager.call?.answer(VideoProfile.STATE_AUDIO_ONLY)
            tvDuration.visibility = View.VISIBLE
            tvDuration.base = SystemClock.elapsedRealtime()
            tvDuration.start()
        }

        btnHangup.setOnClickListener {
            PhoneManager.call?.disconnect()
            tvDuration.stop()
            finish()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        PhoneManager.call?.unregisterCallback(callback)
    }
}