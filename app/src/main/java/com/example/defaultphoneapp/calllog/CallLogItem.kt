package com.example.defaultphoneapp.calllog

/**
 * Created by wenjie on 2024/08/28.
 */
data class CallLogItem(
    val id: Long = 0,
    val name: String? = null,
    val number: String? = null,
    val dateLong: Long = 0,
    val duration: Long = 0,
    val type: Int = 0,
    val new: Int = 0
)