package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                button.text = "已经是默认电话应用"
                button.setBackgroundColor(Color.GREEN)
            } else {
                button.text = "不是默认电话应用"
                button.setBackgroundColor(Color.RED)
            }
        }

    private lateinit var button: Button
    private lateinit var btnCall: Button
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        button = findViewById(R.id.btn)
        btnCall = findViewById(R.id.btn_call)
        editText = findViewById(R.id.edit)

        if (isDefaultPhoneCallApp()) {
            button.text = "已经是默认电话应用"
            button.setBackgroundColor(Color.GREEN)
//            startService(Intent(this , MyInCallService::class.java))
        } else {
            button.text = "不是默认电话应用"
            button.setBackgroundColor(Color.RED)
        }

        button.setOnClickListener {
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

        btnCall.setOnClickListener {
            val phoneNumber = editText.text.toString().trim()
            if (phoneNumber.isNotBlank()) {
                call(phoneNumber)
            }
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