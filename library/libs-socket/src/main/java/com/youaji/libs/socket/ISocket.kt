package com.youaji.libs.socket

import java.net.DatagramPacket
import java.util.concurrent.Executor

interface ISocket<out SOCKET> {

    /** @return 是否启动 */
    fun isStart(): Boolean

    /** @return 是否已经连接 */
    fun isConnected(): Boolean

    /** @return 是否已经关闭 */
    fun isClosed(): Boolean

    /** 原始对象 */
    fun getSocket(): SOCKET?

    /** @return 创建 Socket，主要是便于扩展和修改配置，仅供其子类内部调用，外部请勿直接调用 */
    @Throws(Exception::class)
    fun createSocket(): SOCKET

    /** 启动 */
    fun start()

    /** 关闭 */
    fun close()

    /** 写入数据 */
    fun write(data: ByteArray?)

    /** 写入数据包 */
    fun write(data: DatagramPacket?)

    /** 设置[Executor]，需在 [start] 之前才有效 */
    fun setExecutor(executor: Executor?)

    /** 设置状态监听器 */
    fun setOnSocketStateListener(listener: OnSocketStateListener?)

    /** 设置消息接收监听器 */
    fun setOnMessageReceivedListener(listener: OnMessageReceivedListener?)
}

/** 消息接收监听器 */
interface OnMessageReceivedListener {
    /** 消息接收 */
    fun onReceived(data: ByteArray?)
}

/** 状态监听 */
interface OnSocketStateListener {
    /** 已启动 */
    fun onStarted()

    /** 已关闭 */
    fun onClosed()

    /** 异常 */
    fun onException(e: Exception?)
}