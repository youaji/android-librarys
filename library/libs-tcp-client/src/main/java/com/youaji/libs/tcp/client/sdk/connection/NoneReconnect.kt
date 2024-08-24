package com.youaji.libs.tcp.client.sdk.connection

import com.youaji.libs.tcp.client.sdk.ConnectionInfo

/**
 * 不进行重新连接的重连管理器
 * @author youaji
 * @since 2024/01/11
 */
class NoneReconnect : AbsReconnectionManager() {
    override fun onSocketDisconnection(info: ConnectionInfo, action: String, e: Exception?) {}
    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: String) {}
    override fun onSocketConnectionFailed(info: ConnectionInfo, action: String, e: Exception?) {}
}