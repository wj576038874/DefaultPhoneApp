package com.example.defaultphoneapp.calllog

import android.annotation.SuppressLint
import android.graphics.Color
import android.provider.CallLog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.defaultphoneapp.R
import com.example.defaultphoneapp.databinding.CallLogItemBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CallLogAdapter : RecyclerView.Adapter<CallLogAdapter.MyHolder>() {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    var data: MutableList<CallLogItem>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    class MyHolder(val binding: CallLogItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(
            CallLogItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        val item = data?.get(position) ?: return
        holder.binding.tvPhone.text = item.number
        holder.binding.tvTime.text = simpleDateFormat.format(item.dateLong)
        holder.binding.tvName.text = item.name ?: "未知"
        when(item.type){
            CallLog.Calls.MISSED_TYPE -> {
                holder.binding.tvName.setTextColor(Color.RED)
                holder.binding.tvPhone.setTextColor(Color.RED)
                holder.binding.tvDuration.setTextColor(Color.RED)
                holder.binding.tvDuration.text = "${getTypeStr(item.type)}"
            }
            else ->{
                holder.binding.tvDuration.text = "${getTypeStr(item.type)} ${item.duration} 秒"
                holder.binding.tvDuration.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.black))
                holder.binding.tvName.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.black))
                holder.binding.tvPhone.setTextColor(ContextCompat.getColor(holder.itemView.context,
                    R.color.black))
            }
        }
    }

    private fun getTypeStr(type: Int): String {
        return when (type) {
            CallLog.Calls.INCOMING_TYPE -> "呼入"
            CallLog.Calls.OUTGOING_TYPE -> "呼出"
            CallLog.Calls.MISSED_TYPE -> "未接"
            CallLog.Calls.VOICEMAIL_TYPE -> "语音信箱"
            CallLog.Calls.REJECTED_TYPE -> "挂断"
            CallLog.Calls.BLOCKED_TYPE -> "阻塞"
            else -> "未知"
        }
    }
}