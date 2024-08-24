package com.youaji.libs.socket.udp

import com.youaji.libs.socket.ISocket
import com.youaji.libs.socket.OnMessageReceivedListener
import com.youaji.libs.socket.OnSocketStateListener
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logWarn
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @param port       UDP服务端监听的端口
 * @param dataLength 接收数据包的长度，超出会造成拆分成多条数据包
 */
class UDPServer @JvmOverloads constructor(
    private var port: Int,
    private val dataLength: Int = 1460,
) : ISocket<DatagramSocket?> {

    private val syncLock = Any()

    private var socketStateListener: OnSocketStateListener? = null
    private var messageReceivedListener: OnMessageReceivedListener? = null

    @Volatile
    private var isStarted: Boolean = false
    private var threadExecutor: Executor? = null
    private var datagramSocket: DatagramSocket? = null

    private var targetInetAddress: InetAddress? = null
    private var targetPort = 0

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

    override fun isStart(): Boolean = isStarted && datagramSocket?.isClosed != true
    override fun isConnected(): Boolean = datagramSocket?.isConnected == true
    override fun isClosed(): Boolean = datagramSocket?.isClosed == true
    override fun getSocket(): DatagramSocket? = datagramSocket
    override fun createSocket(): DatagramSocket {
        val socket = DatagramSocket(port)
        socket.reuseAddress = true
        return socket
    }

    override fun start() {
        if (isStart()) return
        if (isAppDebug) logDebug("udp server start...")
        isStarted = true
        obtainExecutor()?.execute {
            try {
                datagramSocket = createSocket()
                datagramSocket?.localPort?.let { port = it }
                if (isAppDebug) logDebug("udp server local address[${datagramSocket?.localAddress?.hostAddress ?: "unknown address"}:$port]")
                socketStateListener?.onStarted()
                while (isStart()) {
                    val data = DatagramPacket(ByteArray(dataLength), dataLength)
                    targetInetAddress = data.address
                    targetPort = data.port
                    try {
                        datagramSocket?.receive(data)
                    } catch (e: SocketException) {
                        e.printStackTrace()
                        if (isClosed()) break
                    }
                    val value = ByteArray(data.length - data.offset)
                    System.arraycopy(data.data, data.offset, value, 0, value.size)
                    if (isAppDebug) logDebug(
                        "udp server received" +
                                "\nfrom:${data.address}:${data.port}" +
                                "\ndata:${String(value)}"
                    )

                    messageReceivedListener?.onReceived(value)
                }
                close()
                if (isAppDebug) logDebug("udp server close...")
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
                datagramSocket?.close()
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn(e)
            socketStateListener?.onException(e)
        }
    }

    override fun write(data: ByteArray?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("udp server has not started")
            return
        }
        try {
            if (targetInetAddress == null) return
            data?.let {
                datagramSocket?.send(DatagramPacket(it, 0, it.size, targetInetAddress, targetPort))
                if (isAppDebug) logDebug("udp server write:" + String(it))
            } ?: if (isAppDebug) logDebug("udp server write:error! data is NULL") else Unit
        } catch (e: Exception) {
            if (isAppDebug) logWarn("udp server write exception:$e")
            socketStateListener?.onException(e)
        }
    }

    override fun write(data: DatagramPacket?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("udp server has not started")
            return
        }
        try {
            datagramSocket?.send(data)
            if (isAppDebug) {
                val log = data?.let {
                    val value = ByteArray(it.length - it.offset)
                    System.arraycopy(it.data, it.offset, value, 0, value.size)
                    String(value)
                } ?: "error! data is NULL"
                if (isAppDebug) logDebug("udp server write:$log")
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn("udp server write exception:$e")
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