package com.youaji.libs.event.bus

import android.app.Application
import android.util.Log

/**
 * 参考来源：https://github.com/biubiuqiu0/flow-event-bus
 */
object EventBus {
    lateinit var application: Application
    var logger: ILogger = defaultLogger
}

private val defaultLogger = object : ILogger {
    private val tag = "EventBus"
    override fun log(level: LogLevel, msg: String, th: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, msg, th)
            LogLevel.DEBUG -> Log.d(tag, msg, th)
            LogLevel.INFO -> Log.i(tag, msg, th)
            LogLevel.WARN -> Log.w(tag, msg, th)
            LogLevel.ERROR -> Log.e(tag, msg, th)
        }
    }
}