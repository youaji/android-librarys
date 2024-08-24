package com.youaji.libs.tcp.interfaces.default

import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author youaji
 * @since 2024/01/11
 */
class DefaultReaderProtocol : IReaderProtocol {
    override val headerLength: Int
        get() = 4

    override fun getBodyLength(header: ByteArray?, byteOrder: ByteOrder?): Int {
        return header?.let { h ->
            byteOrder?.let { o ->
                if (header.size < headerLength) 0
                else {
                    val b = ByteBuffer.wrap(h)
                    b.order(o)
                    b.int
                }
            } ?: 0
        } ?: 0
    }
}