package com.youaji.libs.tcp.server.action

import com.youaji.libs.tcp.core.io.interfaces.IOAction

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IAction {
    interface Server {
        companion object {
            const val serverActionData = "server_action_data"
            const val actionServerListening = "action_server_listening"
            const val actionClientConnected = "action_client_connected"
            const val actionClientDisconnected = "action_client_disconnected"
            const val actionServerWillBeShutdown = "action_server_will_be_shutdown"
            const val actionServerAllReadyShutdown = "action_server_all_ready_shutdown"
        }
    }

    interface Client : IOAction {
        companion object {
            const val actionReadThreadStart = "action_read_thread_start"
            const val actionReadThreadShutdown = "action_read_thread_shutdown"
            const val actionWriteThreadStart = "action_write_thread_start"
            const val actionWriteThreadShutdown = "action_write_thread_shutdown"
        }
    }
}