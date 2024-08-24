package com.youaji.libs.tcp.server.action

import com.youaji.libs.tcp.core.io.interfaces.IOAction.Companion.actionReadComplete
import com.youaji.libs.tcp.core.io.interfaces.IOAction.Companion.actionWriteComplete
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.io.interfaces.IStateSender
import com.youaji.libs.tcp.core.pojo.OriginalData
import com.youaji.libs.tcp.server.action.IAction.Client.Companion.actionReadThreadShutdown
import com.youaji.libs.tcp.server.action.IAction.Client.Companion.actionReadThreadStart
import com.youaji.libs.tcp.server.action.IAction.Client.Companion.actionWriteThreadShutdown
import com.youaji.libs.tcp.server.action.IAction.Client.Companion.actionWriteThreadStart
import java.io.Serializable

/**
 * @author youaji
 * @since 2024/01/11
 */
class ClientActionDispatcher(
    private val actionListener: ClientActionListener? = null
) : IStateSender {
    override fun sendBroadcast(action: String, serializable: Serializable?) {
        if (actionListener == null) return
        dispatch(action, serializable)
    }

    override fun sendBroadcast(action: String) {
        sendBroadcast(action, null)
    }

    private fun dispatch(action: String, serializable: Serializable?) {
        when (action) {
            actionReadThreadStart -> {
                try {
                    actionListener?.onClientReadReady()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionReadThreadShutdown -> {
                try {
                    actionListener?.onClientReadDead(serializable as Exception?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionWriteThreadStart -> {
                try {
                    actionListener?.onClientWriteReady()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionWriteThreadShutdown -> {
                try {
                    actionListener?.onClientWriteDead(serializable as Exception?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionReadComplete -> {
                try {
                    actionListener?.onClientRead(serializable as OriginalData?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            actionWriteComplete -> {
                try {
                    actionListener?.onClientWrite(serializable as ISendable?)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    interface ClientActionListener {
        fun onClientReadReady()
        fun onClientWriteReady()
        fun onClientReadDead(e: Exception?)
        fun onClientWriteDead(e: Exception?)
        fun onClientRead(originalData: OriginalData?)
        fun onClientWrite(sendable: ISendable?)
    }
}