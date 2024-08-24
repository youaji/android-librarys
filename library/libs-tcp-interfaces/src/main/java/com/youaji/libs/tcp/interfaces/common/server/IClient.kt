package com.youaji.libs.tcp.interfaces.common.server

import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.interfaces.common.client.IDisConnectable
import com.youaji.libs.tcp.interfaces.common.client.ISender
import java.io.Serializable

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IClient : IDisConnectable, ISender<IClient>, Serializable {
    val hostIp: String
    val hostName: String
    val uniqueTag: String
    fun setReaderProtocol(protocol: IReaderProtocol)
    fun addIOCallback(callback: IClientIOCallback)
    fun removeIOCallback(callback: IClientIOCallback)
    fun removeIOCallbacks()
}