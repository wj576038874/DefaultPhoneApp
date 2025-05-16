package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.defaultphoneapp.databinding.ActivitySmsBinding
import java.util.ArrayList

/**
 * Created by wenjie on 2025/05/16.
 */
class SmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySmsBinding

    private lateinit var subscriptionManager: SubscriptionManager

    private val sendReceiverAction = "sendReceiverAction"

    private val defaultSmsApps =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_OK) {
                requestSmsDefaultApp()
            }
        }

    private val smsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                requestSmsPermission()
            }
        }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySmsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        subscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        getSimCardList().forEachIndexed { index, it ->
            val radioButton = RadioButton(this)
            radioButton.tag = it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                radioButton.text =
                    "${it.displayName}:${subscriptionManager.getPhoneNumber(it.subscriptionId)}"
            } else {
                radioButton.text = "${it.displayName}:${it.number}"
            }
            radioButton.id = index
            binding.RadioGroup.addView(radioButton)
        }

        binding.btnSend2.setOnClickListener {
            val count = binding.count.text.toString().trim()
            if (count.isEmpty()) {
                Toast.makeText(this, "请输入发送数量", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            send(count.toInt())
        }

        binding.btnSend.setOnClickListener {
            send(1)
        }
    }


    private fun send(count: Int) {
        val phoneNumber = binding.etPhone.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "请输入接收号码", Toast.LENGTH_SHORT).show()
            return
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入短信内通", Toast.LENGTH_SHORT).show()
            return
        }

        val viewId = binding.RadioGroup.checkedRadioButtonId
        if (viewId < 0) {
            Toast.makeText(this, "请选择发送的卡", Toast.LENGTH_SHORT).show()
        } else {
            val info = findViewById<RadioButton>(viewId).tag as SubscriptionInfo
            send(count, phoneNumber, content, info.subscriptionId)
        }
    }

    override fun onResume() {
        super.onResume()
        requestSmsPermission()
//        if (!isDefaultSmsApp()) {
//            requestSmsDefaultApp()
//        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun send(count: Int, phone: String, content: String, subId: Int) {
        binding.tv.text = "发送中..."
        val simInfoList = subscriptionManager.activeSubscriptionInfoList
        val currentSmsManager =
            if (simInfoList != null && simInfoList.isNotEmpty() && simInfoList.size > 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
                } else {
                    SmsManager.getSmsManagerForSubscriptionId(subId)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    getSystemService(SmsManager::class.java).createForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId())
                } else {
                    SmsManager.getDefault()
                }
            }
        val divideContents = currentSmsManager.divideMessage(content)
        val numParts: Int = divideContents.size
        val pendingIntents = ArrayList<PendingIntent>()
        for (i in 0 until numParts) pendingIntents.add(
            PendingIntent.getBroadcast(
                this, 0, Intent(sendReceiverAction), PendingIntent.FLAG_IMMUTABLE
            )
        )
        currentSmsManager.sendMultipartTextMessage(
            phone, null, divideContents, pendingIntents, pendingIntents
        )
        val sendReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            override fun onReceive(context: Context?, intent: Intent?) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        binding.tv.text = "发送成功"
                    }

                    else -> {
                        binding.tv.text = "发送失败"
                    }
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(sendReceiverAction)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(sendReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(sendReceiver, intentFilter)
        }
    }


    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermission.launch(android.Manifest.permission.SEND_SMS)
        }
    }


    private fun getSimCardList(): List<SubscriptionInfo> {
        val subscriptionManager =
            getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
        val simInfoList = subscriptionManager.activeSubscriptionInfoList
        return simInfoList
    }


    private fun isDefaultSmsApp(): Boolean {
        val defaultAppName = Telephony.Sms.getDefaultSmsPackage(this)
        return defaultAppName != null && packageName == defaultAppName
    }

    private fun requestSmsDefaultApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm: RoleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            defaultSmsApps.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
        } else {
            val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
            defaultSmsApps.launch(intent)
        }
    }
}