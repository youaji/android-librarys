package com.youaji.libs.tcp.interfaces.basic

import com.youaji.libs.tcp.core.utils.SLog

/**
 * @author youaji
 * @since 2024/01/11
 */
abstract class AbsLoopThread : Runnable {

    @Volatile
    var thread: Thread? = null

    @Volatile
    var threadName = ""
        private set

    @Volatile
    var isShutdown = true
        private set

    @Volatile
    var loopTimes: Long = 0
        private set

    @Volatile
    private var isStop = true

    @Volatile
    private var ioException: Exception? = null

    constructor() {
        isStop = true
        threadName = this.javaClass.simpleName
    }

    constructor(name: String) {
        isStop = true
        threadName = name
    }

    @Synchronized
    fun start() {
        if (isStop) {
            thread = Thread(this, threadName)
            isStop = false
            loopTimes = 0
            thread?.start()
            SLog.w("$threadName is starting")
        }
    }

    override fun run() {
        try {
            isShutdown = false
            beforeLoop()
            while (!isStop) {
                runLoopThread()
                loopTimes++
            }
        } catch (e: Exception) {
            if (ioException == null) {
                ioException = e
            }
        } finally {
            isShutdown = true
            ioException?.let { e -> loopFinish(e) }
            ioException = null
            SLog.w("$threadName is shutting down")
        }
    }

    @Throws(Exception::class)
    open fun beforeLoop() {
    }

    @Throws(Exception::class)
    abstract fun runLoopThread()
    abstract fun loopFinish(e: Exception?)

    @Synchronized
    open fun shutdown() {
        if (thread != null && !isStop) {
            isStop = true
            thread?.interrupt()
            thread = null
        }
    }

    @Synchronized
    open fun shutdown(e: Exception?) {
        ioException = e
        shutdown()
    }
}