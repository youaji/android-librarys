package com.youaji.libs.tcp.client.impl.action

import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionConnectionFailed
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionConnectionSuccess
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionDisconnection
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionReadThreadShutdown
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionReadThreadStart
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionWriteThreadShutdown
import com.youaji.libs.tcp.client.sdk.action.IAction.Companion.actionWriteThreadStart
import com.youaji.libs.tcp.client.sdk.action.ISocketActionListener
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.core.io.interfaces.IOAction.Companion.actionPulseRequest
import com.youaji.libs.tcp.core.io.interfaces.IOAction.Companion.actionReadComplete
import com.youaji.libs.tcp.core.io.interfaces.IOAction.Companion.actionWriteComplete
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister
import java.io.Serializable
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * 状态机
 * @author youaji
 * @since 2024/01/11
 */
class ActionDispatcher(
    info: ConnectionInfo,
    manager: IConnectionManager,
) : IRegister<ISocketActionListener?, IConnectionManager?>, IStateSender {

    /** 连接信息 */
    @Volatile
    private var connectionInfo: ConnectionInfo

    /** 连接管理器 */
    @Volatile
    private var iConnectionManager: IConnectionManager

    /** 行为回调集合 */
    @Volatile
    private var responseHandlerList = mutableListOf<ISocketActionListener>()

    /** 公平锁,虽然没啥卵用公平,因为使用了 tryLock */
    private val _lock = ReentrantLock(true)

    init {
        iConnectionManager = manager
        connectionInfo = info
    }

    companion object {
        /** 线程回调管理Handler */
        private val HANDLE_THREAD = DispatchThread()

        /** 事件消费队列 */
        private val ACTION_QUEUE: LinkedBlockingQueue<ActionInfo?> = LinkedBlockingQueue<ActionInfo?>()

        init {
            // 启动分发线程
            HANDLE_THREAD.start()
        }
    }

    fun setConnectionInfo(connectionInfo: ConnectionInfo) {
        this.connectionInfo = connectionInfo
    }

    //<editor-fold desc="IRegister override">
    override fun registerReceiver(socketActionListener: ISocketActionListener?): IConnectionManager {
        if (socketActionListener != null) {
            try {
                while (true) {
                    if (_lock.tryLock(1, TimeUnit.SECONDS)) {
                        if (!responseHandlerList.contains(socketActionListener)) {
                            responseHandlerList.add(socketActionListener)
                        }
                        break
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                _lock.unlock()
            }
        }
        return iConnectionManager
    }

    override fun unRegisterReceiver(socketActionListener: ISocketActionListener?): IConnectionManager {
        if (socketActionListener != null) {
            try {
                while (true) {
                    if (_lock.tryLock(1, TimeUnit.SECONDS)) {
                        responseHandlerList.remove(socketActionListener)
                        break
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                _lock.unlock()
            }
        }
        return iConnectionManager
    }
    //</editor-fold>

    /**
     * 分发收到的响应
     *
     * @param action
     * @param arg
     * @param responseHandler
     */
    private fun dispatchActionToListener(action: String, arg: Serializable?, responseHandler: ISocketActionListener) {
        when (action) {
            actionConnectionSuccess -> {
                try {
                    responseHandler.onSocketConnectionSuccess(connectionInfo, action)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionConnectionFailed -> {
                try {
                    val exception = arg as Exception?
                    responseHandler.onSocketConnectionFailed(connectionInfo, action, exception)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionDisconnection -> {
                try {
                    val exception = arg as Exception?
                    responseHandler.onSocketDisconnection(connectionInfo, action, exception)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionReadComplete -> {
                try {
                    arg?.let {
                        if (it is OriginalData) {
                            responseHandler.onSocketReadResponse(connectionInfo, action, it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionReadThreadStart, actionWriteThreadStart -> {
                try {
                    responseHandler.onSocketIOThreadStart(action)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionWriteComplete -> {
                try {
                    arg?.let {
                        if (it is ISendable) {
                            responseHandler.onSocketWriteResponse(connectionInfo, action, it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionWriteThreadShutdown, actionReadThreadShutdown -> {
                try {
                    val exception = arg as Exception?
                    responseHandler.onSocketIOThreadShutdown(action, exception)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionPulseRequest -> {
                try {
                    arg?.let {
                        if (it is IPulseSendable) {
                            responseHandler.onPulseSend(connectionInfo, it)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    //<editor-fold desc="IStateSender override">
    override fun sendBroadcast(action: String) {
        sendBroadcast(action, null)
    }

    override fun sendBroadcast(action: String, serializable: Serializable?) {
        val option: ClientOption = iConnectionManager.option
        val token = option.callbackThreadModeToken
        if (token != null) {
            val runnable = ActionRunnable(ActionInfo(action, serializable, this))
            try {
                token.handleCallbackEvent(runnable)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (option.isCallbackInIndependentThread) { // 独立线程进行回调
            ACTION_QUEUE.offer(ActionInfo(action, serializable, this))
        } else if (!option.isCallbackInIndependentThread) { // IO线程里进行回调
            try {
                while (true) {
                    if (_lock.tryLock(1, TimeUnit.SECONDS)) {
                        val copyData = ArrayList<ISocketActionListener>(responseHandlerList)
                        val iterator = copyData.iterator()
                        while (iterator.hasNext()) {
                            dispatchActionToListener(action, serializable, iterator.next())
                        }
                        break
                    }
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                _lock.unlock()
            }
        } else {
            SLog.e("ActionDispatcher error action:$action is not dispatch")
        }
    }
    //</editor-fold>

    /** 分发线程 */
    private class DispatchThread : AbsLoopThread("client_action_dispatch_thread") {
        @Throws(Exception::class)
        override fun runLoopThread() {
            ACTION_QUEUE.take()?.let { actionInfo ->
                actionInfo.dispatcher?.let { dispatcher ->
                    synchronized(dispatcher.responseHandlerList) {
                        val copyData = ArrayList<ISocketActionListener>(dispatcher.responseHandlerList)
                        val iterator = copyData.iterator()
                        while (iterator.hasNext()) {
                            dispatcher.dispatchActionToListener(actionInfo.action, actionInfo.arg, iterator.next())
                        }
                    }
                }
            }
        }

        override fun loopFinish(e: Exception?) {}
    }

    /** 行为封装 */
    data class ActionInfo(
        val action: String,
        var arg: Serializable? = null,
        var dispatcher: ActionDispatcher? = null
    )

    /** 行为分发抽象 */
    class ActionRunnable internal constructor(private val actionInfo: ActionInfo) : Runnable {
        override fun run() {
            actionInfo.dispatcher?.let { dispatcher ->
                synchronized(dispatcher.responseHandlerList) {
                    val copyData = ArrayList<ISocketActionListener>(dispatcher.responseHandlerList)
                    val iterator = copyData.iterator()
                    while (iterator.hasNext()) {
                        dispatcher.dispatchActionToListener(actionInfo.action, actionInfo.arg, iterator.next())
                    }
                }
            }
        }
    }


}