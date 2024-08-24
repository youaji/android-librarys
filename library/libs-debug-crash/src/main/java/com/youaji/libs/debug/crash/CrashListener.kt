package com.youaji.libs.debug.crash

/**
 * @author youaji
 * @since 2024/01/05
 */
interface CrashListener {
    /**
     * 重启app
     */
    fun againStartApplication()

    /**
     * 自定义上传 crash
     * @param throwable    Throwable
     */
    fun recordedException(throwable: Throwable)
}