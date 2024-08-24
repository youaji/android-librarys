package com.youaji.libs.tcp.client.sdk.connection

import com.youaji.libs.tcp.client.impl.PulseManager
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.action.ISocketActionListener
import com.youaji.libs.tcp.client.sdk.connection.ability.IConfiguration
import com.youaji.libs.tcp.client.sdk.connection.ability.IConnectable
import com.youaji.libs.tcp.interfaces.common.client.IDisConnectable
import com.youaji.libs.tcp.interfaces.common.client.ISender
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IConnectionManager : IConfiguration, IConnectable, IDisConnectable, ISender<IConnectionManager?>, IRegister<ISocketActionListener?, IConnectionManager?> {
    /** 是否连接 true 已连接；false 未连接 */
    val isConnect: Boolean

    /** 是否处在断开连接的阶段 true 正在断开连接；false连接中或者已断开。 */
    val isDisconnecting: Boolean

    /** 心跳管理器,用来配置心跳参数和心跳行为 */
    val pulseManager: PulseManager?

    /** 连接信息 */
    val remoteConnectionInfo: ConnectionInfo

    /** 重连管理器,用来配置重连管理器 */
    val reconnectionManager: AbsReconnectionManager?

    /** 本地连接（绑定）信息 */
    fun setLocalConnectionInfo(info: ConnectionInfo?)
    fun getLocalConnectionInfo(): ConnectionInfo?

    /**
     * 是否保存此次连接
     * @param isHolder true 进行保留缓存管理，false 不进行保存缓存管理。
     */
    fun setConnectionHolder(isHolder: Boolean)

    /**
     * 将当前的连接管理器中的连接信息进行切换.
     * @param info 新的连接信息
     */
    fun switchConnectionInfo(info: ConnectionInfo?)
}