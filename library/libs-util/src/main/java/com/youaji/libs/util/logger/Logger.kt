@file:Suppress("unused")
package com.youaji.libs.util.logger

import android.util.Log
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.limitLength

/** 初始化 Logger (可选) */
fun initLogger(isLoggable: Boolean = true, printer: LoggerPrinter? = null) {
    Logger.isLoggable = { _, _ -> isLoggable }
    printer?.let { Logger.printer = it }
}

fun setLoggerOutput(output: (level: Int, tag: String, message: String, thr: Throwable?) -> Unit) {
    Logger.output = output
}

interface Logger {
    val loggerTag: String get() = TAG

    companion object {
        var isLoggable = { _: Int, _: String -> true }
        var isSaveLogFile = false
        var printer: LoggerPrinter = SimpleLoggerPrinter()
        var output: ((level: Int, tag: String, message: String, thr: Throwable?) -> Unit)? = null
    }
}

@JvmInline
value class LogLevel private constructor(val value: Int) {
    companion object {
        val VERBOSE = LogLevel(Log.VERBOSE)
        val DEBUG = LogLevel(Log.DEBUG)
        val INFO = LogLevel(Log.INFO)
        val WARN = LogLevel(Log.WARN)
        val ERROR = LogLevel(Log.ERROR)
        val ASSERT = LogLevel(Log.ASSERT)
    }
}

interface LoggerPrinter {
    fun log(level: LogLevel, tag: String, message: String, thr: Throwable?)
    fun logWtf(tag: String, message: String, thr: Throwable?)
}

/**
 * 获取对象的标签（不大于 23 个字符的类名）
 */
val Any.TAG: String get() = javaClass.simpleName.limitLength(23)

inline val TAG: String
    get() = Thread.currentThread().stackTrace
        .find { !it.isIgnorable }?.simpleClassName.orEmpty()

inline fun <reified T : Any> Logger(): Logger = object : Logger {
    override val loggerTag: String get() = T::class.java.simpleName.limitLength(23)
}

fun Logger(tag: String): Logger = object : Logger {
    override val loggerTag: String get() = tag
}

/** 打印 Verbose 等级的日志 */
fun Logger.logVerbose(message: Any?, thr: Throwable? = null) =
    log(LogLevel.VERBOSE, loggerTag, message, thr)

/** 打印 Debug 等级的日志 */
fun Logger.logDebug(message: Any?, thr: Throwable? = null) =
    log(LogLevel.DEBUG, loggerTag, message, thr)

/** 打印 Info 等级的日志 */
fun Logger.logInfo(message: Any?, thr: Throwable? = null) =
    log(LogLevel.INFO, loggerTag, message, thr)

/** 打印 Warn 等级的日志 */
fun Logger.logWarn(message: Any?, thr: Throwable? = null) =
    log(LogLevel.WARN, loggerTag, message, thr)

/** 打印 Error 等级的日志 */
fun Logger.logError(message: Any?, thr: Throwable? = null) =
    log(LogLevel.ERROR, loggerTag, message, thr)

/** 打印 Wtf 等级的日志 */
fun Logger.logWtf(message: Any?, thr: Throwable? = null) =
    logWtf(TAG, message.toString(), thr)

inline fun logVerbose(message: Any?, thr: Throwable? = null) =
    log(LogLevel.VERBOSE, TAG, message, thr)

inline fun logDebug(message: Any?, thr: Throwable? = null) =
    log(LogLevel.DEBUG, TAG, message, thr)

inline fun logInfo(message: Any?, thr: Throwable? = null) =
    log(LogLevel.INFO, TAG, message, thr)

inline fun logWarn(message: Any?, thr: Throwable? = null) =
    log(LogLevel.WARN, TAG, message, thr)

inline fun logError(message: Any?, thr: Throwable? = null) =
    log(LogLevel.ERROR, TAG, message, thr)

inline fun logWtf(message: Any?, thr: Throwable? = null) =
    logWtf(TAG, message.toString(), thr)

fun log(level: LogLevel, tag: String, message: Any?, thr: Throwable? = null) {
    if (Logger.isLoggable(level.value, tag)) {
        Logger.printer.log(level, tag, message.toString(), thr)
        Logger.output?.invoke(level.value, tag, message.toString(), thr)
    }
}

fun logWtf(tag: String, message: Any?, thr: Throwable? = null) {
    if (isAppDebug) {
        Logger.printer.logWtf(tag, message.toString(), thr)
        Logger.output?.invoke(0, tag, message.toString(), thr)
    }
}

val StackTraceElement.isIgnorable: Boolean
    get() = isNativeMethod || className == Thread::class.java.name || className == Logger::class.java.name

val StackTraceElement.simpleClassName: String?
    get() = className.split(".").run {
        if (isNotEmpty()) last().limitLength(23) else null
    }

open class SimpleLoggerPrinter : LoggerPrinter {
    override fun log(level: LogLevel, tag: String, message: String, thr: Throwable?) {
        when (level) {
            LogLevel.VERBOSE -> if (thr == null) Log.v(tag, message) else Log.v(tag, message, thr)
            LogLevel.DEBUG -> if (thr == null) Log.d(tag, message) else Log.d(tag, message, thr)
            LogLevel.INFO -> if (thr == null) Log.i(tag, message) else Log.i(tag, message, thr)
            LogLevel.WARN -> if (thr == null) Log.w(tag, message) else Log.w(tag, message, thr)
            LogLevel.ERROR -> if (thr == null) Log.e(tag, message) else Log.e(tag, message, thr)
        }
    }

    override fun logWtf(tag: String, message: String, thr: Throwable?) {
        if (thr == null) Log.wtf(tag, message) else Log.wtf(tag, message, thr)
    }
}
