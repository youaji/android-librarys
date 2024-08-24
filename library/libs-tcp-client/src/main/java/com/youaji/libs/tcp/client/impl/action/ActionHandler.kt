package com.youaji.libs.tcp.client.impl.action

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.action.ISocketActionListener
import com.youaji.libs.tcp.client.sdk.action.SocketActionAdapter
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister

/**
 * @author youaji
 * @since 2024/01/11
 */
class ActionHandler : SocketActionAdapter() {
    private var iConnectionManager: IConnectionManager? = null
    private var currentThreadMode: ClientOption.IOThreadMode? = null
    private var iOThreadIsCalledDisconnect = false
    fun attach(manager: IConnectionManager?, register: IRegister<ISocketActionListener?, IConnectionManager?>) {
        iConnectionManager = manager
        register.registerReceiver(this)
    }

    fun detach(register: IRegister<ISocketActionListener?, IConnectionManager?>) {
        register.unRegisterReceiver(this)
    }

    override fun onSocketIOThreadStart(action: String) {
        if (iConnectionManager?.option?.ioThreadMode !== currentThreadMode) {
            currentThreadMode = iConnectionManager?.option?.ioThreadMode
        }
        iOThreadIsCalledDisconnect = false
    }

    override fun onSocketIOThreadShutdown(action: String, e: Exception?) {
        if (currentThreadMode !== iConnectionManager?.option?.ioThreadMode) { // 切换线程模式,不需要断开连接
            //do nothing
        } else { //多工模式
            if (!iOThreadIsCalledDisconnect) { //保证只调用一次,多工多线程,会调用两次
                iOThreadIsCalledDisconnect = true
                if (e !is ManuallyDisconnectException) {
                    iConnectionManager?.disconnect(e)
                }
            }
        }
    }

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: String, e: Exception?) {
        iConnectionManager?.disconnect(e)
    }
}