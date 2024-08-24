package com.youaji.libs.tcp.client.impl.ability

import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IConnectionSwitchListener {
    fun onSwitchConnectionInfo(manager: IConnectionManager, oldInfo: ConnectionInfo, newInfo: ConnectionInfo)
}