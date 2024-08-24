package com.youaji.libs.tcp.server.impl

import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister
import com.youaji.libs.tcp.interfaces.common.server.IServerActionListener
import com.youaji.libs.tcp.interfaces.common.server.IServerManager
import com.youaji.libs.tcp.server.action.ServerActionDispatcher
import java.io.Serializable

/**
 * @author youaji
 * @since 2024/01/11
 */
open class AbsServerRegisterProxy : IRegister<IServerActionListener?, IServerManager<ServerOption>?>, IStateSender {

    @JvmField
    protected var serverActionDispatcher: ServerActionDispatcher? = null
    private var iServerManager: IServerManager<ServerOption>? = null

    protected fun init(serverManager: IServerManager<ServerOption>?) {
        iServerManager = serverManager
        serverActionDispatcher = ServerActionDispatcher(iServerManager)
    }

    override fun registerReceiver(socketActionListener: IServerActionListener?): IServerManager<ServerOption>? {
        return serverActionDispatcher?.registerReceiver(socketActionListener)
    }

    override fun unRegisterReceiver(socketActionListener: IServerActionListener?): IServerManager<ServerOption>? {
        return serverActionDispatcher?.unRegisterReceiver(socketActionListener)
    }

    override fun sendBroadcast(action: String) {
        serverActionDispatcher?.sendBroadcast(action)
    }

    override fun sendBroadcast(action: String, serializable: Serializable?) {
        serverActionDispatcher?.sendBroadcast(action, serializable)
    }
}