package com.youaji.libs.tcp.core.utils

/**
 * @author youaji
 * @since 2024/01/11
 */
object SLog {
    var isDebug = false

    fun e(msg: String) {
        if (isDebug) {
            System.err.println("YouTCP, $msg")
        }
    }

    fun i(msg: String) {
        if (isDebug) {
            println("YouTCP, $msg")
        }
    }

    fun w(msg: String) {
        i(msg)
    }
}