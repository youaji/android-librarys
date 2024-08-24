package com.youaji.libs.tcp.client.sdk

import java.io.Serializable

/**
 * 连接信息服务类
 * @author youaji
 * @since 2024/01/11
 */
data class ConnectionInfo(
    /** IPV4地址 */
    val ip: String,
    /** 连接服务器端口号 */
    val port: Int,
    /** 当此IP地址Ping不通时的备用IP -- 可以不设置 */
    var backupInfo: ConnectionInfo? = null
) : Serializable, Cloneable {

    public override fun clone(): ConnectionInfo {
        val connectionInfo = ConnectionInfo(ip, port)
        backupInfo?.let { connectionInfo.backupInfo = it.clone() }
        return connectionInfo
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConnectionInfo

        if (ip != other.ip) return false
        if (port != other.port) return false
        if (backupInfo != other.backupInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ip.hashCode()
        result = 31 * result + port
        result = 31 * result + (backupInfo?.hashCode() ?: 0)
        return result
    }
}