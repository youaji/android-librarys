package com.youaji.libs.tcp.client.sdk.connection

import com.youaji.libs.tcp.client.impl.PulseManager
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.action.ISocketActionListener
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.pojo.OriginalData

/**
 * 抽象联调管理器
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsReconnectionManager : ISocketActionListener {

    /** 是否销毁 */
    @Volatile
    protected var isDetach = false

    /** 连接管理器 */
    @Volatile
    protected var iConnectionManager: IConnectionManager? = null

    /** 需要忽略的断开连接集合,当 Exception 在此集合中,忽略该类型的断开异常,不会自动重连 */
    @Volatile
    protected var ignoreDisconnectExceptionList = linkedSetOf<Class<out Exception>>()

    /** 心跳管理器 */
    protected var pulseManager: PulseManager? = null

    /**
     * 关联到某一个连接管理器
     * @param manager 当前连接管理器
     */
    @Synchronized
    fun attach(manager: IConnectionManager) {
        if (isDetach) detach()
        isDetach = false
        iConnectionManager = manager
        pulseManager = manager.pulseManager
        iConnectionManager?.registerReceiver(this)
    }

    /** 解除连接当前的连接管理器 */
    @Synchronized
    open fun detach() {
        isDetach = true
        iConnectionManager?.unRegisterReceiver(this)
    }

    /**
     * 添加需要忽略的异常,当断开异常为该异常时,将不会进行重连.
     * @param e 需要忽略的异常
     */
    fun addIgnoreException(e: Class<out Exception>) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.add(e)
        }
    }

    /**
     * 添加需要忽略的异常,当断开异常为该异常时,将不会进行重连.
     * @param e 需要删除的异常
     */
    fun removeIgnoreException(e: Exception) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.remove(e.javaClass)
        }
    }

    /**
     * 删除需要忽略的异常
     * @param e 需要忽略的异常
     */
    fun removeIgnoreException(e: Class<out Exception>) {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.remove(e)
        }
    }

    /** 删除所有忽略的异常 */
    fun removeIgnoreExceptions() {
        synchronized(ignoreDisconnectExceptionList) {
            ignoreDisconnectExceptionList.clear()
        }
    }

    //<editor-fold desc="ISocketActionListener override">
    override fun onSocketIOThreadStart(action: String) {
        //do nothing;
    }

    override fun onSocketIOThreadShutdown(action: String, e: Exception?) {
        //do nothing;
    }

    override fun onSocketReadResponse(info: ConnectionInfo, action: String, data: OriginalData) {
        //do nothing;
    }

    override fun onSocketWriteResponse(info: ConnectionInfo, action: String, data: ISendable) {
        //do nothing;
    }

    override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {
        //do nothing;
    }
    //</editor-fold>
}