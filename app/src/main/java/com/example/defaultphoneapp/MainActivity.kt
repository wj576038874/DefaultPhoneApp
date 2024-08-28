package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.telecom.TelecomManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.CALL_STATE_IDLE
import android.telephony.TelephonyManager.CALL_STATE_OFFHOOK
import android.telephony.TelephonyManager.CALL_STATE_RINGING
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.loader.app.LoaderManager
import com.example.defaultphoneapp.calllog.CallLogActivity
import com.example.defaultphoneapp.calllog.CallLogContentObserver
import com.example.defaultphoneapp.calllog.CallLogLoadCallback
import com.example.defaultphoneapp.calllog.CallLogService
import com.example.defaultphoneapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.btn.text = "已经是默认电话应用"
                binding.btn.setBackgroundColor(Color.GREEN)
            } else {
                binding.btn.text = "不是默认电话应用"
                binding.btn.setBackgroundColor(Color.RED)
            }
        }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //监听通话记录变化服务
        startService(Intent(this , CallLogService::class.java))

//        val phoneCallReceiver = PhoneCallReceiver()
//        val intentFilter = IntentFilter()
//        intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
//        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL)
//        registerReceiver(phoneCallReceiver, intentFilter)
//
//        listenPhoneState()

        if (isDefaultPhoneCallApp()) {
            binding.btn.text = "已经是默认电话应用"
            binding.btn.setBackgroundColor(Color.GREEN)
//            startService(Intent(this , MyInCallService::class.java))
        } else {
            binding.btn.text = "不是默认电话应用"
            binding.btn.setBackgroundColor(Color.RED)
        }

        binding.btn.setOnClickListener {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
                roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            } else {
                val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
                intent.putExtra(
                    TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName
                )
            }
            activityResultLauncher.launch(intent)
        }

        binding.btnCall.setOnClickListener {
            val phoneNumber = binding.edit.text.toString().trim()
            if (phoneNumber.isNotBlank()) {
                call(phoneNumber)
            }
        }

        binding.btnCallLog.setOnClickListener {
            startActivity(Intent(this, CallLogActivity::class.java))
        }

       binding.btnCall2.setOnClickListener {
            startActivity(Intent(this, MyPhoneCallActivity::class.java))
        }
    }

    private fun listenPhoneState() {
        val telecomManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telecomManager.registerTelephonyCallback(
                mainExecutor,
                object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) {
                        when (state) {
                            CALL_STATE_IDLE -> {
                                Log.d("listenPhoneState", "Call state: 挂断");
                            }

                            CALL_STATE_RINGING -> {
                                Log.d("listenPhoneState", "Call state: 响铃");
                            }

                            CALL_STATE_OFFHOOK -> {
                                Log.d("listenPhoneState", "Call state: 接通");
                            }

                        }
                    }
                })
        }
    }

    @SuppressLint("MissingPermission")
    private fun call(phoneNumber: String) {
        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val uri = Uri.fromParts("tel", phoneNumber, null)
        val phoneAccountHandles = telecomManager.callCapablePhoneAccounts
        if (phoneAccountHandles.size > 0) {
            if (phoneAccountHandles.size > 1) {
                //双卡
            } else {
                //单卡
            }
            val phoneAccountHandle = phoneAccountHandles[0]
            val phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle)//sim1的卡信息
            val extras = Bundle()
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandles[0])
            telecomManager.placeCall(uri, extras)
        } else {
            //没有sim卡无法拨打
            Toast.makeText(this, "无sim卡", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isDefaultPhoneCallApp(): Boolean {
        val manger = getSystemService(TELECOM_SERVICE) as TelecomManager
        if (manger.defaultDialerPackage != null) {
            return manger.defaultDialerPackage == packageName
        }
        return false
    }
}