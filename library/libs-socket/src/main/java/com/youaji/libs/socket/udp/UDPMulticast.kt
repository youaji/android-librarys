package com.youaji.libs.socket.udp

import com.youaji.libs.socket.ISocket
import com.youaji.libs.socket.OnMessageReceivedListener
import com.youaji.libs.socket.OnSocketStateListener
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logWarn
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.SocketException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * 构造
 * @param host       组播地址
 * @param port       组播端口
 * @param dataLength 接收数据包的长度，超出会造成拆分成多条数据包
 */
class UDPMulticast @JvmOverloads constructor(
    private val host: String,
    private var port: Int,
    private val dataLength: Int = 1460,
) : ISocket<MulticastSocket?> {

    private val syncLock = Any()

    private var socketStateListener: OnSocketStateListener? = null
    private var messageReceivedListener: OnMessageReceivedListener? = null

    @Volatile
    private var isStarted: Boolean = false
    private var threadExecutor: Executor? = null
    private var multicastSocket: MulticastSocket? = null
    private var inetAddress: InetAddress = InetAddress.getByName(host)

    private fun obtainExecutor(): Executor? {
        if (threadExecutor == null) {
            synchronized(syncLock) {
                if (threadExecutor == null) {
                    threadExecutor = Executors.newSingleThreadExecutor()
                }
            }
        }
        return threadExecutor
    }

    override fun isStart(): Boolean = isStarted && multicastSocket?.isClosed != true
    override fun isConnected(): Boolean = multicastSocket?.isConnected == true
    override fun isClosed(): Boolean = multicastSocket?.isClosed == true
    override fun getSocket(): MulticastSocket? = multicastSocket

    override fun createSocket(): MulticastSocket {
        val socket = MulticastSocket(port)
        socket.reuseAddress = true
        return socket
    }

    override fun start() {
        if (isStart()) return
        if (isAppDebug) logDebug("udp multicast start...")
        isStarted = true
        obtainExecutor()?.execute {
            try {
                multicastSocket = createSocket()
                inetAddress = InetAddress.getByName(host)
                multicastSocket?.localPort?.let { port = it }
                if (isAppDebug) logDebug("udp multicast local address[${multicastSocket?.localAddress?.hostAddress ?: "unknown address"}:$port]")
                multicastSocket?.joinGroup(inetAddress)
                if (isAppDebug) logDebug("udp multicast join group:[$host:$port]")
                socketStateListener?.onStarted()
                while (isStart()) {
                    val data = DatagramPacket(ByteArray(dataLength), dataLength)
                    try {
                        multicastSocket?.receive(data)
                    } catch (e: SocketException) {
                        if (isClosed()) break
                    }
                    val value = ByteArray(data.length - data.offset)
                    System.arraycopy(data.data, data.offset, value, 0, value.size)
                    if (isAppDebug) logDebug(
                        "udp multicast received" +
                                "\nfrom:${data.address}:${data.port}" +
                                "\ndata:${String(value)}"
                    )
                    messageReceivedListener?.onReceived(value)
                }
                if (!isClosed()) {
                    multicastSocket?.leaveGroup(InetAddress.getByName(host))
                    if (isAppDebug) logDebug("udp multicast leave group[$host:$port]")
                }
                close()
                if (isAppDebug) logDebug("udp multicast close...")
                socketStateListener?.onClosed()
            } catch (e: Exception) {
                isStarted = false
                if (isAppDebug) logWarn(e)
                socketStateListener?.onException(e)
            }
        }
    }

    override fun close() {
        try {
            isStarted = false
            if (!isClosed()) {
                multicastSocket?.close()
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn(e)
            socketStateListener?.onException(e)
        }
    }

    /** 组播消息，默认发送至当前加入的组 */
    override fun write(data: ByteArray?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("udp multicast has not started")
            return
        }
        try {
            data?.let {
                multicastSocket?.send(DatagramPacket(it, 0, it.size, inetAddress, port))
                if (isAppDebug) logDebug("udp multicast write:" + String(it))
            } ?: if (isAppDebug) logDebug("udp multicast write:error! data is NULL") else Unit
        } catch (e: Exception) {
            if (isAppDebug) logWarn("udp multicast write exception:$e")
            socketStateListener?.onException(e)
        }
    }

    override fun write(data: DatagramPacket?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("udp multicast has not started")
            return
        }
        try {
            multicastSocket?.send(data)
            if (isAppDebug) {
                val log = data?.let {
                    val value = ByteArray(it.length - it.offset)
                    System.arraycopy(it.data, it.offset, value, 0, value.size)
                    String(value)
                } ?: "error! data is NULL"
                logDebug("udp multicast write:$log")
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn("udp multicast write exception:$e")
            socketStateListener?.onException(e)
        }
    }

    override fun setExecutor(executor: Executor?) {
        if (isStart()) return
        this.threadExecutor = executor
    }

    override fun setOnSocketStateListener(listener: OnSocketStateListener?) {
        this.socketStateListener = listener
    }

    override fun setOnMessageReceivedListener(listener: OnMessageReceivedListener?) {
        this.messageReceivedListener = listener
    }
}