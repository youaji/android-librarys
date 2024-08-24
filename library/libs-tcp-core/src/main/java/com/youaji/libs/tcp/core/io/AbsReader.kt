package com.youaji.libs.tcp.core.io

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IReader
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import java.io.IOException
import java.io.InputStream

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsReader<Option : ICoreOption> : IReader<Option> {
    @JvmField
    @Volatile
    protected var option: Option? = null
    protected var iStateSender: IStateSender? = null
    protected var inputStream: InputStream? = null

    override fun initialize(inputStream: InputStream, stateSender: IStateSender) {
        iStateSender = stateSender
        this.inputStream = inputStream
    }

    override fun setOption(option: Option) {
        this.option = option
    }

    override fun close() {
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}