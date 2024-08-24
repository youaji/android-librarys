package com.youaji.libs.tcp.client.impl

import com.youaji.libs.tcp.client.impl.exception.DogDeadException
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.IPulse
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author youaji
 * @since 2024/01/11
 */
class PulseManager internal constructor(
    manager: IConnectionManager,
    option: ClientOption,
) : IPulse {
    /** 数据包发送器 */
    @Volatile
    private var iConnectionManager: IConnectionManager?

    /** 连接参数 */
    @Volatile
    private var socketOption: ClientOption

    /** 当前频率 */
    @Volatile
    private var currentFrequency: Long = 0

    /** 当前的线程模式 */
    @Volatile
    private var currentThreadMode: ClientOption.IOThreadMode

    /** 是否死掉 */
    @Volatile
    private var isDead = false

    /** 允许遗漏的次数 */
    @Volatile
    private var _loseTimes = AtomicInteger(-1)

    /** 心跳数据包 */
    private var iPulseSendable: IPulseSendable? = null
    private val pulseThread = PulseThread()

    init {
        iConnectionManager = manager
        socketOption = option
        currentThreadMode = socketOption.ioThreadMode
    }

    val pulseSendable: IPulseSendable?
        get() = iPulseSendable

    @Synchronized
    fun setPulseSendable(sendable: IPulseSendable?): IPulse {
        if (sendable != null) {
            iPulseSendable = sendable
        }
        return this
    }

    @Synchronized
    override fun pulse() {
        privateDead()
        updateFrequency()
        if (currentThreadMode !== ClientOption.IOThreadMode.SIMPLEX) {
            if (pulseThread.isShutdown) {
                pulseThread.start()
            }
        }
    }

    @Synchronized
    override fun trigger() {
        if (isDead) {
            return
        }
        if (currentThreadMode != ClientOption.IOThreadMode.SIMPLEX && iConnectionManager != null) {
            iPulseSendable?.let { iConnectionManager?.send(it) }
        }
    }

    @Synchronized
    override fun dead() {
        _loseTimes.set(0)
        isDead = true
        privateDead()
    }

    @Synchronized
    private fun updateFrequency() {
        if (currentThreadMode !== ClientOption.IOThreadMode.SIMPLEX) {
            currentFrequency = socketOption.pulseFrequency
            currentFrequency =
                if (currentFrequency < 1000) 1000
                else currentFrequency // 间隔最小为一秒
        } else {
            privateDead()
        }
    }

    @Synchronized
    override fun feed() {
        _loseTimes.set(-1)
    }

    private fun privateDead() {
        pulseThread.shutdown()
    }

    val loseTimes: Int
        get() = _loseTimes.get()

    @Synchronized
    fun setOption(option: ClientOption) {
        socketOption = option
        currentThreadMode = socketOption.ioThreadMode
        updateFrequency()
    }

    private inner class PulseThread : AbsLoopThread() {
        @Throws(Exception::class)
        override fun runLoopThread() {
            if (isDead) {
                shutdown()
                return
            }
            if (iConnectionManager != null) {
                iPulseSendable?.let {
                    if (socketOption.pulseFeedLoseTimes != -1 && _loseTimes.incrementAndGet() >= socketOption.pulseFeedLoseTimes) {
                        iConnectionManager?.disconnect(DogDeadException("you need feed dog on time,otherwise he will die"))
                    } else {
                        iConnectionManager?.send(it)
                    }
                }
            }

            // not safety sleep.
            Thread.sleep(currentFrequency)
        }

        override fun loopFinish(e: Exception?) {}
    }
}