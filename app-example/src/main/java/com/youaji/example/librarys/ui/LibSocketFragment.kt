package com.youaji.example.librarys.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.youaji.example.librarys.databinding.FragmentLibSocketBinding
import com.youaji.example.librarys.extensions.Utils
import com.youaji.libs.socket.OnMessageReceivedListener
import com.youaji.libs.socket.OnSocketStateListener
import com.youaji.libs.socket.Socket
import com.youaji.libs.socket.tcp.TCPClient
import com.youaji.libs.socket.tcp.TCPServer
import com.youaji.libs.socket.udp.UDPClient
import com.youaji.libs.socket.udp.UDPMulticast
import com.youaji.libs.socket.udp.UDPServer
import com.youaji.libs.ui.basic.BasicBindingFragment
import java.io.Closeable

class LibSocketFragment : BasicBindingFragment<FragmentLibSocketBinding>() {

    private var currCheckedId = -1

    private var inputIP = ""
    private var inputPort1 = 0
    private var inputPort2 = 0
    private var inputMessage = ""

    private var socket: Socket<Closeable?>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initType()
        binding.btnStart.setOnClickListener { setStart() }
        binding.btnStop.setOnClickListener { setStop() }
        binding.btnSend.setOnClickListener { setSend() }
    }

    @SuppressLint("SetTextI18n")
    private fun initType() {
        binding.groupType.setOnCheckedChangeListener { _, checkedId ->
            currCheckedId = checkedId
            when (checkedId) {
                binding.radioTcpClient.id -> {
                    binding.textIp.text = "连接地址:"
                    binding.editIp.setText("192.168.1.1")

                    binding.textPort1.text = "连接端口:"
                    binding.textPort1.isVisible = true
                    binding.textPort2.text = ""
                    binding.textPort2.isVisible = false

                    binding.editPort1.setText("9001")
                    binding.editPort1.isVisible = true
                    binding.editPort2.setText("")
                    binding.editPort2.isVisible = false
                }

                binding.radioTcpServer.id -> {
                    binding.textIp.text = "本地地址:"
                    binding.editIp.setText(Utils.getLocalAddress())

                    binding.textPort1.text = "监听端口:"
                    binding.textPort1.isVisible = true
                    binding.textPort2.text = ""
                    binding.textPort2.isVisible = false

                    binding.editPort1.setText("9002")
                    binding.editPort1.isVisible = true
                    binding.editPort2.setText("")
                    binding.editPort2.isVisible = false
                }

                binding.radioUdpClient.id -> {
                    binding.textIp.text = "广播地址:"
                    binding.editIp.setText("192.168.1.2")

                    binding.textPort1.text = "广播端口:"
                    binding.textPort1.isVisible = true
                    binding.textPort2.text = "本地端口"
                    binding.textPort2.isVisible = false

                    binding.editPort1.setText("9003")
                    binding.editPort1.isVisible = true
                    binding.editPort2.setText("9004")
                    binding.editPort2.isVisible = true
                }

                binding.radioUdpServer.id -> {
                    binding.textIp.text = "本地地址:"
                    binding.editIp.setText(Utils.getLocalAddress())

                    binding.textPort1.text = "监听端口:"
                    binding.textPort1.isVisible = true
                    binding.textPort2.text = ""
                    binding.textPort2.isVisible = false

                    binding.editPort1.setText("9005")
                    binding.editPort1.isVisible = true
                    binding.editPort2.setText("")
                    binding.editPort2.isVisible = false
                }

                binding.radioUdpMulticast.id -> {
                    binding.textIp.text = "组播地址:"
                    binding.editIp.setText("224.1.1.1")

                    binding.textPort1.text = "组播端口:"
                    binding.textPort1.isVisible = true
                    binding.textPort2.text = ""
                    binding.textPort2.isVisible = false

                    binding.editPort1.setText("9006")
                    binding.editPort1.isVisible = true
                    binding.editPort2.setText("")
                    binding.editPort2.isVisible = false
                }
            }
        }
    }

    private fun setStart() {
        checkValues()
        setStop()
        val iSocket =
            when (currCheckedId) {
                binding.radioTcpClient.id -> TCPClient(inputIP, inputPort1)
                binding.radioTcpServer.id -> TCPServer(inputPort1)
                binding.radioUdpClient.id -> UDPClient(inputIP, inputPort1, inputPort2, 49163)
                binding.radioUdpServer.id -> UDPServer(inputPort1)
                binding.radioUdpMulticast.id -> UDPMulticast(inputIP, inputPort1)
                else -> return
            }
        socket = Socket(iSocket)
        socket?.setOnSocketStateListener(object : OnSocketStateListener {
            override fun onStarted() {
                printLog("Started")
            }

            override fun onClosed() {
                printLog("Closed")
            }

            override fun onException(e: Exception?) {
                printLog("Exception：${e?.message}")
            }
        })
        socket?.setOnMessageReceivedListener(object : OnMessageReceivedListener {
            override fun onReceived(data: ByteArray?) {
                printLog("收到：${data?.let { String(it) } ?: "data is null!"}")
            }
        })
        socket?.start()
    }

    private fun setStop() {
        socket?.closeAndQuit()
    }

    private fun setSend() {
        inputMessage = binding.textMessage.text.toString()
        socket?.write(inputMessage.toByteArray())
        printLog("发送：${inputMessage}")
    }

    private fun checkValues(): Boolean {
        inputIP = binding.editIp.text.toString()
        try {
            inputPort1 = binding.editPort1.text.toString().toInt()
            inputPort2 = binding.editPort2.text.toString().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
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