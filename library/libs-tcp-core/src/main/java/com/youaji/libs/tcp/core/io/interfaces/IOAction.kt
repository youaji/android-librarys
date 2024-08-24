package com.youaji.libs.tcp.core.io.interfaces

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IOAction {
    companion object {
        /** 收到推送消息响应 */
        const val actionReadComplete = "action_read_complete"

        /** 写给服务器响应 */
        const val actionWriteComplete = "action_write_complete"

        /** 发送心跳请求 */
        const val actionPulseRequest = "action_pulse_request"
    }
}