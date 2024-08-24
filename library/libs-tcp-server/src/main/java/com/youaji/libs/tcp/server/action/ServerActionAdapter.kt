package com.youaji.libs.tcp.server.action

import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.interfaces.common.server.IClientPool
import com.youaji.libs.tcp.interfaces.common.server.IServerActionListener
import com.youaji.libs.tcp.interfaces.common.server.IServerShutdown

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class ServerActionAdapter : IServerActionListener {
    override fun onServerListening(serverPort: Int) {}
    override fun onClientConnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?) {}
    override fun onClientDisconnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?) {}
    override fun onServerWillBeShutdown(serverPort: Int, shutdown: IServerShutdown?, clientPool: IClientPool<IClient, String>?, throwable: Throwable?) {}
    override fun onServerAlreadyShutdown(serverPort: Int) {}
}