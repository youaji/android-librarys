package com.youaji.example.librarys.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentLibFtpClientBinding
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.logger.logDebug

class LibFTPClientFragment : BasicBindingFragment<FragmentLibFtpClientBinding>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
//        val nativeLibTestSonic = NativeLibTestSonic()
//        nativeLibTestSonic.start(object : OnAcousticImageCallback {
//            override fun onCallback(code: Int, argb32Buffer: ByteArray?) {
//                printLog("OnAcousticImageCallback --- $code === $argb32Buffer")
//            }
//        })
    }

    private var loggerText: String = ""
    private fun printLog(log: String) {
        logDebug(log)
        runOnUI {
            loggerText += "-> $log\n"
            binding.layoutLog.textLog.setText(loggerText)
            binding.layoutLog.textLog.setSelection(loggerText.length)
        }
    }

    private fun runOnUI(action: Runnable) {
        requireActivity().runOnUiThread(action)
    }
}