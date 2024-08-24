package com.youaji.libs.tcp.client.sdk

import java.net.Socket

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class YouSocketFactory {
    @Throws(Exception::class)
    abstract fun createSocket(info: ConnectionInfo, options: ClientOption): Socket
}