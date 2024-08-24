package com.youaji.libs.tcp.interfaces.common.server

import com.youaji.libs.tcp.core.io.interfaces.ISendable

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IClientPool<Client, Key> {
    fun size(): Int
    fun cache(t: Client)
    fun findByUniqueTag(tag: Key): Client
    fun sendToAll(sendable: ISendable)
}