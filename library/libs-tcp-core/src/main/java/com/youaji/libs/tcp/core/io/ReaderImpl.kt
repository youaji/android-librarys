package com.youaji.libs.tcp.core.io

import com.youaji.libs.tcp.core.exceptions.ReadException
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.io.interfaces.IOAction
import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.core.utils.BytesUtils
import com.youaji.libs.tcp.core.utils.SLog
import java.io.IOException
import java.nio.ByteBuffer

/**
 * @author youaji
 * @since 2024/01/11
 */
class ReaderImpl<Option : ICoreOption> : AbsReader<Option>() {

    private var remainingBuf: ByteBuffer? = null

    @Throws(RuntimeException::class)
    override fun read() {
        option?.let { options ->
            val originalData = OriginalData()
            val headerProtocol = options.readerProtocol
            val headerLength = headerProtocol.headerLength
            val headBuf = ByteBuffer.allocate(headerLength)
            headBuf.order(options.readByteOrder)
            try {
                remainingBuf?.let { rb ->
                    rb.flip()
                    val length = rb.remaining().coerceAtMost(headerLength)
                    headBuf.put(rb.array(), 0, length)
                    if (length < headerLength) {
                        // there are no data left
                        remainingBuf = null
                        readHeaderFromChannel(headBuf, headerLength - length)
                    } else {
                        rb.position(headerLength)
                    }
                } ?: readHeaderFromChannel(headBuf, headBuf.capacity())

                originalData.headBytes = headBuf.array()
                if (SLog.isDebug) {
                    SLog.i("read head: " + BytesUtils.toHexStringForLog(headBuf.array()))
                }

                val bodyLength = headerProtocol.getBodyLength(originalData.headBytes, options.readByteOrder)
                if (SLog.isDebug) {
                    SLog.i("need read body length: $bodyLength")
                }

                if (bodyLength > 0) {
                    if (bodyLength > options.maxReadDataMB * 1024 * 1024) {
                        throw ReadException(
                            """Need to follow the transmission protocol.
Please check the client/server code.
According to the packet header data in the transport protocol, the package length is $bodyLength Bytes.
You need check your <ReaderProtocol> definition"""
                        )
                    }

                    val byteBuffer = ByteBuffer.allocate(bodyLength)
                    byteBuffer.order(options.readByteOrder)
                    remainingBuf?.let { rb ->
                        val bodyStartPosition = rb.position()
                        val length = rb.remaining().coerceAtMost(bodyLength)
                        byteBuffer.put(rb.array(), bodyStartPosition, length)
                        rb.position(bodyStartPosition + length)
                        if (length == bodyLength) {
                            remainingBuf = if (rb.remaining() > 0) {
                                // there are data left
                                val temp = ByteBuffer.allocate(rb.remaining())
                                temp.order(options.readByteOrder)
                                temp.put(rb.array(), rb.position(), rb.remaining())
                                temp
                            } else {
                                // there are no data left
                                null
                            }
                            // cause this time data from remaining buffer not from channel.
                            originalData.bodyBytes = byteBuffer.array()
                            iStateSender?.sendBroadcast(IOAction.actionReadComplete, originalData)
                            return
                        } else {
                            // there are no data left in buffer and some data pieces in channel
                            remainingBuf = null
                        }
                    }
                    readBodyFromChannel(byteBuffer)
                    originalData.bodyBytes = byteBuffer.array()
                } else if (bodyLength == 0) {
                    originalData.bodyBytes = ByteArray(0)
                    remainingBuf?.let { rb ->
                        // the body is empty so header remaining buf need set null
                        remainingBuf = if (rb.hasRemaining()) {
                            val temp = ByteBuffer.allocate(rb.remaining())
                            temp.order(options.readByteOrder)
                            temp.put(rb.array(), rb.position(), rb.remaining())
                            temp
                        } else {
                            null
                        }
                    }
                } else {
                    //  bodyLength < 0
                    throw ReadException("read body is wrong,this socket input stream is end of file read $bodyLength ,that mean this socket is disconnected by server")
                }
                iStateSender?.sendBroadcast(IOAction.actionReadComplete, originalData)
            } catch (e: Exception) {
                throw ReadException(e)
            }
        }
    }

    @Throws(IOException::class)
    private fun readHeaderFromChannel(headBuf: ByteBuffer, readLength: Int) {
        for (i in 0 until readLength) {
            val bytes = ByteArray(1)
            val value = inputStream?.read(bytes)
            if (value == -1) {
                throw ReadException("read head is wrong, this socket input stream is end of file read $value ,that mean this socket is disconnected by server")
            }
            headBuf.put(bytes)
        }
    }

    @Throws(IOException::class)
    private fun readBodyFromChannel(byteBuffer: ByteBuffer) {
        while (byteBuffer.hasRemaining()) {
            try {
                option?.let { options ->
                    val bufArray = ByteArray(options.readPackageBytes)
                    val len = inputStream?.read(bufArray) ?: -1
                    if (len != -1) {
                        val remaining = byteBuffer.remaining()
                        if (len > remaining) {
                            byteBuffer.put(bufArray, 0, remaining)
                            remainingBuf = ByteBuffer.allocate(len - remaining)
                            remainingBuf?.order(options.readByteOrder)
                            remainingBuf?.put(bufArray, remaining, len - remaining)
                        } else {
                            byteBuffer.put(bufArray, 0, len)
                        }
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }
        if (SLog.isDebug) {
            SLog.i("read total bytes: " + BytesUtils.toHexStringForLog(byteBuffer.array()))
            SLog.i("read total length:" + (byteBuffer.capacity() - byteBuffer.remaining()))
        }
    }
}