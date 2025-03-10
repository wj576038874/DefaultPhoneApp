package com.example.defaultphoneapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.defaultphoneapp.calllog.CallLogAdapter
import com.example.defaultphoneapp.calllog.CallLogLoadCallback
import com.example.defaultphoneapp.databinding.FragmentCalllogBinding

/**
 * Created by wenjie on 2025/03/07.
 */
class HomeCalllogFragment : Fragment() {

    private var _binding: FragmentCalllogBinding? = null

    private val binding: FragmentCalllogBinding
        get() = _binding!!

    private val adapter by lazy { CallLogAdapter() }

    private val callLogLoadCallback by lazy {
        CallLogLoadCallback(requireContext()) {
            adapter.data = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalllogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        LoaderManager.getInstance(this).initLoader(1, null, callLogLoadCallback)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}