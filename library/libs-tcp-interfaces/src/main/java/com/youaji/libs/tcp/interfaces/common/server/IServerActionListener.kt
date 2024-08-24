package com.youaji.libs.tcp.interfaces.common.server

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IServerActionListener {
    fun onServerListening(serverPort: Int)
    fun onClientConnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?)
    fun onClientDisconnected(client: IClient?, serverPort: Int, clientPool: IClientPool<IClient, String>?)
    fun onServerWillBeShutdown(serverPort: Int, shutdown: IServerShutdown?, clientPool: IClientPool<IClient, String>?, throwable: Throwable?)
    fun onServerAlreadyShutdown(serverPort: Int)
}