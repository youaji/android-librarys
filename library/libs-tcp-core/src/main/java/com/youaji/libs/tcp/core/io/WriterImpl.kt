package com.youaji.libs.tcp.core.io

import com.youaji.libs.tcp.core.exceptions.WriteException
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IOAction
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.io.interfaces.IWriter
import com.youaji.libs.tcp.core.utils.BytesUtils
import com.youaji.libs.tcp.core.utils.SLog
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

/**
 * @author youaji
 * @since 2024/01/11
 */
class WriterImpl<Option : ICoreOption> : IWriter<Option> {

    @Volatile
    private var option: Option? = null
    private var iStateSender: IStateSender? = null
    private var outputStream: OutputStream? = null
    private val queue: LinkedBlockingQueue<ISendable> = LinkedBlockingQueue<ISendable>()
    override fun initialize(outputStream: OutputStream, stateSender: IStateSender) {
        this.iStateSender = stateSender
        this.outputStream = outputStream
    }

    override fun setOption(option: Option) {
        this.option = option
    }

    override fun offer(sendable: ISendable?) {
        queue.offer(sendable)
    }

    @Throws(RuntimeException::class)
    override fun write(): Boolean {
        var sendable: ISendable? = null
        try {
            sendable = queue.take()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        sendable?.let { s ->
            try {
                option?.let { options ->
                    val sendBytes = s.parse()
                    val packageSize: Int = options.writePackageBytes
                    var remainingCount = sendBytes.size
                    val writeBuf = ByteBuffer.allocate(packageSize)
                    writeBuf.order(options.writeByteOrder)
                    var index = 0
                    while (remainingCount > 0) {
                        val realWriteLength = packageSize.coerceAtMost(remainingCount)
                        writeBuf.clear()
                        writeBuf.rewind()
                        writeBuf.put(sendBytes, index, realWriteLength)
                        writeBuf.flip()
                        val writeArr = ByteArray(realWriteLength)
                        writeBuf[writeArr]
                        outputStream?.write(writeArr)
                        outputStream?.flush()
                        if (SLog.isDebug) {
                            val forLogBytes = sendBytes.copyOfRange(index, index + realWriteLength)
                            SLog.i("write bytes: " + BytesUtils.toHexStringForLog(forLogBytes))
                            SLog.i("bytes write length:$realWriteLength")
                        }
                        index += realWriteLength
                        remainingCount -= realWriteLength
                    }
                    if (sendable is IPulseSendable) {
                        iStateSender?.sendBroadcast(IOAction.actionPulseRequest, sendable)
                    } else {
                        iStateSender?.sendBroadcast(IOAction.actionWriteComplete, sendable)
                    }
                } ?: return false
            } catch (e: Exception) {
                throw WriteException(e)
            }
            return true
        }
        return false
    }

    override fun close() {
        try {
            outputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}