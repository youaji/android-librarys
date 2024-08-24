package com.youaji.libs.tcp.client.impl.thread

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.action.IAction
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.io.interfaces.IWriter
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import java.io.IOException

/**
 * @author youaji
 * @since 2024/01/11
 */
class DuplexWriteThread<Option : ICoreOption>(
    writer: IWriter<Option>,
    stateSender: IStateSender
) : AbsLoopThread("client_duplex_write_thread") {
    private val iStateSender: IStateSender
    private val iWriter: IWriter<Option>

    init {
        this.iStateSender = stateSender
        this.iWriter = writer
    }

    override fun beforeLoop() {
        iStateSender.sendBroadcast(IAction.actionWriteThreadStart)
    }

    @Throws(IOException::class)
    override fun runLoopThread() {
        iWriter.write()
    }

    @Synchronized
    override fun shutdown(e: Exception?) {
        iWriter.close()
        super.shutdown(e)
    }

    override fun loopFinish(e: Exception?) {
        val exception = if (e is ManuallyDisconnectException) null else e
        if (exception != null) SLog.e("duplex write error,thread is dead with exception:${exception.message}")
        iStateSender.sendBroadcast(IAction.actionWriteThreadShutdown, e)
    }
}