package com.youaji.libs.tcp.server.action

import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister
import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.interfaces.common.server.IClientPool
import com.youaji.libs.tcp.interfaces.common.server.IServerActionListener
import com.youaji.libs.tcp.interfaces.common.server.IServerManager
import com.youaji.libs.tcp.server.action.IAction.Server.Companion.actionClientConnected
import com.youaji.libs.tcp.server.action.IAction.Server.Companion.actionClientDisconnected
import com.youaji.libs.tcp.server.action.IAction.Server.Companion.actionServerAllReadyShutdown
import com.youaji.libs.tcp.server.action.IAction.Server.Companion.actionServerListening
import com.youaji.libs.tcp.server.action.IAction.Server.Companion.actionServerWillBeShutdown
import com.youaji.libs.tcp.server.impl.ServerOption
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue

/**
 * 服务器状态机
 * @author youaji
 * @since 2024/01/11
 */
class ServerActionDispatcher(
    manager: IServerManager<ServerOption>?
) : IRegister<IServerActionListener?, IServerManager<ServerOption>?>, IStateSender {

    companion object {
        /** 线程回调管理Handler */
        private val HANDLE_THREAD = DispatchThread()

        /** 事件消费队列 */
        private val ACTION_QUEUE = LinkedBlockingQueue<ActionInfo?>()

        init {
            // 启动分发线程
            HANDLE_THREAD.start()
        }
    }

    /** 回调列表 */
    @Volatile
    private var responseHandlerList = mutableListOf<IServerActionListener>()

    /** 服务器端口 */
    @Volatile
    private var serverPort = 0

    /** 客户端池子 */
    @Volatile
    private var clientPool: IClientPool<IClient, String>? = null

    /** 服务器管理器实例 */
    @Volatile
    private var serverManager: IServerManager<ServerOption>?

    init {
        serverManager = manager
    }

    fun setServerPort(localPort: Int) {
        serverPort = localPort
    }

    fun setClientPool(clientPool: IClientPool<IClient, String>?) {
        this.clientPool = clientPool
    }

    override fun registerReceiver(socketActionListener: IServerActionListener?): IServerManager<ServerOption>? {
        if (socketActionListener != null) {
            synchronized(responseHandlerList) {
                if (!responseHandlerList.contains(socketActionListener)) {
                    responseHandlerList.add(socketActionListener)
                }
            }
        }
        return serverManager
    }

    override fun unRegisterReceiver(socketActionListener: IServerActionListener?): IServerManager<ServerOption>? {
        synchronized(responseHandlerList) { responseHandlerList.remove(socketActionListener) }
        return serverManager
    }

    /**
     * 分发收到的响应
     *
     * @param action
     * @param responseHandler
     */
    private fun dispatchActionToListener(action: String, arg: Any?, responseHandler: IServerActionListener) {
        when (action) {
            actionServerListening -> {
                try {
                    responseHandler.onServerListening(serverPort)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionClientConnected -> {
                try {
                    val client: IClient? = arg as IClient?
                    responseHandler.onClientConnected(client, serverPort, clientPool)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionClientDisconnected -> {
                try {
                    val client: IClient? = arg as IClient?
                    responseHandler.onClientDisconnected(client, serverPort, clientPool)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionServerWillBeShutdown -> {
                try {
                    val throwable = arg as Throwable?
                    responseHandler.onServerWillBeShutdown(serverPort, serverManager, clientPool, throwable)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionServerAllReadyShutdown -> {
                try {
                    responseHandler.onServerAlreadyShutdown(serverPort)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun sendBroadcast(action: String) {
        sendBroadcast(action, null)
    }

    override fun sendBroadcast(action: String, serializable: Serializable?) {
        ACTION_QUEUE.offer(ActionInfo(action, serializable, this))
    }

    /** 行为封装 */
    data class ActionInfo(
        var action: String = "",
        var arg: Serializable? = null,
        var dispatcher: ServerActionDispatcher? = null
    )

    /** 分发线程 */
    private class DispatchThread : AbsLoopThread("server_action_dispatch_thread") {
        @Throws(Exception::class)
        override fun runLoopThread() {
            val actionInfo = ACTION_QUEUE.take()
            actionInfo?.dispatcher?.let { dispatcher ->
                val list = dispatcher.responseHandlerList
                synchronized(list) {
                    val iterator: MutableIterator<IServerActionListener?> = list.iterator()
                    while (iterator.hasNext()) {
                        iterator.next()?.let { listener ->
                            dispatcher.dispatchActionToListener(actionInfo.action, actionInfo.arg, listener)
                        }
                    }
                }
            }
        }

        override fun loopFinish(e: Exception?) {}
    }


}