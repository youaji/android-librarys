package com.youaji.libs.tcp.server.impl.client

import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.server.action.ClientActionDispatcher
import com.youaji.libs.tcp.server.impl.ServerOption
import java.net.InetAddress
import java.net.Socket

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsClient(
    protected val socket: Socket,
    protected var serverOption: ServerOption,
) : IClient, ClientActionDispatcher.ClientActionListener {

    @Volatile
    private var isCallDead = false

    @Volatile
    private var isCallReady = false

    private val cacheForNotPrepare = listOf<OriginalData>()
    private val _uniqueTag: String
        get() = hostIp + "-" + System.currentTimeMillis() + "-" + System.nanoTime() + "-" + socket.port

    protected val readerProtocol: IReaderProtocol = serverOption.readerProtocol
    protected val inetAddress: InetAddress = socket.inetAddress

    protected abstract fun onClientReady()
    protected abstract fun onClientDead(e: Exception?)

    //<editor-fold desc="IClient override">
    override val hostIp: String
        get() = inetAddress.hostAddress ?: ""
    override val hostName: String
        get() = inetAddress.canonicalHostName
    override val uniqueTag: String
        get() = _uniqueTag
    //</editor-fold>

    //<editor-fold desc="ClientActionListener override">
    override fun onClientReadReady() {
        synchronized(this) {
            if (!isCallReady) {
                onClientReady()
                isCallDead = false
                isCallReady = true
            }
        }
    }

    override fun onClientWriteReady() {
        synchronized(this) {
            if (!isCallReady) {
                onClientReady()
                isCallDead = false
                isCallReady = true
            }
        }
    }

    override fun onClientReadDead(e: Exception?) {
        synchronized(this) {
            if (!isCallDead) {
                onClientDead(e)
                isCallDead = true
                isCallReady = false
            }
        }
    }

    override fun onClientWriteDead(e: Exception?) {
        synchronized(this) {
            if (!isCallDead) {
                onClientDead(e)
                isCallDead = true
                isCallReady = false
            }
        }
    }
    //</editor-fold>
}