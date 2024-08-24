package com.youaji.libs.tcp.server.impl.io

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IReader
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.server.action.IAction
import com.youaji.libs.tcp.server.exception.InitiativeDisconnectException
import java.io.IOException

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientReadThread<Option : ICoreOption>(
    private val iReader: IReader<Option>,
    private val clientStateSender: IStateSender,
) : AbsLoopThread("server_client_read_thread") {

    override fun beforeLoop() {
        clientStateSender.sendBroadcast(IAction.Client.actionReadThreadStart)
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
        val exception = if (e is InitiativeDisconnectException) null else e
        if (exception != null) SLog.e("duplex read error,thread is dead with exception:" + exception.message)
        clientStateSender.sendBroadcast(IAction.Client.actionReadThreadShutdown, e)
    }
}