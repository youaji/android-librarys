package com.youaji.libs.debug.ftp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.libs.debug.ftp.databinding.LibsDebugFtpFragmentFtpClientBinding


class FTPClientFragment : Fragment() {


    private lateinit var binding: LibsDebugFtpFragmentFtpClientBinding
    private lateinit var directory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { directory = it.getString("directory", "") }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LibsDebugFtpFragmentFtpClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


}