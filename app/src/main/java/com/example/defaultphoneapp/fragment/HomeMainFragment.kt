package com.example.defaultphoneapp.fragment

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.telecom.TelecomManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.TELECOM_SERVICE
import androidx.fragment.app.Fragment
import com.example.defaultphoneapp.databinding.FragmentMainBinding

/**
 * Created by wenjie on 2025/03/07.
 */
class HomeMainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding: FragmentMainBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    /**
     * 拨打电话
     */
    @SuppressLint("MissingPermission")
    private fun call(phoneNumber: String) {
        val telecomManager = requireContext().getSystemService(TELECOM_SERVICE) as TelecomManager
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
            phoneAccount.label//运营商
            phoneAccount.subscriptionAddress//号码
            val extras = Bundle()
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandles[0])
            telecomManager.placeCall(uri, extras)
        } else {
            //没有sim卡无法拨打
            Toast.makeText(requireContext(), "无sim卡", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}