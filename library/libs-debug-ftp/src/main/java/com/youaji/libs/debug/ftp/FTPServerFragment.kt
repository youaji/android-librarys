package com.youaji.libs.debug.ftp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.youaji.libs.debug.ftp.databinding.LibsDebugFtpFragmentFtpServerBinding

class FTPServerFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FTPServerFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: LibsDebugFtpFragmentFtpServerBinding
    private lateinit var directory: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = LibsDebugFtpFragmentFtpServerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        directory = requireArguments().getString("directory", "")
    }


}