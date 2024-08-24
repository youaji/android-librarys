package com.youaji.libs.tcp.client.impl.thread

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.action.IAction
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IReader
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.io.interfaces.IWriter
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import java.io.IOException

/**
 * @author youaji
 * @since 2024/01/11
 */
class SimplexIOThread<Option : ICoreOption>(
    reader: IReader<Option>,
    writer: IWriter<Option>,
    stateSender: IStateSender,
) : AbsLoopThread("client_simplex_io_thread") {
    private val mStateSender: IStateSender
    private val mReader: IReader<Option>
    private val mWriter: IWriter<Option>
    private var isWrite = false

    init {
        this.mStateSender = stateSender
        this.mReader = reader
        this.mWriter = writer
    }

    @Throws(IOException::class)
    override fun beforeLoop() {
        mStateSender.sendBroadcast(IAction.actionWriteThreadStart)
        mStateSender.sendBroadcast(IAction.actionReadThreadStart)
    }

    @Throws(IOException::class)
    override fun runLoopThread() {
        isWrite = mWriter.write()
        if (isWrite) {
            isWrite = false
            mReader.read()
        }
    }

    @Synchronized
    override fun shutdown(e: Exception?) {
        mReader.close()
        mWriter.close()
        super.shutdown(e)
    }

    override fun loopFinish(e: Exception?) {
        var e = e
        e = if (e is ManuallyDisconnectException) null else e
        if (e != null) {
            SLog.e("simplex error,thread is dead with exception:" + e.message)
        }
        mStateSender.sendBroadcast(IAction.actionWriteThreadShutdown, e)
        mStateSender.sendBroadcast(IAction.actionReadThreadShutdown, e)
    }
}