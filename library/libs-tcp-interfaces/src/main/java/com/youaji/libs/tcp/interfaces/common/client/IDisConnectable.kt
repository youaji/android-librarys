package com.youaji.libs.tcp.interfaces.common.client

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IDisConnectable {

    /** 断开当前连接管理器的链接，断开回调中的断开异常将是 Null */
    fun disconnect()

    /**
     * 断开当前连接管理器的链接，并伴随着一个异常
     * @param e 断开时希望伴随的异常对象
     */
    fun disconnect(e: Exception?)

}