package com.youaji.libs.tcp.server.impl.io

import com.youaji.libs.tcp.core.io.ReaderImpl
import com.youaji.libs.tcp.core.io.WriterImpl
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.interfaces.common.IIOManager
import com.youaji.libs.tcp.server.exception.InitiativeDisconnectException
import com.youaji.libs.tcp.server.impl.ServerOption
import java.io.InputStream
import java.io.OutputStream

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientIOManager(
    inputStream: InputStream,
    outputStream: OutputStream,
    private var serverOption: ServerOption,
    private var iClientStateSender: IStateSender
) : IIOManager<ServerOption> {
    private val iReader = ReaderImpl<ServerOption>()
    private val iWriter = WriterImpl<ServerOption>()
    private var clientReadThread: ClientReadThread<ServerOption>? = null
    private var clientWriteThread: ClientWriteThread<ServerOption>? = null

    init {
        assertHeaderProtocolNotEmpty()
        setOption(serverOption)
        iReader.initialize(inputStream, iClientStateSender)
        iWriter.initialize(outputStream, iClientStateSender)
    }

    override fun startEngine() {
        // do nothing
    }

    fun startReadEngine() {
        clientReadThread?.shutdown()
        clientReadThread = null
        clientReadThread = ClientReadThread(iReader, iClientStateSender)
        clientReadThread?.start()
    }

    fun startWriteEngine() {
        clientWriteThread?.shutdown()
        clientWriteThread = null
        clientWriteThread = ClientWriteThread(iWriter, iClientStateSender)
        clientWriteThread?.start()
    }

    private fun shutdownAllThread(e: Exception?) {
        clientReadThread?.shutdown(e)
        clientReadThread = null
        clientWriteThread?.shutdown(e)
        clientWriteThread = null
    }

    override fun setOption(option: ServerOption) {
        serverOption = option
        assertHeaderProtocolNotEmpty()
        iReader.setOption(serverOption)
        iWriter.setOption(serverOption)
    }

    override fun send(sendable: ISendable) {
        iWriter.offer(sendable)
    }

    override fun close() {
        close(InitiativeDisconnectException())
    }

    override fun close(e: Exception?) {
        shutdownAllThread(e)
    }

    private fun assertHeaderProtocolNotEmpty() {
//        val protocol = serverOption.readerProtocol ?: throw IllegalArgumentException("The reader protocol can not be Null.")
        val protocol = serverOption.readerProtocol
        require(protocol.headerLength != 0) { "The header length can not be zero." }
    }
}