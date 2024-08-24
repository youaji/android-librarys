package com.youaji.libs.tcp.server.impl.client

import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.interfaces.common.server.IClientIOCallback
import com.youaji.libs.tcp.server.action.ClientActionDispatcher
import com.youaji.libs.tcp.server.action.IAction
import com.youaji.libs.tcp.server.exception.CacheException
import com.youaji.libs.tcp.server.impl.ServerOption
import com.youaji.libs.tcp.server.impl.io.ClientIOManager
import java.io.IOException
import java.net.Socket

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientImpl(socket: Socket, option: ServerOption) : AbsClient(socket, option) {

    @Volatile
    private var isDead = false

    @Volatile
    private var isReadThreadStarted = false

    @Volatile
    private var clientPool: ClientPoolImpl? = null

    @Volatile
    private var callbackList = mutableListOf<IClientIOCallback>()

    private var ioManager: ClientIOManager? = null
    private val actionDispatcher: IStateSender = ClientActionDispatcher(this)
    private var serverStateSender: IStateSender? = null

    init {
        try {
            initIOManager()
        } catch (e: IOException) {
            disconnect(e)
        }
    }

    fun setClientPool(pool: ClientPoolImpl?) {
        this.clientPool = pool
    }

    fun setServerStateSender(sender: IStateSender?) {
        this.serverStateSender = sender
    }

    @Throws(IOException::class)
    private fun initIOManager() {
        val inputStream = socket.getInputStream()
        val outputStream = socket.getOutputStream()
        ioManager = ClientIOManager(inputStream, outputStream, serverOption, actionDispatcher)
    }

    fun startIOEngine() {
        ioManager?.let { ioManager ->
            synchronized(ioManager) { ioManager.startWriteEngine() }
        }
    }

    //<editor-fold desc="IDisConnectable override">
    override fun disconnect() {
        ioManager?.let { ioManager ->
            synchronized(ioManager) { ioManager.close() }
        } ?: onClientDead(null)

        try {
            synchronized(socket) { socket.close() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        removeIOCallbacks()
        isReadThreadStarted = false
    }

    override fun disconnect(e: Exception?) {
        ioManager?.let { ioManager ->
            synchronized(ioManager) { ioManager.close(e) }
        } ?: onClientDead(e)

        try {
            synchronized(socket) { socket.close() }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        removeIOCallbacks()
        isReadThreadStarted = false
    }
    //</editor-fold>

    //<editor-fold desc="ISender override">
    override fun send(sendable: ISendable): AbsClient {
        ioManager?.send(sendable)
        return this
    }
    //</editor-fold>

    //<editor-fold desc="IClient override">
    override fun setReaderProtocol(protocol: IReaderProtocol) {
        ioManager?.let { ioManager ->
            synchronized(ioManager) {
                val builder = ServerOption.Builder(serverOption)
                builder.setReaderProtocol(protocol)
                serverOption = builder.build()
                ioManager.setOption(serverOption)
            }
        }
    }

    override fun addIOCallback(callback: IClientIOCallback) {
        if (isDead) return
        synchronized(callbackList) { callbackList.add(callback) }
        ioManager?.let { ioManager ->
            synchronized(ioManager) {
                if (!isReadThreadStarted) {
                    isReadThreadStarted = true
                    ioManager.startReadEngine()
                }
            }
        }
    }

    override fun removeIOCallback(callback: IClientIOCallback) {
        synchronized(callbackList) { callbackList.remove(callback) }
    }

    override fun removeIOCallbacks() {
        synchronized(callbackList) { callbackList.clear() }
    }
    //</editor-fold>

    //<editor-fold desc="ClientActionListener override">
    override fun onClientRead(originalData: OriginalData?) {
        val list = mutableListOf<IClientIOCallback>()
        list.addAll(callbackList)
        for (clientIOCallback in list) {
            try {
                clientPool?.let { pool -> clientIOCallback.onClientRead(originalData, this, pool) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onClientWrite(sendable: ISendable?) {
        val list = mutableListOf<IClientIOCallback>()
        list.addAll(callbackList)
        for (clientIOCallback in list) {
            try {
                clientPool?.let { pool -> clientIOCallback.onClientWrite(sendable, this, pool) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="AbsClient override">
    override fun onClientReady() {
        if (isDead) return
        clientPool?.cache(this)
        serverStateSender?.sendBroadcast(IAction.Server.actionClientConnected, this)
    }

    override fun onClientDead(e: Exception?) {
        if (isDead) return
        if (e !is CacheException) {
            clientPool?.unCache(this)
        }
        if (e != null && serverOption.isDebug) {
            e.printStackTrace()
        }
        disconnect(e)
        serverStateSender?.sendBroadcast(IAction.Server.actionClientDisconnected, this)
        synchronized(this) { isDead = true }
    }
    //</editor-fold>
}