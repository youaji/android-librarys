package com.youaji.libs.tcp.client.impl.thread

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.core.io.ReaderImpl
import com.youaji.libs.tcp.core.io.WriterImpl
import com.youaji.libs.tcp.core.io.interfaces.IReader
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.io.interfaces.IWriter
import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.interfaces.common.IIOManager
import java.io.InputStream
import java.io.OutputStream

/**
 * @author youaji
 * @since 2024/01/11
 */
class IOThreadManager(
    inputStream: InputStream,
    outputStream: OutputStream,
    option: ClientOption,
    iStateSender: IStateSender
) : IIOManager<ClientOption> {
    @Volatile
    private var clientOption: ClientOption
    private val iStateSender: IStateSender
    private var iReader: IReader<ClientOption>
    private var iWriter: IWriter<ClientOption>
    private var simplexThread: AbsLoopThread? = null
    private var duplexReadThread: DuplexReadThread<ClientOption>? = null
    private var duplexWriteThread: DuplexWriteThread<ClientOption>? = null
    private var currentThreadMode: ClientOption.IOThreadMode? = null

    init {
        this.clientOption = option
        this.iStateSender = iStateSender
        assertHeaderProtocolNotEmpty()
        this.iReader = ReaderImpl()
        this.iReader.initialize(inputStream, this.iStateSender)
        this.iWriter = WriterImpl()
        this.iWriter.initialize(outputStream, this.iStateSender)
    }


    @Synchronized
    override fun startEngine() {
        currentThreadMode = clientOption.ioThreadMode
        // 初始化读写工具类
        iReader.setOption(clientOption)
        iWriter.setOption(clientOption)
        when (clientOption.ioThreadMode) {
            ClientOption.IOThreadMode.DUPLEX -> {
                SLog.w("DUPLEX is processing")
                duplex()
            }

            ClientOption.IOThreadMode.SIMPLEX -> {
                SLog.w("SIMPLEX is processing")
                simplex()
            }

//            else -> throw RuntimeException("未定义的线程模式")
        }
    }

    private fun duplex() {
        shutdownAllThread(null)
        duplexWriteThread = DuplexWriteThread(iWriter, iStateSender)
        duplexReadThread = DuplexReadThread(iReader, iStateSender)
        duplexWriteThread?.start()
        duplexReadThread?.start()
    }

    private fun simplex() {
        shutdownAllThread(null)
        simplexThread = SimplexIOThread(iReader, iWriter, iStateSender)
        simplexThread?.start()
    }

    private fun shutdownAllThread(e: Exception?) {
        simplexThread?.shutdown(e)
        simplexThread = null
        duplexReadThread?.shutdown(e)
        duplexReadThread = null
        duplexWriteThread?.shutdown(e)
        duplexWriteThread = null
    }

    @Synchronized
    override fun setOption(option: ClientOption) {
        clientOption = option
        if (currentThreadMode == null) {
            currentThreadMode = clientOption.ioThreadMode
        }
        assertTheThreadModeNotChanged()
        assertHeaderProtocolNotEmpty()
        iWriter.setOption(clientOption)
        iReader.setOption(clientOption)
    }

    override fun send(sendable: ISendable) {
        iWriter.offer(sendable)
    }

    override fun close() {
        close(ManuallyDisconnectException())
    }

    @Synchronized
    override fun close(e: Exception?) {
        shutdownAllThread(e)
        currentThreadMode = null
    }

    private fun assertHeaderProtocolNotEmpty() {
//        val protocol: IReaderProtocol = socketOption.readerProtocol ?: throw IllegalArgumentException("The reader protocol can not be Null.")
        val protocol: IReaderProtocol = clientOption.readerProtocol
        require(protocol.headerLength != 0) { "The header length can not be zero." }
    }

    private fun assertTheThreadModeNotChanged() {
        require(clientOption.ioThreadMode == currentThreadMode) {
            ("can't hot change iothread mode from $currentThreadMode to ${clientOption.ioThreadMode} in blocking io manager")
        }
    }
}