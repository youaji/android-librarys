package com.youaji.libs.tcp.core.io.interfaces

import java.io.OutputStream

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IWriter<Option : ICoreOption> {
    fun initialize(outputStream: OutputStream, stateSender: IStateSender)
    fun setOption(option: Option)
    fun offer(sendable: ISendable?)

    @Throws(RuntimeException::class)
    fun write(): Boolean
    fun close()
}