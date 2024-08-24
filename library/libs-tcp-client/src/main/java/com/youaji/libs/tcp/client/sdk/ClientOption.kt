package com.youaji.libs.tcp.client.sdk

import com.youaji.libs.tcp.client.impl.action.ActionDispatcher
import com.youaji.libs.tcp.client.impl.exception.DogDeadException
import com.youaji.libs.tcp.client.sdk.connection.AbsReconnectionManager
import com.youaji.libs.tcp.client.sdk.connection.DefaultReconnectManager
import com.youaji.libs.tcp.client.sdk.connection.NoneReconnect
import com.youaji.libs.tcp.client.sdk.connection.ability.IConfiguration
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.protocol.IReaderProtocol
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.default.DefaultReaderProtocol
import java.nio.ByteOrder

/**
 * YouSocket 参数配置类
 * @author youaji
 * @since 2024/01/11
 */
class ClientOption private constructor() : ICoreOption {
    //<editor-fold desc="默认值配置">
    private var _isDebug = false
        set(value) {
            SLog.isDebug = value
            field = value
        }
    private var _maxReadDataMB = 5
    private var _readerProtocol: IReaderProtocol = DefaultReaderProtocol()
    private var _readPackageBytes = 50
    private var _writePackageBytes = 100
    private var _readByteOrder = ByteOrder.BIG_ENDIAN
    private var _writeByteOrder = ByteOrder.BIG_ENDIAN

    /** Socket 通讯模式 请注意：阻塞式仅支持冷切换(断开后切换) | 非阻塞式可以热切换 */
    var ioThreadMode: IOThreadMode = IOThreadMode.DUPLEX
        private set

    /** 重新连接管理器 */
    var reconnectionManager: AbsReconnectionManager = DefaultReconnectManager()
        private set

    /**
     * 连接是否管理保存
     * true：连接将会保存在管理器中，进行性能优化和断线重连。
     * false：不会保存在管理器中，对于已经保存的会进行删除，将不进行性能优化和断线重连。
     *
     */
    var isConnectionHolden = true
        private set

    /** 从独立线程进行回调 */
    var isCallbackInIndependentThread = true
        private set

    /** 脉搏频率单位是毫秒 */
    var pulseFrequency: Long = (5 * 1000).toLong()
        private set

    /** 脉搏丢失次数 大于或等于丢失次数时将断开该通道的连接，抛出[DogDeadException] */
    var pulseFeedLoseTimes = 5
        private set

    /** 连接超时时间(秒) */
    var connectTimeoutSecond = 3
        private set

    /** 安全套接字层配置 */
    var sslConfig: YouSocketSSLConfig? = null
        private set

    /** 套接字工厂 */
    var socketFactory: YouSocketFactory? = null
        private set

    /** 将分发放到 handler 中,外部需要传入 HandlerToken 并且调用 Handler.post(runnable); */
    var callbackThreadModeToken: ThreadModeToken? = null
        private set
    //</editor-fold>

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

    /** 线程模式 */
    enum class IOThreadMode {
        /** 单工通讯 */
        SIMPLEX,

        /** 双工通讯 */
        DUPLEX,
    }

    abstract class ThreadModeToken {
        abstract fun handleCallbackEvent(runnable: ActionDispatcher.ActionRunnable)
    }

    class Builder {
        private var socketOption: ClientOption

        constructor() {
            socketOption = ClientOption()
        }

        constructor(option: ClientOption) {
            socketOption = option
        }

        constructor(configuration: IConfiguration) {
            socketOption = configuration.option
        }

        /**
         * Socket 通讯模式
         * 请注意：阻塞式仅支持冷切换(断开后切换)；非阻塞式可以热切换。
         * @param mode [IOThreadMode]
         */
        fun setIOThreadMode(mode: IOThreadMode): Builder {
            socketOption.ioThreadMode = mode
            return this
        }

        /**
         * 最大读取数据的兆数(MB)
         * 防止服务器返回数据体过大的数据导致前端内存溢出
         * @param maxMB 兆字节为单位
         */
        fun setMaxReadDataMB(maxMB: Int): Builder {
            socketOption._maxReadDataMB = maxMB
            return this
        }

        /**
         * 安全套接字层配置
         * @param config [YouSocketSSLConfig]
         */
        fun setSSLConfig(config: YouSocketSSLConfig): Builder {
            socketOption.sslConfig = config
            return this
        }

        /**
         * Socket 通讯中，业务层定义的数据包包头格式
         * 默认为 [DefaultReaderProtocol]
         * @param protocol [IReaderProtocol] 通讯头协议
         */
        fun setReaderProtocol(protocol: IReaderProtocol): Builder {
            socketOption._readerProtocol = protocol
            return this
        }

        /**
         * 设置脉搏间隔频率，单位是毫秒
         * @param frequency 间隔毫秒数
         */
        fun setPulseFrequency(frequency: Long): Builder {
            socketOption.pulseFrequency = frequency
            return this
        }

        /**
         * 连接是否管理保存
         * true:连接将会保存在管理器中,进行性能优化和断线重连
         * false:不会保存在管理器中,对于已经保存的会进行删除,将不进行性能优化和断线重连.
         * 默认是 true
         * @param isHolden true 将此次链接进行缓存管理，false 则不进行缓存管理，
         */
        fun setConnectionHolden(isHolden: Boolean): Builder {
            socketOption.isConnectionHolden = isHolden
            return this
        }

        /**
         * 脉搏丢失次数
         * 大于或等于丢失次数时将断开该通道的连接，抛出 [DogDeadException]
         * 默认是 5 次
         * @param times 丢失心跳 ACK 的次数,例如 5,当丢失 3 次时,自动断开.
         */
        fun setPulseFeedLoseTimes(times: Int): Builder {
            socketOption.pulseFeedLoseTimes = times
            return this
        }

        /**
         * 设置输出 Socket 管道中给服务器的字节序
         * 默认是：大端字节序
         * @param order [ByteOrder] 字节序
         */
        fun setWriteByteOrder(order: ByteOrder): Builder {
            socketOption._writeByteOrder = order
            return this
        }

        /**
         * 设置输入 Socket 管道中读取时的字节序
         * 默认是：大端字节序
         * @param order [ByteOrder] 字节序
         */
        fun setReadByteOrder(order: ByteOrder): Builder {
            socketOption._readByteOrder = order
            return this
        }

        /**
         * 发送给服务器时单个数据包的总长度
         * @param bytes 单个数据包的总大小
         */
        fun setWritePackageBytes(bytes: Int): Builder {
            socketOption._writePackageBytes = bytes
            return this
        }

        /**
         * 从服务器读取时单个数据包的总长度
         * @param bytes 单个数据包的总大小
         */
        fun setReadPackageBytes(bytes: Int): Builder {
            socketOption._readPackageBytes = bytes
            return this
        }

        /**
         * 设置连接超时时间,该超时时间是链路上从开始连接到连接上的时间
         * @param second 超时秒数,注意单位是秒
         */
        fun setConnectTimeoutSecond(second: Int): Builder {
            socketOption.connectTimeoutSecond = second
            return this
        }

        /**
         * 设置断线重连的连接管理器
         * 默认的连接管理器为 [DefaultReconnectManager]
         * 如果不需要断线重连请设置该参数为 [NoneReconnect]
         *
         * @param manager 断线重连管理器[AbsReconnectionManager]
         */
        fun setReconnectionManager(manager: AbsReconnectionManager): Builder {
            socketOption.reconnectionManager = manager
            return this
        }

        /**
         * 设置 Socket 工厂类，用于提供一个可以连接的 Socket。
         * 可以是加密 Socket，也可以是未加密的 socket。
         * @param factory socket 工厂方法
         */
        fun setSocketFactory(factory: YouSocketFactory): Builder {
            socketOption.socketFactory = factory
            return this
        }

        /**
         * 设置回调在线程中,不是在UI线程中.
         * 需要自己实现 handleCallbackEvent 方法.在方法中使用 Handler.post(runnable) 进行回调
         * @param token 针对 android 设计,可以使回调在 android 的主线程中,
         */
        fun setCallbackThreadModeToken(token: ThreadModeToken): Builder {
            socketOption.callbackThreadModeToken = token
            return this
        }

        fun build(): ClientOption = socketOption
    }
}