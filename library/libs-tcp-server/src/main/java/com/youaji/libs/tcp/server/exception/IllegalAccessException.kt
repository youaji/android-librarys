package com.youaji.libs.tcp.server.exception

/**
 * @author youaji
 * @since 2024/01/11
 */
class IllegalAccessException : RuntimeException {
    /**
     * Constructs an `IllegalAccessException` without a detail message.
     */
    constructor() : super()

    /**
     * Constructs an `IllegalAccessException` with a detail message.
     * @param s the detail message.
     */
    constructor(s: String?) : super(s)

    companion object {
        private const val serialVersionUID = 6616958222490762034L
    }
}