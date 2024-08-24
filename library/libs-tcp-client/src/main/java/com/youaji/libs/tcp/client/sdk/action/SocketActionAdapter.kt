package com.youaji.libs.tcp.client.sdk.action

import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.core.io.interfaces.IPulseSendable
import com.youaji.libs.tcp.core.io.interfaces.ISendable
import com.youaji.libs.tcp.core.pojo.OriginalData

/**
 * Socket行为适配器,是行为监听器的一个 Simple 版本,详情请见[ISocketActionListener]
 * @author youaji
 * @since 2024/01/11
 */
abstract class SocketActionAdapter : ISocketActionListener {
    /**
     * Socket通讯IO线程的启动
     * 该方法调用后IO线程将会正常工作
     * 例如InputStream线程启动后,讲回调此方法,如果OutPutStream线程启动,也会回调此方法.
     * 一次成功的双工通讯建立,会调用此方法两次.
     *
     * @param action [IAction.actionReadThreadStart]
     * [IAction.actionWriteThreadStart]
     */
    override fun onSocketIOThreadStart(action: String) {}

    /**
     * Socket通讯IO线程的关闭
     * 该方法调用后IO线程将彻底死亡
     * 例如InputStream线程销毁后,讲回调此方法,如果OutPutStream线程销毁,也会回调此方法.
     * 一次成功的双工通讯销毁,会调用此方法两次.
     *
     * @param action [IAction.actionReadThreadShutdown]
     * [IAction.actionWriteThreadShutdown]
     * @param e      线程关闭所遇到的异常信息,正常断开也可能会有异常信息.
     */
    override fun onSocketIOThreadShutdown(action: String, e: Exception?) {}

    /**
     * Socket断开后进行的回调
     * 当Socket彻底断开后,系统会回调该方法
     *
     * @param info   这次连接的连接信息
     * @param action [IAction.actionDisconnection]
     * @param e      Socket断开时的异常信息,如果是正常断开(调用disconnect()),异常信息将为null.使用e变量时应该进行判空操作
     */
    override fun onSocketDisconnection(info: ConnectionInfo, action: String, e: Exception?) {}

    /**
     * 当Socket连接建立成功后
     * 系统会回调该方法,此时有可能读写线程还未启动完成,不过不会影响大碍
     * 当回调此方法后,我们可以认为Socket连接已经建立完成,并且读写线程也初始化完
     *
     * @param info   这次连接的连接信息
     * @param action [IAction.actionConnectionSuccess]
     */
    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: String) {}

    /**
     * 当Socket连接失败时会进行回调
     * 建立Socket连接,如果服务器出现故障,网络出现异常都将导致该方法被回调
     * 系统回调此方法时,IO线程均未启动.如果IO线程启动将会回调[.onSocketDisconnection]
     *
     * @param info   这次连接的连接信息
     * @param action [IAction.actionConnectionFailed]
     * @param e      连接未成功建立的错误原因
     */
    override fun onSocketConnectionFailed(info: ConnectionInfo, action: String, e: Exception?) {}

    /**
     * Socket通讯从服务器读取到消息后的响应
     *
     * @param action [IAction.ACTION_READ_COMPLETE]
     * @param data   原始的读取到的数据[OriginalData]
     */
    override fun onSocketReadResponse(info: ConnectionInfo, action: String, data: OriginalData) {}

    /**
     * Socket通讯写出后的响应回调
     *
     * @param action [IAction.ACTION_WRITE_COMPLETE]
     * @param data   写出的数据[ISendable]
     */
    override fun onSocketWriteResponse(info: ConnectionInfo, action: String, data: ISendable) {}

    /**
     * Socket心跳发送后的回调
     * 心跳发送是一个很特殊的写操作
     * 该心跳发送后将不会回调[.onSocketWriteResponse]方法
     *
     * @param info 这次连接的连接信息
     * @param data 心跳发送数据[IPulseSendable]
     */
    override fun onPulseSend(info: ConnectionInfo, data: IPulseSendable) {}
}