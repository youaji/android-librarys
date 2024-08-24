package com.youaji.libs.socket.tcp

import com.youaji.libs.socket.ISocket
import com.youaji.libs.socket.OnMessageReceivedListener
import com.youaji.libs.socket.OnSocketStateListener
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logWarn
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * @param host TCP服务端的主机地址
 * @param port TCP服务端的端口
 */
class TCPClient @JvmOverloads constructor(
    private val host: String,
    private val port: Int,
) : ISocket<Socket?> {

    private val syncLock = Any()

    private var socketStateListener: OnSocketStateListener? = null
    private var messageReceivedListener: OnMessageReceivedListener? = null

    @Volatile
    private var isStarted: Boolean = false
    private var threadExecutor: Executor? = null
    private var socket: Socket? = null

    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

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

    override fun isStart(): Boolean = isStarted && socket?.isClosed != true
    override fun isConnected(): Boolean = socket?.isConnected == true
    override fun isClosed(): Boolean = socket?.isClosed == true
    override fun getSocket(): Socket? = socket

    override fun createSocket(): Socket {
        val socket = Socket()
        socket.keepAlive = true
        socket.reuseAddress = true
        return socket
    }

    override fun start() {
        if (isStart()) return
        if (isAppDebug) logDebug("tcp client start...")
        isStarted = true
        obtainExecutor()?.execute {
            try {
                socket = createSocket()
                socket?.connect(InetSocketAddress(host, port), 10000)
                if (isAppDebug) logDebug("tcp client local address[${socket?.localAddress?.hostAddress ?: "unknown address"}:${socket?.localPort}]")
                if (isAppDebug) logDebug("tcp client Connect to[$host:$port]")
                socketStateListener?.onStarted()
                inputStream = socket?.getInputStream()
                outputStream = socket?.getOutputStream()
                while (isStart() && socket?.isInputShutdown != true) {
                    val len: Int = inputStream?.available() ?: 0
                    if (len > 0) {
                        val data = ByteArray(len)
                        val ret: Int = inputStream?.read(data) ?: 0
                        if (ret != -1) {
                            if (isAppDebug) logDebug("tcp client received data:${String(data)}")
                            messageReceivedListener?.onReceived(data)
                        }
                    }
                }
                inputStream?.close()
                outputStream?.close()
                close()
                if (isAppDebug) logDebug("tcp client close...")
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
                socket?.close()
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn(e)
            socketStateListener?.onException(e)
        }
    }

    override fun write(data: ByteArray?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("tcp client has not started")
            return
        }
        data?.let {
            if (socket?.isOutputShutdown != true) {
                try {
                    outputStream?.write(it)
                    outputStream?.flush()
                    if (isAppDebug) logDebug("tcp client write:" + String(it))
                } catch (e: Exception) {
                    if (isAppDebug) logWarn("tcp client write exception:$e")
                    socketStateListener?.onException(e)
                }
            }
        } ?: if (isAppDebug) logDebug("tcp client write:error! data is NULL") else Unit
    }

    override fun write(data: DatagramPacket?) {
        data?.let {
            val value = ByteArray(it.length - it.offset)
            System.arraycopy(it.data, it.offset, value, 0, value.size)
            write(value)
        } ?: if (isAppDebug) logDebug("tcp client write:error! data is NULL") else Unit
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