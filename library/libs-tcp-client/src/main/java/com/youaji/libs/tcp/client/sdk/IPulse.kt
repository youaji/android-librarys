package com.youaji.libs.tcp.client.sdk

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IPulse {
    /** 开始心跳 */
    fun pulse()

    /** 触发一次心跳 */
    fun trigger()

    /** 停止心跳 */
    fun dead()

    /** 心跳返回后喂狗,ACK */
    fun feed()
}