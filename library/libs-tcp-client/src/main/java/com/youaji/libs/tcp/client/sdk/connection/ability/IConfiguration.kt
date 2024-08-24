package com.youaji.libs.tcp.client.sdk.connection.ability

import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager

/**
 * @author youaji
 * @since 2024/01/11
 */
interface IConfiguration {
    /**
     * 获得当前连接管理器的参数配置
     *
     * @return 参数配置
     */
    val option: ClientOption

    /**
     * 修改参数配置
     *
     * @param option 新的参数配置
     * @return 当前的链接管理器
     */
    fun option(option: ClientOption): IConnectionManager
}