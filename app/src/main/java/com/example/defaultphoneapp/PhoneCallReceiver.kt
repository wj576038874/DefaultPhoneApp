package com.example.defaultphoneapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log

/**
 * 通话广播接收器
 */
class PhoneCallReceiver : BroadcastReceiver() {

    /**
     * 是否为来电，true为来电，false为去电
     */
    var isIncomingCall: Boolean? = null
        private set

    /**
     * 只记录呼入的号码
     * 则数组里面有的号码证明是呼入 查询到没有的号码为呼出
     */
    var isInComingCallFlagArr: MutableList<String> = mutableListOf()

    /**
     * 是否正在通话中，true为通话状态，false为空闲
     */
    var isInCall: Boolean = false
        private set

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                //去电首次会发生此Action
                //拨出号码:这个地方只有你拨号的瞬间会调用
                val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                if (phoneNumber.isNullOrEmpty()) {
                    Log.d(TAG, "phoneNumber is null or empty")
                    return
                }
                onOutCall(phoneNumber)
            }
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                //来电、去电都会发生此Action
                val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if (phoneNumber.isNullOrEmpty()) {
                    Log.d(TAG, "phoneNumber is null or empty")
                    return
                }
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                Log.d(TAG, "state change -> phoneNumber: $phoneNumber, state: $state")
                when (state) {
                    TelephonyManager.EXTRA_STATE_IDLE -> {
                        Log.d(TAG, "EXTRA_STATE_IDLE -> ${TelephonyManager.EXTRA_STATE_IDLE}")
                        //空闲状态:不管是去电还是来电通话结束都会调用，不管是哪一方挂断
                        onIdle(phoneNumber, checkIsInComingCall(phoneNumber = phoneNumber))
                    }
                    TelephonyManager.EXTRA_STATE_RINGING -> {
                        //来电响铃:只有来电的时候会调用
                        Log.d(TAG, "CALL_STATE_RINGING -> ${TelephonyManager.CALL_STATE_RINGING}")
                        isIncomingCall = true
                        isInComingCallFlagArr.add(phoneNumber)
                    }
                    TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                        //接通
                        //如果是来电，这个必须点击接听按钮才会调用
                        //如果是拨打，那么一开始就会调用，而不是打通了之后才会调用
                        Log.d(TAG, "CALL_STATE_OFFHOOK -> ${TelephonyManager.CALL_STATE_OFFHOOK}")
                        if (isIncomingCall == true) {
                            onInCall(phoneNumber)
                        } else {
                            onOutCallConnect(phoneNumber)
                        }
                    }
                }
            }
        }
    }

    private fun onOutCall(phoneNumber: String) {
        isIncomingCall = false
        isInCall = true
    }

    private fun onOutCallConnect(phoneNumber: String){
    }

    private fun onInCall(phoneNumber: String) {
        //来电时开始录音
        isInCall = true
    }

    private fun onIdle(phoneNumber: String, isIncomingState: Boolean) {
        PhoneManager.call?.disconnect()
        isInComingCallFlagArr.clear()
        isIncomingCall = null
        isInCall = false
    }

    /**
     * 检查号码是不是呼入的号码
     * @param phoneNumber String
     * @return Boolean
     */
    private fun checkIsInComingCall(phoneNumber: String): Boolean{
        var isIncomingCall = false
        isInComingCallFlagArr.forEach {
            if(it == phoneNumber) isIncomingCall = true
        }
        return isIncomingCall
    }

    companion object {
        private const val TAG = "PhoneCallReceiver"

    }
}