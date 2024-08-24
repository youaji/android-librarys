@file:Suppress("unused")
package com.youaji.libs.util.logger

import android.content.Context
import androidx.startup.Initializer
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.PrettyFormatStrategy
import com.orhanobut.logger.Logger
import com.youaji.libs.util.AppInitializer
import com.youaji.libs.util.appName
import com.youaji.libs.util.isAppDebug
import com.youaji.libs.util.logger.impl.CsvFormatStrategy
import com.youaji.libs.util.logger.impl.DiskLogAdapter


class LoggerInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val prettyStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)      // (可选) 是否显示线程信息，默认为 ture
            .methodCount(0)             // (可选) 显示的方法行数，默认为 2
            .methodOffset(0)            // (可选)
            .tag(appName)                   // (可选) 每个日志的全局标记. 默认 PRETTY_LOGGER
            .build()

        val csvFormatStrategy = CsvFormatStrategy.newBuilder()
            .tag(appName)
            .build()

        Logger.addLogAdapter(object : AndroidLogAdapter(prettyStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return isAppDebug
            }
        })

        Logger.addLogAdapter(object : DiskLogAdapter(csvFormatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return com.youaji.libs.util.logger.Logger.isSaveLogFile
            }
        })

        initLogger(isAppDebug, object : LoggerPrinter {
            override fun log(level: LogLevel, tag: String, message: String, thr: Throwable?) {
                when (level) {
                    LogLevel.VERBOSE -> Logger.t(tag).v(message)
                    LogLevel.DEBUG -> Logger.t(tag).d(message)
                    LogLevel.INFO -> Logger.t(tag).i(message)
                    LogLevel.WARN -> Logger.t(tag).w(message)
                    LogLevel.ERROR -> Logger.t(tag).e(message)
                }
            }

            override fun logWtf(tag: String, message: String, thr: Throwable?) {
                Logger.t(tag).wtf(message)
            }

        })

    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return listOf(AppInitializer::class.java)
    }
}