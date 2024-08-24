package com.youaji.libs.socket.tcp

import com.youaji.libs.socket.ISocket
import com.youaji.libs.socket.OnMessageReceivedListener
import com.youaji.libs.socket.OnSocketStateListener
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logWarn
import java.net.DatagramPacket
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @param port      ServerSocket的端口
 * @param backlog   ServerSocket允许链接队列的最大长度
 */
class TCPServer @JvmOverloads constructor(
    private var port: Int,
    private val backlog: Int = 50,
) : ISocket<ServerSocket?> {

    private val syncLock = Any()

    private var socketStateListener: OnSocketStateListener? = null
    private var messageReceivedListener: OnMessageReceivedListener? = null

    @Volatile
    private var isStarted: Boolean = false
    private var isConnected: Boolean = false
    private var threadExecutor: Executor? = null
    private var serverSocket: ServerSocket? = null

    private val cacheClient = mutableMapOf<String, Socket>()

    private fun obtainExecutor(): Executor? {
        if (threadExecutor == null) {
            synchronized(syncLock) {
                if (threadExecutor == null) {
                    threadExecutor = ThreadPoolExecutor(
                        5,
                        backlog,
                        0,
                        TimeUnit.MILLISECONDS,
                        LinkedBlockingQueue()
                    )
                }
            }
        }
        return threadExecutor
    }

    private fun processSocketTask(executor: Executor?, socket: Socket?) {
        executor?.execute { processSocket(socket) }
    }

    private fun processSocket(socket: Socket?) {
        try {
            if (socket == null) return
            val host = socket.inetAddress?.hostAddress ?: return
            val port = socket.port
            val key = "$host:$port"
            cacheClient[key] = socket
            val inputStream = socket.getInputStream()
            var len: Int
            while (isStart() && !socket.isInputShutdown) {
                len = inputStream.available()
                if (len > 0) {
                    val data = ByteArray(len)
                    val ret = inputStream?.read(data)
                    if (ret != -1) {
                        if (isAppDebug) logDebug("tcp server received data:${String(data)}")
                        messageReceivedListener?.onReceived(data)
                    }
                }
            }
            cacheClient.remove(key)
            inputStream?.close()
            socket.close()
        } catch (e: Exception) {
            if (isAppDebug) logWarn(e)
            socketStateListener?.onException(e)
        }
    }

    override fun isStart(): Boolean = isStarted && serverSocket?.isClosed != true
    override fun isConnected(): Boolean = isConnected && serverSocket?.isBound == true
    override fun isClosed(): Boolean = serverSocket?.isClosed == true
    override fun getSocket(): ServerSocket? = serverSocket
    override fun createSocket(): ServerSocket {
        val serverSocket = ServerSocket(port, backlog)
        serverSocket.reuseAddress = true
        return serverSocket
    }

    override fun start() {
        if (isStart()) return
        if (isAppDebug) logDebug("tcp server start...")
        isStarted = true
        obtainExecutor()?.execute {
            try {
                serverSocket = createSocket()
                serverSocket?.localPort?.let { port = it }
                isConnected = true
                if (isAppDebug) logDebug("tcp server local address[${serverSocket?.inetAddress?.hostAddress ?: "unknown address"}:${serverSocket?.localPort}]")
                if (cacheClient.isNotEmpty()) {
                    cacheClient.clear()
                }
                socketStateListener?.onStarted()
                while (isStart()) {
                    try {
                        val socket = serverSocket?.accept()
                        processSocketTask(threadExecutor, socket)
                    } catch (e: SocketException) {
                        e.printStackTrace()
                        if (isClosed()) break
                    }
                }
                close()
                if (isAppDebug) logDebug("tcp server close...")
                socketStateListener?.onClosed()
            } catch (e: Exception) {
                isConnected = false
                isStarted = false
                if (isAppDebug) logWarn(e)
                socketStateListener?.onException(e)
            }
        }
    }

    override fun close() {
        try {
            cacheClient.clear()
            isConnected = false
            isStarted = false
            if (!isClosed()) {
                serverSocket?.close()
            }
        } catch (e: Exception) {
            if (isAppDebug) logWarn(e)
            socketStateListener?.onException(e)
        }
    }

    /**
     * TCPServer 当前表示群发
     * @param data
     */
    override fun write(data: ByteArray?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("tcp server has not started")
            return
        }
        for (socket in cacheClient.values) {
            if (!socket.isOutputShutdown) {
                try {
                    data?.let {
                        val outputStream = socket.getOutputStream()
                        outputStream.write(it)
                        outputStream.flush()
                        if (isAppDebug) logDebug("tcp server write:${String(it)}")
                    } ?: if (isAppDebug) logDebug("tcp server write:error! data is NULL") else Unit
                } catch (e: Exception) {
                    if (isAppDebug) logWarn("tcp server write exception:$e")
                    socketStateListener?.onException(e)
                }
            }
        }
    }

    /**
     * TCPServer 当前表示发送到指定 Address，如果不指定，将使用群发
     * @param data
     */
    override fun write(data: DatagramPacket?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("tcp server has not started")
            return
        }
        data?.let {
            val value = ByteArray(it.length - it.offset)
            it.address?.let { address ->
                val host = address.hostAddress
                val port = it.port
                val key = "$host:$port"
                val socket = cacheClient[key]
                if (socket != null) {
                    System.arraycopy(it.data, it.offset, value, 0, value.size)
                    if (!socket.isOutputShutdown) {
                        try {
                            val outputStream = socket.getOutputStream()
                            outputStream.write(value)
                            outputStream.flush()
                            if (isAppDebug) logDebug("tcp server write:${String(value)}")
                        } catch (e: Exception) {
                            if (isAppDebug) logWarn("tcp server write exception:$e")
                            socketStateListener?.onException(e)
                        }
                    }
                    return
                }
                write(value)
            } ?: if (isAppDebug) logDebug("tcp server write:data address is NULL") else Unit
        } ?: if (isAppDebug) logDebug("tcp server write:data is NULL") else Unit
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