package com.youaji.libs.tcp.interfaces.common.server

import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.pojo.OriginalData

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IClientIOCallback {
    fun onClientRead(data: OriginalData?, client: IClient?, pool: IClientPool<IClient?, String>)
    fun onClientWrite(sendable: ISendable?, client: IClient?, pool: IClientPool<IClient?, String>)
}