package com.youaji.example.librarys.ui.socket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentSocketOkBinding
import com.youaji.example.librarys.extensions.SocketHistory
import com.youaji.example.librarys.extensions.setSocketUrlChoose
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.getHex
//import com.youaji.module.cmd.CmdService
//import com.youaji.module.cmd.CmdState

class SocketOkFragment : BasicBindingFragment<FragmentSocketOkBinding>() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.spinner.setSocketUrlChoose { ip, port ->
            binding.textIp.setText(ip)
            binding.textPort.setText(port.toString())
            binding.btnStart.performClick()
        }
        binding.btnStart.setOnClickListener {
            if (!checkInput()) {
                printLog("配置的IP或PORT不规范")
                return@setOnClickListener
            }
            SocketHistory.insertHistory(ip, port)
            startSocketService()
        }
        binding.btnStop.setOnClickListener { stopSocket() }
        binding.btnSend.setOnClickListener {
            if (binding.textMessage.text?.isNotEmpty() == true) {
//                CmdService.instance.send(binding.textMessage.text?.toString()?.toByteArray() ?: byteArrayOf())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startSocketService() {
        printLog("--开始绑定Service--")
//        CmdService.instance.connect(requireActivity(), ip, port) { state ->
//            when (state) {
//                CmdState.Start -> printLog("--Start--")
//                CmdState.SendPulseAfter -> printLog("--SendPulseAfter--")
//                CmdState.ServiceDisconnected -> printLog("--onServiceDisconnected--")
//                is CmdState.Connected -> printLog(
//                    "--Connected--" +
//                            "\naction:${state.action}"
//                )
//
//                is CmdState.Disconnect -> printLog(
//                    "--Disconnect--" +
//                            "\naction:${state.action}" +
//                            "\nexception:${state.exception}"
//                )
//
//                is CmdState.Failed -> printLog(
//                    "--Failed--" +
//                            "\naction:${state.action}" +
//                            "\nexception:${state.exception}"
//                )
//
//                is CmdState.ReadResponse -> printLog(
//                    "--ReadResponse--" +
//                            "\naction:${state.action}" +
//                            "\nheadBytes:${parseValue(state.headBytes)}" +
//                            "\nbodyBytes:${parseValue(state.bodyBytes)}"
//                )
//
//
//                is CmdState.WriteResponse -> printLog(
//                    "--WriteResponse--" +
//                            "\naction:${state.action}" +
//                            "\ndata:${parseValue(state.data)}"
//                )
//            }
//        }
    }

    private fun stopSocket() {
//        CmdService.instance.releaseConnect()
    }

    private var ip: String = ""
    private var port: Int = 0
    private fun checkInput(): Boolean {
        var isChecked: Boolean = binding.textIp.text?.isNotEmpty() == true
        if (isChecked) ip = binding.textIp.text.toString()
        isChecked = binding.textPort.text?.isNotEmpty() == true
        if (isChecked) port = binding.textPort.text.toString().toInt()
        return isChecked
    }

    private fun parseValue(value: ByteArray?): String {
        value?.let {
            if (binding.checkHexValue.isChecked) {
                return it.getHex()
            }
            if (binding.radioCharsetUtf8.isChecked) {
                return String(it, Charsets.UTF_8)
            } else if (binding.radioCharsetUtf16.isChecked) {
                return String(it, Charsets.UTF_16)
            }
        } ?: return ""
        return "$value"
    }

    private var loggerText: String = ""

    private fun printLog(log: String) {
        runOnUI {
            loggerText += "$log\n"
            binding.layoutLog.textLog.setText(loggerText)
            binding.layoutLog.textLog.setSelection(loggerText.length)
        }
    }

    private fun runOnUI(action: Runnable) {
        requireActivity().runOnUiThread(action)
    }
}