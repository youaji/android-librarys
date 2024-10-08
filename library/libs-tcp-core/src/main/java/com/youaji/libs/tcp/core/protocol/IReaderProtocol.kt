package com.youaji.libs.tcp.core.protocol

import java.nio.ByteOrder

/**
 * 包头数据格式
 * @author youaji
 * @since 2024/01/11
 */
interface IReaderProtocol {
    /**
     * 返回包头长度,该长度将告知框架,解析服务端数据时,哪一部分数据属于包头.
     *
     * @return 包头的长度, 该长度应该是一个固定的包头长度
     */
    val headerLength: Int

    /**
     * 框架根据[IReaderProtocol.headerLength]方法截取到包头后,讲会调用该方法<br></br>
     * 交由开发者进行解析,开发者应该从参数 [.getBodyLength]}header中解析出该次通讯服务端返回的包体长度<br></br>
     *
     * @param header    根据getHeaderLength()方法获得的包头原始数据.开发者应该从此header种解析出包体长度数据.
     * @param byteOrder 当前包头字节数组种,包头数据的字节序类型.
     * @return 开发者应该从此header种解析出包体长度数据.此值不应该是一个字面量定值, 应该是解析出来的一个数值.
     */
    fun getBodyLength(header: ByteArray?, byteOrder: ByteOrder?): Int
}