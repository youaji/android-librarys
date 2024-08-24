package com.youaji.libs.tcp.interfaces.utils

/**
 * @author youaji
 * @since 2024/01/11
 */
object ThreadUtils {
    fun sleep(mills: Long) {
        var m = mills
        var weakTime: Long = 0
        var startTime: Long = 0
        while (true) {
            try {
                m =
                    if (weakTime - startTime < m) m - (weakTime - startTime)
                    else break
                startTime = System.currentTimeMillis()
                Thread.sleep(m)
                weakTime = System.currentTimeMillis()
            } catch (e: InterruptedException) {
                weakTime = System.currentTimeMillis()
            }
        }
    }
}