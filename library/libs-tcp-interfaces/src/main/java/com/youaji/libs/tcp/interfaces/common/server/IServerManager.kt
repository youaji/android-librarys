package com.youaji.libs.tcp.interfaces.common.server

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IServerManager<Option : ICoreOption> : IServerShutdown {
    val isLive: Boolean
    val clientPool: IClientPool<String?, IClient?>?
    fun listen()
    fun listen(option: Option)

}