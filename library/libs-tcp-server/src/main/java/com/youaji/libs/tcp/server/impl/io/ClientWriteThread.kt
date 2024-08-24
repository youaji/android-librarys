package com.youaji.libs.tcp.server.impl.io

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.io.interfaces.IWriter
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.server.action.IAction
import com.youaji.libs.tcp.server.exception.InitiativeDisconnectException
import java.io.IOException

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientWriteThread<Option : ICoreOption>(
    private val iWriter: IWriter<Option>,
    private val clientStateSender: IStateSender,
) : AbsLoopThread("server_client_write_thread") {

    override fun beforeLoop() {
        clientStateSender.sendBroadcast(IAction.Client.actionWriteThreadStart)
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
        val exception = if (e is InitiativeDisconnectException) null else e
        if (exception != null) SLog.e("duplex write error,thread is dead with exception:" + exception.message)
        clientStateSender.sendBroadcast(IAction.Client.actionReadThreadShutdown, e)
    }
}