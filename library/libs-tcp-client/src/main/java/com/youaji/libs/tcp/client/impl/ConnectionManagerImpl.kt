package com.youaji.libs.tcp.client.impl

import com.youaji.libs.tcp.client.impl.action.ActionHandler
import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.impl.exception.UnConnectException
import com.youaji.libs.tcp.client.impl.thread.IOThreadManager
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.action.IAction
import com.youaji.libs.tcp.client.sdk.connection.AbsReconnectionManager
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.common.IIOManager
import com.youaji.libs.tcp.interfaces.default.DefaultX509ProtocolTrustManager
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.security.SecureRandom
import javax.net.ssl.SSLContext

/**
 * @author youaji
 * @since 2024/01/11
 */
class ConnectionManagerImpl(
    remoteInfo: ConnectionInfo,
    localInfo: ConnectionInfo? = null,
) : AbsConnectionManager(remoteInfo, localInfo) {


    /** IO通讯管理器 */
    private var ioManager: IIOManager<ClientOption>? = null

    /** 连接线程 */
    private var connectThread: Thread? = null

    /** Socket 行为监听器 */
    private var actionHandler: ActionHandler? = null

    /** 套接字 */
    @Volatile
    private var _socket: Socket? = null

    /** socket参配项 */
    @Volatile
    private lateinit var _socketOption: ClientOption

    /** 脉搏管理器  */
    @Volatile
    private var _pulseManager: PulseManager? = null

    /** 重新连接管理器 */
    @Volatile
    private var _reconnectionManager: AbsReconnectionManager? = null

    /** 能否连接 */
    @Volatile
    private var _isConnectionPermitted = true

    /** 是否正在断开 */
    @Volatile
    private var _isDisconnecting = false

    init {
        SLog.i("block connection init with:${remoteInfo.ip}:${remoteInfo.port}")
        if (localInfo != null) {
            SLog.i("binding local addr:" + localInfo.ip + " port:" + localInfo.port)
        }
    }

    //<editor-fold desc="IConnectable override">
    @Synchronized
    override fun connect() {
        SLog.i("Thread name:" + Thread.currentThread().name + " id:" + Thread.currentThread().id)
        if (!_isConnectionPermitted) return
        _isConnectionPermitted = false

        if (isConnect) return
        _isDisconnecting = false

        if (actionHandler != null) {
            actionHandler?.detach(this)
            SLog.i("actionHandler is detached.")
        }
        actionHandler = ActionHandler()
        actionHandler?.attach(this, this)
        SLog.i("actionHandler is attached.")

        if (_reconnectionManager != null) {
            _reconnectionManager?.detach()
            SLog.i("ReconnectionManager is detached.")
        }
        _reconnectionManager = _socketOption.reconnectionManager
        if (_reconnectionManager != null) {
            _reconnectionManager?.attach(this)
            SLog.i("ReconnectionManager is attached.")
        }

        val info: String = remoteInfo.ip + ":" + remoteInfo.port
        connectThread = ConnectionThread("ConnectThread for $info")
        connectThread?.isDaemon = true
        connectThread?.start()
    }
    //</editor-fold>

    @get:Throws(Exception::class)
    @get:Synchronized
    private val createSocketByConfig: Socket
        get() {
            // 自定义socket操作
            _socketOption.socketFactory?.let { factory ->
                _socketOption.let { option ->
                    return factory.createSocket(remoteInfo, option)
                }
            }

            // 默认操作
            val config = _socketOption.sslConfig ?: return Socket()
            val factory = config.customSSLFactory
            return if (factory == null) {
                var protocol = "SSL"
                config.protocol?.let { p ->
                    if (p.isNotEmpty()) protocol = p
                }
                var trustManagers = config.trustManagers
                if (trustManagers.isNullOrEmpty()) {
                    // 缺省信任所有证书
                    trustManagers = arrayOf(DefaultX509ProtocolTrustManager())
                }
                try {
                    val sslContext = SSLContext.getInstance(protocol)
                    sslContext.init(config.keyManagers, trustManagers, SecureRandom())
                    sslContext.socketFactory.createSocket()
                } catch (e: Exception) {
                    if (_socketOption.isDebug) {
                        e.printStackTrace()
                        SLog.e(e.message ?: "")
                    }
                    Socket()
                }
            } else {
                try {
                    factory.createSocket()
                } catch (e: IOException) {
                    if (_socketOption.isDebug) {
                        e.printStackTrace()
                        SLog.e(e.message ?: "")
                    }
                    Socket()
                }
            }
        }

    private inner class ConnectionThread(name: String) : Thread(name) {
        override fun run() {
            try {
                try {
                    _socket = createSocketByConfig
                } catch (e: Exception) {
                    if (_socketOption.isDebug) {
                        e.printStackTrace()
                    }
                    throw UnConnectException("Create socket failed.", e)
                }
                localInfo?.let { local ->
                    SLog.i("try bind: " + local.ip + " port:" + local.port)
                    _socket?.bind(InetSocketAddress(local.ip, local.port))
                }

                SLog.i("Start connect: " + remoteInfo.ip + ":" + remoteInfo.port + " socket server...")

                _socket?.connect(InetSocketAddress(remoteInfo.ip, remoteInfo.port), _socketOption.connectTimeoutSecond * 1000)
                // 关闭 Nagle 算法,无论 TCP 数据报大小,立即发送
                _socket?.tcpNoDelay = true
                resolveManager()
                sendBroadcast(IAction.actionConnectionSuccess)
                SLog.i("Socket server: " + remoteInfo.ip + ":" + remoteInfo.port + " connect successful!")
            } catch (e: Exception) {
                if (_socketOption.isDebug) {
                    e.printStackTrace()
                }
                val exception: Exception = UnConnectException(e)
                SLog.e("Socket server " + remoteInfo.ip + ":" + remoteInfo.port + " connect failed! error msg:" + e.message)
                sendBroadcast(IAction.actionConnectionFailed, exception)
            } finally {
                _isConnectionPermitted = true
            }
        }
    }

    @Throws(IOException::class)
    private fun resolveManager() {
        _pulseManager = PulseManager(this, _socketOption)
        _socket?.let { s ->
            ioManager = IOThreadManager(
                s.getInputStream(),
                s.getOutputStream(),
                _socketOption,
                actionDispatcher
            )
        }
        ioManager?.startEngine()
    }

    //<editor-fold desc="IDisConnectable override">
    override fun disconnect() {
        disconnect(ManuallyDisconnectException())
    }

    override fun disconnect(e: Exception?) {
        synchronized(this) {
            if (isDisconnecting) return
            _isDisconnecting = true
            _pulseManager?.dead()
            _pulseManager = null
        }
        if (e is ManuallyDisconnectException) {
            if (_reconnectionManager != null) {
                _reconnectionManager?.detach()
                SLog.i("ReconnectionManager is detached.")
            }
        }
        val info: String = remoteInfo.ip + ":" + remoteInfo.port
        val thread = DisconnectThread(e, "DisconnectThread for $info")
        thread.isDaemon = true
        thread.start()
    }
    //</editor-fold>

    private inner class DisconnectThread(private var exception: Exception?, name: String) : Thread(name) {
        override fun run() {
            try {
                ioManager?.close(exception)
                if (connectThread?.isAlive == true) {
                    connectThread?.interrupt()
                    try {
                        SLog.i("disconnect thread need waiting for connection thread done.")
                        connectThread?.join()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    SLog.i("connection thread is done. disconnection thread going on")
                    connectThread = null
                }

                if (_socket != null) {
                    try {
                        _socket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                if (actionHandler != null) {
                    actionHandler?.detach(this@ConnectionManagerImpl)
                    SLog.i("actionHandler is detached.")
                    actionHandler = null
                }
            } finally {
                _isDisconnecting = false
                _isConnectionPermitted = true
                if (exception !is UnConnectException && _socket != null) {
                    exception = if (exception is ManuallyDisconnectException) null else exception
                    sendBroadcast(IAction.actionDisconnection, exception)
                }
                _socket = null
                if (exception != null) {
                    if (_socketOption.isDebug) {
                        SLog.e("socket is disconnecting because: ${exception?.message}")
                        exception?.printStackTrace()
                    }
                }
            }
        }
    }

    //<editor-fold desc="ISender override">
    override fun send(sendable: ISendable): IConnectionManager {
        if (ioManager != null && isConnect) {
            ioManager?.send(sendable)
        }
        return this
    }

    //</editor-fold>

    //<editor-fold desc="IConfiguration override">
    override val option: ClientOption
        get() = _socketOption

    override fun option(option: ClientOption): IConnectionManager {
        _socketOption = option
        ioManager?.setOption(_socketOption)
        _pulseManager?.setOption(_socketOption)
        if (_reconnectionManager != _socketOption.reconnectionManager) {
            _reconnectionManager?.detach()
            SLog.i("reconnection manager is replaced")
            _reconnectionManager = _socketOption.reconnectionManager
            _reconnectionManager?.attach(this)
        }
        return this
    }
    //</editor-fold>


    //<editor-fold desc="IConnectionManager override">
    override val isConnect: Boolean
        get() = _socket?.let { it.isConnected && !it.isClosed } ?: false

    override val isDisconnecting: Boolean
        get() = _isDisconnecting

    override val pulseManager: PulseManager?
        get() = _pulseManager

    override val reconnectionManager: AbsReconnectionManager
        get() = _socketOption.reconnectionManager

    override fun setLocalConnectionInfo(info: ConnectionInfo?) {
        check(!isConnect) { "Socket is connected, can't set local info after connect." }
        localInfo = info
    }

    override fun getLocalConnectionInfo(): ConnectionInfo? {
        var local: ConnectionInfo? = super.getLocalConnectionInfo()
        if (local == null) {
            if (isConnect) {
                val address = _socket?.localSocketAddress as InetSocketAddress
                local = ConnectionInfo(address.hostName, address.port)
            }
        }
        return local
    }

    override fun setConnectionHolder(isHolder: Boolean) {
        _socketOption = ClientOption.Builder(_socketOption).setConnectionHolden(isHolder).build()
    }
    //</editor-fold>
}