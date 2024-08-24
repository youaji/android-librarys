package com.youaji.libs.tcp.core.io.interfaces

import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import java.nio.ByteOrder

/**
 * @author youaji
 * @since 2024/01/11
 */
interface ICoreOption {
    /** 调试模式 */
    val isDebug: Boolean

    /** 最大读取数据的兆数(MB) 防止数据体过大的数据导致前端内存溢出。 */
    val maxReadDataMB: Int

    /** Socket 通讯中,业务层定义的数据包包头格式 */
    val readerProtocol: IReaderProtocol

    /** 读取 Socket 管道中字节序时的字节序 */
    val readByteOrder: ByteOrder

    /** 写入 Socket 管道中字节序时的字节序 */
    val writeByteOrder: ByteOrder

    /** 读取时单次读取的缓存字节长度,数值越大,读取效率越高.但是相应的系统消耗将越大 */
    val readPackageBytes: Int

    /** 发送时单个数据包的总长度 */
    val writePackageBytes: Int
}