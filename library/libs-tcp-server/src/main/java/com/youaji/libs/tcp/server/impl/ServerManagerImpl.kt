package com.youaji.libs.tcp.server.impl

import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.interfaces.common.server.IClientPool
import com.youaji.libs.tcp.interfaces.common.server.IServerManagerPrivate
import com.youaji.libs.tcp.server.action.IAction
import com.youaji.libs.tcp.server.exception.InitiativeDisconnectException
import com.youaji.libs.tcp.server.impl.client.ClientImpl
import com.youaji.libs.tcp.server.impl.client.ClientPoolImpl
import java.io.IOException
import java.net.ServerSocket

/**
 * @author youaji
 * @since 2024/01/11
 */
class ServerManagerImpl : AbsServerRegisterProxy(), IServerManagerPrivate<ServerOption> {

    private var isInit = false
    private var serverPort = -999
    private var serverSocket: ServerSocket? = null
    private var clientPoolImpl: ClientPoolImpl? = null
    private var serverOption: ServerOption = ServerOption.Builder().build()
    private var acceptThread: AbsLoopThread? = null
    override fun initServerPrivate(serverPort: Int) {
        checkCallStack()
        if (!isInit && this.serverPort == -999) {
            init(this)
            this.serverPort = serverPort
            serverActionDispatcher?.setServerPort(this.serverPort)
            isInit = true
            SLog.w("server manager initiation")
        } else {
            SLog.e("duplicate init server manager!")
        }
    }

    private fun checkCallStack() {
        val elementsArray = Thread.currentThread().stackTrace
        var isValid = false
        for (e in elementsArray) {
            if (e.className.contains("ManagerHolder") && e.methodName == "getServer") {
                isValid = true
            }
        }
        if (!isValid) {
            throw IllegalAccessException("You can't call this method directly.This is privately function! ")
        }
    }

    override fun listen() {
//        if (serverOption == null) {
//            serverOption = ServerOption.Builder().build()
//        }
        listen(serverOption)
    }

    override fun listen(option: ServerOption) {
//        requireNotNull(option) { "option can not be null" }
//        require(option is ServerOption) { "option must instance of ServerOption" }
        try {
            serverOption = option
            serverSocket = ServerSocket(serverPort)
            configuration(serverSocket)
            acceptThread = AcceptThread("server accepting in $serverPort")
            acceptThread?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            shutdown()
        }
    }

    override val isLive: Boolean
        get() = isInit && serverSocket?.isClosed == false && acceptThread?.isShutdown == false

    @Suppress("UNCHECKED_CAST")
    override val clientPool: IClientPool<String?, IClient?>?
        get() = clientPoolImpl as IClientPool<String?, IClient?>?

    private inner class AcceptThread(name: String) : AbsLoopThread(name) {
        @Throws(Exception::class)
        override fun beforeLoop() {
            clientPoolImpl = ClientPoolImpl(serverOption.connectCapacity)
            @Suppress("UNCHECKED_CAST")
            serverActionDispatcher?.setClientPool(clientPoolImpl as IClientPool<IClient, String>)
            sendBroadcast(IAction.Server.actionServerListening)
        }

        @Throws(Exception::class)
        override fun runLoopThread() {
            serverSocket?.let { socket ->
                val client = ClientImpl(socket.accept(), serverOption)
                client.setClientPool(clientPoolImpl)
                client.setServerStateSender(this@ServerManagerImpl)
                client.startIOEngine()
            }
        }

        override fun loopFinish(e: Exception?) {
            if (e !is InitiativeDisconnectException) {
                sendBroadcast(IAction.Server.actionServerWillBeShutdown, e)
            }
        }
    }

    private fun configuration(serverSocket: ServerSocket?) {
        // TODO 待细化配置
    }

    override fun shutdown() {
        if (serverSocket == null) return
        clientPoolImpl?.serverDown()
        try {
            serverSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        serverSocket = null
        clientPoolImpl = null
        acceptThread?.shutdown(InitiativeDisconnectException())
        acceptThread = null
        sendBroadcast(IAction.Server.actionServerAllReadyShutdown)
    }
}