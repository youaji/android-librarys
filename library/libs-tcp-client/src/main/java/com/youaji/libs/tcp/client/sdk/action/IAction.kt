package com.youaji.libs.tcp.client.sdk.action

import com.youaji.libs.tcp.core.io.interfaces.IOAction

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IAction : IOAction {
    companion object {
        //数据key
        const val actionData = "action_data"

        //socket读线程启动响应
        const val actionReadThreadStart = "action_read_thread_start"

        //socket读线程关闭响应
        const val actionReadThreadShutdown = "action_read_thread_shutdown"

        //socket写线程启动响应
        const val actionWriteThreadStart = "action_write_thread_start"

        //socket写线程关闭响应
        const val actionWriteThreadShutdown = "action_write_thread_shutdown"

        //socket连接服务器成功响应
        const val actionConnectionSuccess = "action_connection_success"

        //socket连接服务器失败响应
        const val actionConnectionFailed = "action_connection_failed"

        //socket与服务器断开连接
        const val actionDisconnection = "action_disconnection"
    }
}