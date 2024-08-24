package com.youaji.libs.tcp.interfaces.common.server

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IServerManagerPrivate<Option : ICoreOption> : IServerManager<Option> {
    fun initServerPrivate(serverPort: Int)
}