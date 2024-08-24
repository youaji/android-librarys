package com.youaji.libs.tcp.server.impl.client

import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.interfaces.common.server.IClient
import com.youaji.libs.tcp.interfaces.common.server.IClientPool
import com.youaji.libs.tcp.server.exception.CacheException

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientPoolImpl(
    capacity: Int
) : AbsClientPool<String, IClient>(capacity), IClientPool<IClient?, String> {

    fun unCache(iClient: IClient) {
        removeValue(iClient.uniqueTag)
    }

    fun unCache(key: String) {
        removeValue(key)
    }

    fun serverDown() {
        echoRun(object : Echo<String, IClient> {
            override fun onEcho(key: String, value: IClient) {
                value.disconnect()
            }
        })
        removeValues()
    }

    //<editor-fold desc="IClientPool override">
    override fun size(): Int = super.poolSize()

    override fun cache(t: IClient?) {
        t?.let { c -> super.setValue(c.uniqueTag, c) }
    }

    override fun findByUniqueTag(tag: String): IClient? {
        return super.getValue(tag)
    }

    override fun sendToAll(sendable: ISendable) {
        echoRun(object : Echo<String, IClient> {
            override fun onEcho(key: String, value: IClient) {
                value.send(sendable)
            }
        })
    }
    //</editor-fold>

    //<editor-fold desc="AbsClientPool override">
    override fun onCacheFull(key: String, lastOne: IClient) {
        lastOne.disconnect(CacheException("cache is full,you need remove"))
        unCache(lastOne)
    }

    override fun onCacheDuplicate(key: String, oldOne: IClient) {
        oldOne.disconnect(CacheException("there are cached in this server.it need removed before new cache"))
        unCache(oldOne)
    }

    override fun onCacheEmpty() {
        //do nothing
    }
    //</editor-fold>
}