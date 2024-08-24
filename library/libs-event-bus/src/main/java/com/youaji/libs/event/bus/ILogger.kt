package com.youaji.libs.event.bus

sealed class LogLevel {
    object VERBOSE : LogLevel()
    object DEBUG : LogLevel()
    object INFO : LogLevel()
    object WARN : LogLevel()
    object ERROR : LogLevel()
}

interface ILogger {
    fun log(level: LogLevel, msg: String, th: Throwable? = null)
}
