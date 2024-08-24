package com.youaji.libs.debug.ftp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.youaji.libs.debug.ftp.databinding.LibsDebugFtpFragmentFtpMainBinding


class FTPMainFragment : Fragment() {

    private lateinit var binding: LibsDebugFtpFragmentFtpMainBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LibsDebugFtpFragmentFtpMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnClient.setOnClickListener {
            findNavController().navigate(R.id.action_FTPMainFragment_to_FTPClientFragment)
        }
        binding.btnServer.setOnClickListener {
            findNavController().navigate(R.id.action_FTPMainFragment_to_FTPServerFragment)
        }
    }


}