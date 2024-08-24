package com.youaji.libs.socket

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.logDebug
import com.youaji.libs.util.logger.logWarn
import java.lang.Integer.toHexString
import java.net.DatagramPacket
import java.util.Locale
import java.util.concurrent.Executor

class Socket<SOCKET>(
    private val iSocket: ISocket<SOCKET>
) : ISocket<SOCKET> {

    companion object {
        private const val WHAT_SEND_MESSAGE = 0x11
        private const val WHAT_RECEIVE_MESSAGE = 0x12
        private const val WHAT_STATE_STARTED = 0x21
        private const val WHAT_STATE_CLOSED = 0x22
        private const val WHAT_STATE_EXCEPTION = 0xFF
        private const val BYTE_DATA_MESSAGE = 0x01
        private const val DATAGRAM_PACKET_MESSAGE = 0x02
    }

    private val mainHandler: Handler
    private val workHandler: Handler
    private val handlerThread = HandlerThread("SocketMessageThread")
    private var socketStateListener: OnSocketStateListener? = null
    private var messageReceivedListener: OnMessageReceivedListener? = null

    init {
        handlerThread.start()
        workHandler = object : Handler(handlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    WHAT_SEND_MESSAGE -> if (msg.arg1 == BYTE_DATA_MESSAGE) {
                        iSocket.write(msg.obj as ByteArray)
                    } else if (msg.arg1 == DATAGRAM_PACKET_MESSAGE) {
                        iSocket.write(msg.obj as DatagramPacket)
                    }
                }
            }
        }

        mainHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (isAppDebug) logDebug("MainHandler handle message: [0x${toHexString(msg.what).uppercase(Locale.getDefault())}]")
                when (msg.what) {
                    WHAT_RECEIVE_MESSAGE -> messageReceivedListener?.onReceived(msg.obj as ByteArray)
                    WHAT_STATE_STARTED -> socketStateListener?.onStarted()
                    WHAT_STATE_CLOSED -> socketStateListener?.onClosed()
                    WHAT_STATE_EXCEPTION -> socketStateListener?.onException(msg.obj as java.lang.Exception)
                }
            }
        }
    }

    /**
     * 关闭并退出，与 [.close]类似，相对于[.close] 的区别是会多一步退出线程消息队列操作；退出消息队列后将无法在发送消息。
     * 此方法一般用于在明确不再使用时调用。
     */
    fun closeAndQuit() {
        close()
        handlerThread.quit()
    }

    override fun isStart(): Boolean = iSocket.isStart()
    override fun isConnected(): Boolean = iSocket.isConnected()
    override fun isClosed(): Boolean = iSocket.isClosed()
    override fun getSocket(): SOCKET? = iSocket.getSocket()

    override fun createSocket(): SOCKET {
        return iSocket.createSocket()
    }

    override fun start() {
        if (isStart()) return
        if (socketStateListener == null) {
            setOnSocketStateListener(null)
        }
        iSocket.start()
    }

    override fun close() {
        iSocket.close()
    }

    override fun write(data: ByteArray?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("Client has not started")
            return
        }
        workHandler.obtainMessage(WHAT_SEND_MESSAGE, BYTE_DATA_MESSAGE, 0, data).sendToTarget()
    }

    override fun write(data: DatagramPacket?) {
        if (!isStart()) {
            if (isAppDebug) logWarn("Client has not started")
            return
        }
        workHandler.obtainMessage(WHAT_SEND_MESSAGE, DATAGRAM_PACKET_MESSAGE, 0, data).sendToTarget()
    }

    override fun setExecutor(executor: Executor?) {
        iSocket.setExecutor(executor)
    }

    override fun setOnSocketStateListener(listener: OnSocketStateListener?) {
        socketStateListener = listener
        iSocket.setOnSocketStateListener(object : OnSocketStateListener {
            override fun onStarted() {
                mainHandler.sendEmptyMessage(WHAT_STATE_STARTED)
            }

            override fun onClosed() {
                mainHandler.sendEmptyMessage(WHAT_STATE_CLOSED)
            }

            override fun onException(e: Exception?) {
                mainHandler.obtainMessage(WHAT_STATE_EXCEPTION, e).sendToTarget()
            }
        })
    }

    override fun setOnMessageReceivedListener(listener: OnMessageReceivedListener?) {
        messageReceivedListener = listener
        iSocket.setOnMessageReceivedListener(object : OnMessageReceivedListener {
            override fun onReceived(data: ByteArray?) {
                mainHandler.obtainMessage(WHAT_RECEIVE_MESSAGE, data).sendToTarget()
            }
        })
    }
}