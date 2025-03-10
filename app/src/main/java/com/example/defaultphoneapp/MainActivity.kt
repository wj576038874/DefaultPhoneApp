package com.example.defaultphoneapp

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.loader.app.LoaderManager
import androidx.viewpager2.widget.ViewPager2
import com.example.defaultphoneapp.calllog.CallLogActivity
import com.example.defaultphoneapp.calllog.CallLogContentObserver
import com.example.defaultphoneapp.calllog.CallLogLoadCallback
import com.example.defaultphoneapp.calllog.CallLogService
import com.example.defaultphoneapp.databinding.ActivityMainBinding
import com.example.defaultphoneapp.fragment.HomeCalllogFragment
import com.example.defaultphoneapp.fragment.HomeMainFragment

class MainActivity : AppCompatActivity() {

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                //已经是默认电话应用
            } else {
                //不是默认电话应用
            }
        }

    private val permissions = arrayOf(
        //通话记录 权限组 以下权限一个允许全部就允许，反则亦然
        android.Manifest.permission.READ_CALL_LOG,
        android.Manifest.permission.WRITE_CALL_LOG,

        //电话 权限组 以下权限一个允许全部就允许，反则亦然
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.ANSWER_PHONE_CALLS,
        android.Manifest.permission.READ_PHONE_NUMBERS,
    )

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            //已经允许的权限数量和 声明的权限数量相等 说明权限全部允许
            val granted = result.filter { it.value }
            if (granted.size == permissions.size) {
                startCallLogChangeService()
            } else {
                AlertDialog.Builder(this).setTitle("温馨提示").setMessage("请授予必须要的权限")
                    .setPositiveButton("ok") { dialog, _ ->
                        dialog.cancel()
                        requestPermissions()
                    }.setNegativeButton("cancel", null).show()
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

        val fragments = listOf(HomeMainFragment(), HomeCalllogFragment())
        val pagerAdapter = MyPagerAdapter(this, fragments)
        binding.viewpager.adapter = pagerAdapter

        binding.menu.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.main -> {
                    binding.viewpager.currentItem = 0
                }

                R.id.calllog -> {
                    binding.viewpager.currentItem = 1
                }
            }
            true
        }

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> binding.menu.selectedItemId = R.id.main
                    1 -> binding.menu.selectedItemId = R.id.calllog
                }
            }
        })

        if (permissionGranted()) {
            startCallLogChangeService()
        } else {
            permissionsLauncher.launch(permissions)
        }
    }


    override fun onResume() {
        super.onResume()
        if (isDefaultPhoneCallApp()) {

        } else {
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
    }

    private fun listenPhoneState() {
        val telecomManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telecomManager.registerTelephonyCallback(mainExecutor,
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


    /**
     * 是否被设置为默认电话应用
     */
    private fun isDefaultPhoneCallApp(): Boolean {
        val manger = getSystemService(TELECOM_SERVICE) as TelecomManager
        if (manger.defaultDialerPackage != null) {
            return manger.defaultDialerPackage == packageName
        }
        return false
    }

    /**
     * 开启服务 监听通话记录改变
     */
    private fun startCallLogChangeService() {
        //开启监听通话记录变化服务
        startService(Intent(this, CallLogService::class.java))
    }

    /**
     * 请求权限
     */
    private fun requestPermissions() {
        permissionsLauncher.launch(permissions)
    }

    /**
     * 检测权限是否允许
     */
    private fun permissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, permissions[0]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, permissions[1]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, permissions[2]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, permissions[3]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, permissions[4]
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this, permissions[5]
        ) == PackageManager.PERMISSION_GRANTED
    }
}