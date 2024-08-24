package com.youaji.libs.tcp.core.io.interfaces

import java.io.Serializable

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IStateSender {
    fun sendBroadcast(action: String)
    fun sendBroadcast(action: String, serializable: Serializable?)
}