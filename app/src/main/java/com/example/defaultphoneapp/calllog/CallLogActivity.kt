package com.example.defaultphoneapp.calllog

import androidx.loader.app.LoaderManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.defaultphoneapp.databinding.ActivityCallLogBinding

/**
 * Created by wenjie on 2024/08/28.
 */
class CallLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCallLogBinding

    private val adapter by lazy { CallLogAdapter() }

    private val callLogLoadCallback = CallLogLoadCallback(this) {
        adapter.data = it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallLogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        LoaderManager.getInstance(this).initLoader(1, null, callLogLoadCallback)
    }
}

