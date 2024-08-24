package com.youaji.libs.tcp.core.io.interfaces

import java.io.InputStream

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IReader<Option : ICoreOption> {
    fun initialize(inputStream: InputStream, stateSender: IStateSender)
    fun setOption(option: Option)

    @Throws(RuntimeException::class)
    fun read()

    fun close()
}