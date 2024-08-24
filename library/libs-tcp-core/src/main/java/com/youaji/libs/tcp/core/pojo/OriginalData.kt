package com.youaji.libs.tcp.core.pojo

import java.io.Serializable

/**
 * 原始数据结构体
 * @author youaji
 * @since 2024/01/11
 */
data class OriginalData(
    /** 原始数据包头字节数组 */
    var headBytes: ByteArray = byteArrayOf(),
    /** 原始数据包体字节数组 */
    var bodyBytes: ByteArray = byteArrayOf(),
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OriginalData

        if (!headBytes.contentEquals(other.headBytes)) return false
        if (!bodyBytes.contentEquals(other.bodyBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = headBytes.contentHashCode()
        result = 31 * result + bodyBytes.contentHashCode()
        return result
    }
}