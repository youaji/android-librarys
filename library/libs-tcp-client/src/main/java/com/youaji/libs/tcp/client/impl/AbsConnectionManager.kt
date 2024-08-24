package com.youaji.libs.tcp.client.impl

import com.youaji.libs.tcp.client.impl.ability.IConnectionSwitchListener
import com.youaji.libs.tcp.client.impl.action.ActionDispatcher
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.action.ISocketActionListener
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import java.io.Serializable

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsConnectionManager(
    /** 连接信息 */
    protected var remoteInfo: ConnectionInfo,
    /** 本地绑定信息 */
    protected var localInfo: ConnectionInfo? = null,
) : IConnectionManager {
    /** 连接信息 switch 监听器 */
    private var iConnectionSwitchListener: IConnectionSwitchListener? = null

    /** 状态机 */
    protected val actionDispatcher: ActionDispatcher by lazy { ActionDispatcher(remoteInfo, this) }

    fun setOnConnectionSwitchListener(listener: IConnectionSwitchListener?) {
        iConnectionSwitchListener = listener
    }

    protected fun sendBroadcast(action: String, serializable: Serializable?) {
        actionDispatcher.sendBroadcast(action, serializable)
    }

    protected fun sendBroadcast(action: String) {
        actionDispatcher.sendBroadcast(action)
    }

    //<editor-fold desc="IRegister override">
    override fun registerReceiver(socketActionListener: ISocketActionListener?): IConnectionManager {
        actionDispatcher.registerReceiver(socketActionListener)
        return this
    }

    override fun unRegisterReceiver(socketActionListener: ISocketActionListener?): IConnectionManager {
        actionDispatcher.unRegisterReceiver(socketActionListener)
        return this
    }
    //</editor-fold>

    //<editor-fold desc="IConnectionManager override">
    override val remoteConnectionInfo: ConnectionInfo
        get() = remoteInfo

    override fun getLocalConnectionInfo(): ConnectionInfo? {
        return localInfo
    }

    @Synchronized
    override fun switchConnectionInfo(info: ConnectionInfo?) {
        info?.let { i ->
            val tempOldInfo = remoteInfo
            remoteInfo = i.clone()
            actionDispatcher.setConnectionInfo(remoteInfo)
            iConnectionSwitchListener?.onSwitchConnectionInfo(this, tempOldInfo, remoteInfo)
        }
    }
    //</editor-fold>


}