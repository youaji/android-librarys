package com.youaji.libs.util.logger.impl

import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.LogStrategy
import com.orhanobut.logger.Logger
import com.youaji.libs.util.cacheDirPath
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CSV formatted file logging for Android. Writes to CSV the following data: epoch timestamp,
 * ISO8601 timestamp (human-readable), log level, tag, log message.
 */
class CsvFormatStrategy private constructor(
    private val builder: Builder
) : FormatStrategy {

    companion object {
        private const val MAX_BYTES = 500 * 1024 // 500K averages to a 4000 lines per file
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    private val newLine: String get() = System.getProperty("line.separator") ?: ""
    private val newLineReplacement = " <br> "
    private val separator = ","

    override fun log(priority: Int, onceOnlyTag: String?, msg: String) {
        var message = msg
        val tag = formatTag(onceOnlyTag)
        builder.date?.time = System.currentTimeMillis()
        val sb = StringBuilder()

        // machine-readable date/time
        sb.append(builder.date?.time ?: "")

        // human-readable date/time
        sb.append(separator)
        builder.date?.let { sb.append(builder.dateFormat?.format(it) ?: "") }

        // level
        sb.append(separator)
        sb.append(logLevel(priority))

        // tag
        sb.append(separator)
        sb.append(tag)

        // message
        if (message.contains(newLine)) {
            // a new line would break the CSV format, so we replace it here
            message = message.replace(newLine.toRegex(), newLineReplacement)
        }
        sb.append(separator)
        sb.append(message)

        // new line
        sb.append(newLine)
        builder.logStrategy?.log(priority, tag, sb.toString())
    }

    private fun formatTag(tag: String?): String {
        return if (tag?.isNotEmpty() == true && tag != builder.tag) builder.tag + "-" + tag
        else builder.tag
    }

    private fun logLevel(value: Int): String {
        return when (value) {
            Logger.VERBOSE -> "VERBOSE"
            Logger.DEBUG -> "DEBUG"
            Logger.INFO -> "INFO"
            Logger.WARN -> "WARN"
            Logger.ERROR -> "ERROR"
            Logger.ASSERT -> "ASSERT"
            else -> "UNKNOWN"
        }
    }

    class Builder {
        var tag = "PRETTY_LOGGER"
        var date: Date? = null
        var dateFormat: SimpleDateFormat? = null
        var logStrategy: LogStrategy? = null
        fun date(d: Date?): Builder {
            date = d
            return this
        }

        fun dateFormat(sdf: SimpleDateFormat): Builder {
            dateFormat = sdf
            return this
        }

        fun logStrategy(ls: LogStrategy): Builder {
            logStrategy = ls
            return this
        }

        fun tag(tag: String): Builder {
            this.tag = tag
            return this
        }

        fun build(): CsvFormatStrategy {
            if (date == null) {
                date = Date()
            }
            if (dateFormat == null) {
                dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS", Locale.UK)
            }
            if (logStrategy == null) {
//                val diskPath = Environment.getExternalStorageDirectory().absolutePath
                val diskPath = cacheDirPath
                val folder = diskPath + File.separatorChar + "logger"
                val ht = HandlerThread("AndroidFileLogger.$folder")
                ht.start()

                val handler: Handler = DiskLogStrategy.WriteHandler(ht.looper, folder, MAX_BYTES)
                logStrategy = DiskLogStrategy(handler)
            }
            return CsvFormatStrategy(this)
        }

    }

}