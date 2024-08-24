package com.youaji.libs.tcp.server.impl.client

import java.util.concurrent.ConcurrentSkipListMap

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsClientPool<K, V>(private val capacity: Int) {

    abstract fun onCacheFull(key: K, lastOne: V)
    abstract fun onCacheDuplicate(key: K, oldOne: V)
    abstract fun onCacheEmpty()

    interface Echo<K, V> {
        fun onEcho(key: K, value: V)
    }

    @Volatile
    private var hashMap = ConcurrentSkipListMap<K, V>()

    fun poolSize(): Int = hashMap.size

    @Synchronized
    fun setValue(key: K, value: V) {
        val old = hashMap[key]
        old?.let { onCacheDuplicate(key, it) }
        if (capacity == hashMap.size) {
            tail?.let { t -> onCacheFull(t.key, t.value) }
        }
        if (hashMap.containsKey(key)) return
        if (capacity == hashMap.size) return
        hashMap[key] = value
    }

    fun getValue(key: K): V? = hashMap[key]

    @Synchronized
    fun removeValue(key: K) {
        hashMap.remove(key)
        if (hashMap.isEmpty()) {
            onCacheEmpty()
        }
    }

    @Synchronized
    fun removeValues() {
        hashMap.clear()
    }

    @Synchronized
    fun echoRun(echo: Echo<K, V>?) {
        if (echo == null) return
        val iterator: Iterator<Map.Entry<K, V>> = hashMap.entries.iterator()
        while (iterator.hasNext()) {
            val (key, value) = iterator.next()
            echo.onEcho(key, value)
        }
    }

    private val tail: Map.Entry<K, V>?
        get() {
            if (hashMap.isEmpty()) return null
            val iterator = hashMap.entries.iterator()
            var t: Map.Entry<K, V>? = null
            while (iterator.hasNext()) {
                t = iterator.next()
            }
            return t
        }
}