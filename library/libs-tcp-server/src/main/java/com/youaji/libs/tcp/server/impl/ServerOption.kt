package com.youaji.libs.tcp.server.impl

import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.interfaces.default.DefaultReaderProtocol
import java.nio.ByteOrder

/**
 * @author youaji
 * @since 2024/01/11
 */
class ServerOption private constructor() : ICoreOption {
    //<editor-fold desc="默认值配置">
    private var _isDebug = false
    private var _maxReadDataMB = 10
    private var _readerProtocol: IReaderProtocol = DefaultReaderProtocol()
    private var _readPackageBytes = 50
    private var _writePackageBytes = 100
    private var _readByteOrder = ByteOrder.BIG_ENDIAN
    private var _writeByteOrder = ByteOrder.BIG_ENDIAN
    //</editor-fold>

    /** 服务器连接能力数 */
    var connectCapacity = 50
        private set

    fun setDebug(debug: Boolean) {
        this._isDebug = debug
    }

    //<editor-fold desc="IIOCoreOption override">
    override val isDebug: Boolean
        get() = _isDebug
    override val maxReadDataMB
        get() = _maxReadDataMB
    override val readerProtocol: IReaderProtocol
        get() = _readerProtocol
    override val readPackageBytes
        get() = _readPackageBytes
    override val writePackageBytes
        get() = _writePackageBytes
    override val readByteOrder: ByteOrder
        get() = _readByteOrder
    override val writeByteOrder: ByteOrder
        get() = _writeByteOrder
    //</editor-fold>

    class Builder {
        private var serverOption: ServerOption

        constructor() {
            serverOption = ServerOption()
        }

        constructor(option: ServerOption) {
            val clone = ServerOption()
            clone.connectCapacity = option.connectCapacity
            clone._maxReadDataMB = option._maxReadDataMB
            clone._readerProtocol = option._readerProtocol
            clone._readPackageBytes = option._readPackageBytes
            clone._writePackageBytes = option._writePackageBytes
            clone._readByteOrder = option._readByteOrder
            clone._writeByteOrder = option._writeByteOrder
            serverOption = clone
        }

        fun setConnectCapacity(capacity: Int): Builder {
            serverOption.connectCapacity = capacity
            return this
        }

        fun setMaxReadDataMB(maxMB: Int): Builder {
            serverOption._maxReadDataMB = maxMB
            return this
        }

        fun setReaderProtocol(protocol: IReaderProtocol): Builder {
            serverOption._readerProtocol = protocol
            return this
        }

        fun setReadPackageBytes(packageBytes: Int): Builder {
            serverOption._readPackageBytes = packageBytes
            return this
        }

        fun setWritePackageBytes(packageBytes: Int): Builder {
            serverOption._writePackageBytes = packageBytes
            return this
        }

        fun setReadOrder(order: ByteOrder?): Builder {
            serverOption._readByteOrder = order
            return this
        }

        fun setWriteOrder(order: ByteOrder?): Builder {
            serverOption._writeByteOrder = order
            return this
        }

        fun build(): ServerOption {
            return serverOption
        }
    }
}