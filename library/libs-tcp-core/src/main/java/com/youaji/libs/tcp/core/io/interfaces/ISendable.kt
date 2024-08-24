package com.youaji.libs.tcp.core.io.interfaces

import java.io.Serializable

/**
 * 可发送类,继承该类,并实现 parse 方法即可获得发送能力
 * @author youaji
 * @since 2024/01/11
 */
interface ISendable : Serializable {
    /**
     * 数据转化
     * @return 将要发送的数据的字节数组
     */
    fun parse(): ByteArray
}