package com.youaji.libs.tcp.client.impl.exception

/**
 * @author youaji
 * @since 2024/01/11
 */
class ManuallyDisconnectException : RuntimeException {
    constructor() : super()
    constructor(message: String?) : super(message)
    constructor(cause: Throwable?) : super(cause)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}