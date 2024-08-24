package com.youaji.libs.tcp.client.sdk.connection.ability

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IConnectable {
    /** 将当前连接管理器发起连接 */
    fun connect()
}