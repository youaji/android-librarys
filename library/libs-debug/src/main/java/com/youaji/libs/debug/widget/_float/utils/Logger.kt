package com.youaji.libs.debug.widget._float.utils

import android.util.Log

/**
 * @author youaji
 * @since 2024/01/05
 */
internal object Logger {

    private var tag = "EasyFloat--->"

    // 设为false关闭日志
    private var logEnable = true

    fun i(msg: Any) = i(tag, msg.toString())

    fun v(msg: Any) = v(tag, msg.toString())

    fun d(msg: Any) = d(tag, msg.toString())

    fun w(msg: Any) = w(tag, msg.toString())

    fun e(msg: Any) = e(tag, msg.toString())

    fun i(tag: String, msg: String) {
        if (logEnable) Log.i(tag, msg)
    }

    fun v(tag: String, msg: String) {
        if (logEnable) Log.v(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (logEnable) Log.d(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (logEnable) Log.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        if (logEnable) Log.e(tag, msg)
    }

}