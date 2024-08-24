package com.youaji.libs.tcp.core.exceptions

/**
 * 读异常
 * @author youaji
 * @since 2024/01/11
 */
class ReadException : RuntimeException {
    constructor()
            : super()

    constructor(message: String?)
            : super(message)

    constructor(cause: Throwable?)
            : super(cause)

    constructor(message: String?, cause: Throwable?)
            : super(message, cause)

    @Suppress("NewApi")
    constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean)
            : super(message, cause, enableSuppression, writableStackTrace)
}