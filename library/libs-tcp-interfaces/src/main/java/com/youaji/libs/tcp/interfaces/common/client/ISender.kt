package com.youaji.libs.tcp.interfaces.common.client

import com.youaji.libs.tcp.core.io.interfaces.ISendable

/**
 * @author youaji
 * @since 2024/01/11
 */
interface ISender<T> {
    /**
     * 在当前的连接上发送数据
     *
     * @param sendable 具有发送能力的Bean [ISendable]
     * @return T
     */
    fun send(sendable: ISendable): T
}