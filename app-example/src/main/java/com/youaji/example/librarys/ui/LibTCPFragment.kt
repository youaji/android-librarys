package com.youaji.example.librarys.ui

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.youaji.example.librarys.databinding.FragmentLibTcpBinding
import com.youaji.example.librarys.repo.ConfigRepo
import com.youaji.libs.tcp.client.impl.action.ActionDispatcher
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.YouSocket
import com.youaji.libs.tcp.client.sdk.action.SocketActionAdapter
import com.youaji.libs.tcp.client.sdk.connection.DefaultReconnectManager
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.client.sdk.connection.NoneReconnect
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.interfaces.common.server.IClientIOCallback
import com.youaji.libs.tcp.interfaces.common.server.IClientPool
import com.youaji.libs.tcp.interfaces.common.server.IServerManager
import com.youaji.libs.tcp.interfaces.common.server.IServerShutdown
import com.youaji.libs.tcp.server.action.ServerActionAdapter
import com.youaji.libs.tcp.server.impl.ServerOption
import com.youaji.libs.ui.basic.BasicBindingFragment
import com.youaji.libs.util.toast
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class LibTCPFragment : BasicBindingFragment<FragmentLibTcpBinding>() {

    private var inputIP = ""
    private var inputIPNew = ""
    private var inputPort = 0
    private var inputPortNew = 0
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val serverManager: IServerManager<ServerOption> by lazy {
        YouSocket.server<ServerOption>(8080).registerReceiver(serverAdapter)
    }
    private val ioCallback = object : IClientIOCallback {
        override fun onClientRead(data: OriginalData?, client: IClient?, pool: IClientPool<IClient?, String>) {
            if (client == null) {
                printLog("收到客户端消息,但是IClient对象为NULL")
                return
            }
            if (data == null) {
                printLog("收到客户端消息,但是OriginalData对象为NULL")
                return
            }

            val head = String(data.headBytes, Charset.forName("UTF-8"))
            val body = String(data.bodyBytes, Charset.forName("UTF-8"))
            printLog("服务接收[${client.hostIp}]：head:$head body:$body")
        }

        override fun onClientWrite(sendable: ISendable?, client: IClient?, pool: IClientPool<IClient?, String>) {
            if (client == null) {
                printLog("发给客户端消息,但是IClient对象为NULL")
                return
            }
            if (sendable == null) {
                printLog("发给客户端消息,但是ISendable对象为NULL")
                return
            }
            printLog("服务发送[${client.hostIp}]：${String(sendable.parse(), Charset.forName("UTF-8"))}")
        }

    }
    private val serverAdapter = object : ServerActionAdapter() {
        override fun onServerListening(serverPort: Int) {
            printLog("服务[${Thread.currentThread().name}]：接收中[$serverPort]...")
        }

        override fun onClientConnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?) {
            printLog(
                "服务[${Thread.currentThread().name}]：已连接[${client?.hostIp}]" +
                        "\nhostName[${client?.hostName}]" +
                        "\nuniqueTag[${client?.uniqueTag}]" +
                        "\n服务端口[$serverPort] -- 已连接客户端数[${clientPool?.size() ?: 0}]"
            )
            client?.addIOCallback(ioCallback)
        }

        override fun onClientDisconnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?) {
            printLog(
                "服务[${Thread.currentThread().name}]：已断开[${client?.hostIp}]" +
                        "\nhostName[${client?.hostName}]" +
                        "\nuniqueTag[${client?.uniqueTag}]" +
                        "\n服务端口[$serverPort] -- 已连接客户端数[${clientPool?.size() ?: 0}]"
            )
            client?.removeIOCallback(ioCallback)
        }

        override fun onServerWillBeShutdown(serverPort: Int, shutdown: IServerShutdown?, clientPool: IClientPool<IClient, String>?, throwable: Throwable?) {
            printLog(
                "服务[${Thread.currentThread().name}]：将被关闭" +
                        "\n服务端口[$serverPort] -- 已连接客户端数[${clientPool?.size() ?: 0}]" +
                        "\nmessage[${throwable?.message}}]"
            )
            shutdown?.shutdown()
        }

        override fun onServerAlreadyShutdown(serverPort: Int) {
            printLog("服务[${Thread.currentThread().name}]：已关闭[$serverPort]...")
        }
    }

    private var ioThreadMode: ClientOption.IOThreadMode = ClientOption.IOThreadMode.SIMPLEX
    private var clientManager: IConnectionManager? = null
    private val clientAdapter = object : SocketActionAdapter() {
        override fun onSocketIOThreadStart(action: String) {
            printLog("通讯线程启动 action:$action")
            refreshButtonText()
        }

        override fun onSocketIOThreadShutdown(action: String, e: Exception?) {
            printLog("通讯线程关闭 action:$action exception:${e?.message}")
            refreshButtonText()
        }

        override fun onSocketConnectionSuccess(info: ConnectionInfo, action: String) {
            printLog("连接成功 ip:${info.ip} port:${info.port} backup:${info.backupInfo}")
            refreshButtonText()
        }

        override fun onSocketDisconnection(info: ConnectionInfo, action: String, e: Exception?) {
            when (e) {
                null -> {
                    printLog("正常断开")
                }

                is RedirectException -> {
                    printLog("正在重定向连接...")
                    clientManager?.switchConnectionInfo(e.redirectInfo)
                    clientManager?.connect()
                }

                else -> {
                    printLog("异常断开：${e.message}")
                }
            }
            refreshButtonText()
        }

        override fun onSocketConnectionFailed(info: ConnectionInfo, action: String, e: Exception?) {
            printLog("连接失败：${e?.message ?: "unknown"}")
            refreshButtonText()
        }

        override fun onSocketReadResponse(info: ConnectionInfo, action: String, data: OriginalData) {
            val head = String(data.headBytes, Charset.forName("UTF-8"))
            val body = String(data.bodyBytes, Charset.forName("UTF-8"))
            printLog("收到：head:$head body:$body")
            refreshButtonText()
        }

        override fun onSocketWriteResponse(info: ConnectionInfo, action: String, data: ISendable) {
            printLog("发送：${String(data.parse(), Charset.forName("UTF-8"))}")
            refreshButtonText()
        }

        override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {
            printLog("发送心跳：${String(data.parse(), Charset.forName("UTF-8"))}")
            refreshButtonText()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.editIp.setText(ConfigRepo.lastTcpClientConfig.ip)
        binding.editPort.setText(ConfigRepo.lastTcpClientConfig.port)
        binding.groupThreadMode.check(binding.radioDuplex.id)
        binding.switchReconnect.isChecked = false
        binding.textMessage.setText("from client message")

        binding.btnConnect.setOnClickListener { connectServer() }
        binding.btnDisconnect.setOnClickListener { disconnectServer() }
        binding.btnClearLog.setOnClickListener { clearLog() }
        binding.btnRedirect.setOnClickListener { redirect() }
        binding.switchReconnect.setOnCheckedChangeListener { _, isChecked ->
            clientManager?.let { manager ->
                manager.option(
                    ClientOption.Builder(manager.option)
                        .setReconnectionManager(if (isChecked) DefaultReconnectManager() else NoneReconnect())
                        .build()
                )
            }
            printLog("失败重连${if (isChecked) "开启" else "关闭"}")
        }
        binding.btnSetFrequ.setOnClickListener {
            val frequency = binding.editHeartbeatFrequ.text.toString().toLongOrNull() ?: 0L
            if (frequency <= 0L) {
                toast("频率必须大于0")
                return@setOnClickListener
            }
            clientManager?.let { manager ->
                manager.option(
                    ClientOption.Builder(manager.option)
                        .setPulseFrequency(frequency)
                        .build()
                )
            }
            printLog("设置心跳频率：$frequency ms")
        }
        binding.btnStartHeartbeat.setOnClickListener { startPulse() }
        binding.btnManualHeartbeat.setOnClickListener { clientManager?.pulseManager?.trigger() }
        binding.btnSend.setOnClickListener { send2Server() }
        binding.groupThreadMode.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == binding.radioSimplex.id) {
                ioThreadMode = ClientOption.IOThreadMode.SIMPLEX
                printLog("当前通讯模式：单工")
            } else if (checkedId == binding.radioDuplex.id) {
                ioThreadMode = ClientOption.IOThreadMode.DUPLEX
                printLog("当前通讯模式：双工")
            }
        }

        binding.switchServer.setOnCheckedChangeListener { _, isChecked ->
            printLog("服务：${getIPAddress()}:8080")
            if (isChecked) {
                if (serverManager.isLive) {
                    printLog("服务：已开启")
                }
                printLog("服务：${getIPAddress()}:8080 开启中...")
                serverManager.listen()
            } else {
                printLog("服务：${getIPAddress()}:8080 关闭中...")
                serverManager.shutdown()
            }
        }
        refreshButtonText()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        binding.textServerTitle.text = "${getIPAddress()}\n启动8080服务器？"
        binding.switchServer.isEnabled = hasIPAddress
        if (!binding.switchServer.isEnabled) binding.switchServer.isChecked = false
    }

    override fun onDestroy() {
        disconnectServer()
        super.onDestroy()
    }

    private fun refreshButtonText() {
        binding.btnConnect.isEnabled = !isConnect
        binding.btnDisconnect.isEnabled = isConnect
    }

    private val isConnect: Boolean
        get() = clientManager?.isConnect ?: false

    private fun connectServer() {
        if (checkValues()) {
            toast("IP或端口不合格")
            return
        }

        ConfigRepo.lastTcpClientConfig = ConfigRepo.TCPLastClientConfig(inputIP, inputPort.toString())

        printLog("开始连接...")
        val connectionInfo = ConnectionInfo(inputIP, inputPort)
        val clientOption = ClientOption.Builder()
            .setIOThreadMode(ioThreadMode)
            .setReconnectionManager(NoneReconnect())
            .setCallbackThreadModeToken(object : ClientOption.ThreadModeToken() {
                override fun handleCallbackEvent(runnable: ActionDispatcher.ActionRunnable) {
                    handler.post(runnable)
                }
            })
            .build()
        clientOption.setDebug(true)
        clientManager = YouSocket.open(connectionInfo).option(clientOption)
        clientManager?.registerReceiver(clientAdapter)
        clientManager?.connect()
    }

    private fun disconnectServer() {
        clientManager?.disconnect()
//        clientManager?.unRegisterReceiver(clientAdapter)
        clientManager = null
    }

    private fun redirect() {
        if (checkNewValues()) {
            toast("IP或端口不合格")
            return
        }
        send2Server("$inputIPNew:$inputPortNew")
    }

    private fun send2Server(message: String = binding.textMessage.text.toString()) {
        if (message.isEmpty()) {
            toast("消息不能为空")
            return
        }
        clientManager?.send(object : ISendable {
            override fun parse(): ByteArray {
                val body = message.toByteArray(Charset.defaultCharset())
                val byteBuffer = ByteBuffer.allocate(body.size + 4)
                byteBuffer.order(ByteOrder.BIG_ENDIAN)
                byteBuffer.putInt(body.size)
                byteBuffer.put(body)
                return byteBuffer.array()
            }
        })
    }

    private fun startPulse() {
        clientManager?.pulseManager?.setPulseSendable(object : IPulseSendable {
            override fun parse(): ByteArray {
                val body = "pulse from client".toByteArray(Charset.defaultCharset())
                val byteBuffer = ByteBuffer.allocate(body.size + 4)
                byteBuffer.order(ByteOrder.BIG_ENDIAN)
                byteBuffer.putInt(body.size)
                byteBuffer.put(body)
                return byteBuffer.array()
            }
        })?.pulse()
    }

    private fun checkValues(): Boolean {
        try {
            inputIP = binding.editIp.text.toString()
            inputPort = binding.editPort.text.toString().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return false
    }

    private fun checkNewValues(): Boolean {
        try {
            inputIPNew = binding.editIpNew.text.toString()
            inputPortNew = binding.editPortNew.text.toString().toInt()
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }
        return false
    }

    private var loggerText: String = ""

    private fun printLog(log: String) {
        runOnUI {
            loggerText += "$log\n"
            binding.layoutLog.textLog.setText(loggerText)
            binding.layoutLog.textLog.setSelection(loggerText.length)
        }
    }

    private fun clearLog() {
        runOnUI {
            loggerText = ""
            binding.layoutLog.textLog.setText(loggerText)
        }
    }

    private fun runOnUI(action: Runnable) {
        requireActivity().runOnUiThread(action)
    }

    private var hasIPAddress = false

    @Suppress("DEPRECATION")
    private fun getIPAddress(): String {
        hasIPAddress = false
        val info = (requireContext().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo
        if (info?.isConnected == true) {
            if (info.type == ConnectivityManager.TYPE_MOBILE) { // 当前使用2G/3G/4G网络
                try {
                    // Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                    while (networkInterfaces.hasMoreElements()) {
                        val enumIpAddresses = networkInterfaces.nextElement().inetAddresses
                        while (enumIpAddresses.hasMoreElements()) {
                            val inetAddress = enumIpAddresses.nextElement()
                            if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                                return inetAddress.getHostAddress() ?: ""
                            }
                        }
                    }
                } catch (e: SocketException) {
                    e.printStackTrace()
                }
            } else if (info.type == ConnectivityManager.TYPE_WIFI) { // 当前使用无线网络
                val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                val ip = if (ipAddress == 0) "未连接wifi"
                else {
                    hasIPAddress = true
                    (
                            (ipAddress and 0xff).toString() + "."
                                    + (ipAddress shr 8 and 0xff) + "."
                                    + (ipAddress shr 16 and 0xff) + "."
                                    + (ipAddress shr 24 and 0xff)
                            )
                }
                return ip
            }
        } else {
            return "当前无网络连接,请在设置中打开网络"
        }
        return "IP获取失败"
    }

    class RedirectException(val redirectInfo: ConnectionInfo) : RuntimeException()
}