package com.youaji.libs.tcp.client.sdk

import com.youaji.libs.tcp.client.impl.ManagerHolder
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.interfaces.common.dispatcher.IRegister
import com.youaji.libs.tcp.interfaces.common.server.IServerActionListener
import com.youaji.libs.tcp.interfaces.common.server.IServerManager

/**
 * OkSocket是一款轻量级的Socket通讯框架,可以提供单工,双工的TCP通讯.
 * 本类提供OkSocket的所有对外接口,使用OkSocket框架应从本类的open开启一个连接通道.
 * @author youaji
 * @since 2024/01/11
 */
object YouSocket {
    private val holder: ManagerHolder by lazy { ManagerHolder.get }

    /**
     * 获得一个SocketServer服务器.
     *
     * @param serverPort
     * @return
     */
    fun <ServerOption : ICoreOption> server(serverPort: Int): IRegister<IServerActionListener, IServerManager<ServerOption>> {
        @Suppress("UNCHECKED_CAST")
        return holder.getServer<ServerOption>(serverPort) as IRegister<IServerActionListener, IServerManager<ServerOption>>
    }

    /**
     * 开启一个socket通讯通道,参配为默认参配
     *
     * @param connectInfo 连接信息[ConnectionInfo]
     * @return 该参数的连接管理器 [IConnectionManager] 连接参数仅作为配置该通道的参配,不影响全局参配
     */
    fun open(connectInfo: ConnectionInfo): IConnectionManager {
        return holder.getConnection(connectInfo)
    }

    /**
     * 开启一个socket通讯通道,参配为默认参配
     *
     * @param ip   需要连接的主机IPV4地址
     * @param port 需要连接的主机开放的Socket端口号
     * @return 该参数的连接管理器 [IConnectionManager] 连接参数仅作为配置该通道的参配,不影响全局参配
     */
    fun open(ip: String, port: Int): IConnectionManager {
        val info = ConnectionInfo(ip, port)
        return holder.getConnection(info)
    }

    /**
     * 开启一个socket通讯通道
     * Deprecated please use [YouSocket.open]@[IConnectionManager.option]
     *
     * @param connectInfo 连接信息[ConnectionInfo]
     * @param options   连接参配[ClientOption]
     * @return 该参数的连接管理器 [IConnectionManager] 连接参数仅作为配置该通道的参配,不影响全局参配
     */
    @Deprecated("")
    fun open(connectInfo: ConnectionInfo, options: ClientOption): IConnectionManager {
        return holder.getConnection(connectInfo, options)
    }

    /**
     * 开启一个 socket 通讯通道
     * Deprecated please use [YouSocket.open] [IConnectionManager.option]
     *
     * @param ip        需要连接的主机IPV4地址
     * @param port      需要连接的主机开放的Socket端口号
     * @param options   连接参配[ClientOption]
     * @return 该参数的连接管理器 [IConnectionManager]
     */
    @Deprecated("")
    fun open(ip: String, port: Int, options: ClientOption): IConnectionManager {
        val info = ConnectionInfo(ip, port)
        return holder.getConnection(info, options)
    }
}