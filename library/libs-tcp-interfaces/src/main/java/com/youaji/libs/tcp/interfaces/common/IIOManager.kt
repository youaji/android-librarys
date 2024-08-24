package com.youaji.libs.tcp.interfaces.common

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.ISendable

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IIOManager<Option : ICoreOption> {
    fun startEngine()
    fun setOption(option: Option)
    fun send(sendable: ISendable)
    fun close()
    fun close(e: Exception?)
}