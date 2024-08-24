package com.youaji.libs.tcp.client.impl.thread

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.action.IAction
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IReader
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import java.io.IOException

/**
 * @author youaji
 * @since 2024/01/11
 */
class DuplexReadThread<Option : ICoreOption>(
    reader: IReader<Option>,
    stateSender: IStateSender,
) : AbsLoopThread("client_duplex_read_thread") {
    private val iStateSender: IStateSender
    private val iReader: IReader<Option>

    init {
        this.iStateSender = stateSender
        this.iReader = reader
    }

    override fun beforeLoop() {
        iStateSender.sendBroadcast(IAction.actionReadThreadStart)
    }

    @Throws(IOException::class)
    override fun runLoopThread() {
        iReader.read()
    }

    @Synchronized
    override fun shutdown(e: Exception?) {
        iReader.close()
        super.shutdown(e)
    }

    override fun loopFinish(e: Exception?) {
        val exception = if (e is ManuallyDisconnectException) null else e
        if (exception != null) SLog.e("duplex read error,thread is dead with exception:${exception.message}")
        iStateSender.sendBroadcast(IAction.actionReadThreadShutdown, e)
    }
}