package com.youaji.libs.tcp.client.sdk.connection

import com.youaji.libs.tcp.client.impl.exception.ManuallyDisconnectException
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.basic.AbsLoopThread
import com.youaji.libs.tcp.interfaces.utils.ThreadUtils

/**
 * @author youaji
 * @since 2024/01/11
 */
class DefaultReconnectManager : AbsReconnectionManager() {

    /** 连接失败次数,不包括断开异常 */
    private var connectionFailedTimes = 0

    /** 最大连接失败次数，不包括断开异常 */
    private val maxConnectionFailedTimes = 12

    @Volatile
    private var reconnectTestingThread = ReconnectTestingThread()

    //<editor-fold desc="ISocketActionListener override">
    override fun onSocketDisconnection(info: ConnectionInfo, action: String, e: Exception?) {
        if (isNeedReconnect(e)) { // break with exception
            reconnectDelay()
        } else {
            resetThread()
        }
    }

    override fun onSocketConnectionSuccess(info: ConnectionInfo, action: String) {
        resetThread()
    }

    override fun onSocketConnectionFailed(info: ConnectionInfo, action: String, e: Exception?) {
        e?.let {
            connectionFailedTimes++
            if (connectionFailedTimes > maxConnectionFailedTimes) {
                resetThread()
                // 连接失败达到阈值,需要切换备用线路.
                val originInfo = iConnectionManager?.remoteConnectionInfo
                val backupInfo = originInfo?.backupInfo
                backupInfo?.let { backup ->
                    backup.backupInfo = ConnectionInfo(originInfo.ip, originInfo.port)
                    iConnectionManager?.let { manager ->
                        if (!manager.isConnect) {
                            SLog.i("Prepare switch to the backup line " + backupInfo.ip + ":" + backupInfo.port + " ...")
                            synchronized(manager) { manager.switchConnectionInfo(backupInfo) }
                            reconnectDelay()
                        }
                    }
                } ?: reconnectDelay()
            } else {
                reconnectDelay()
            }
        }
    }
    //</editor-fold>

    override fun detach() {
        super.detach()
    }

    /**
     * @param e
     * @return 是否需要重连
     */
    private fun isNeedReconnect(e: Exception?): Boolean {
        synchronized(ignoreDisconnectExceptionList) {
            if (e != null && e !is ManuallyDisconnectException) { // break with exception
                val iterator = ignoreDisconnectExceptionList.iterator()
                while (iterator.hasNext()) {
                    val classException = iterator.next()
                    if (classException.isAssignableFrom(e.javaClass)) {
                        return false
                    }
                }
                return true
            }
            return false
        }
    }

    /** 重置重连线程,关闭线程 */
    @Synchronized
    private fun resetThread() {
        reconnectTestingThread.shutdown()
    }

    /** 开始延迟重连 */
    private fun reconnectDelay() {
        synchronized(reconnectTestingThread) {
            if (reconnectTestingThread.isShutdown) {
                reconnectTestingThread.start()
            }
        }
    }

    private inner class ReconnectTestingThread : AbsLoopThread() {

        /** 延时连接时间 */
        private var reconnectTimeDelay = 10 * 1000L

        @Throws(Exception::class)
        override fun beforeLoop() {
            super.beforeLoop()
            iConnectionManager?.let { manager ->
                if (reconnectTimeDelay < manager.option.connectTimeoutSecond * 1000L) {
                    reconnectTimeDelay = manager.option.connectTimeoutSecond * 1000L
                }
            }
        }

        @Throws(Exception::class)
        override fun runLoopThread() {
            if (isDetach) {
                SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!")
                shutdown()
                return
            }

            // 延迟执行
            SLog.i("Reconnect after $reconnectTimeDelay mills ...")
            ThreadUtils.sleep(reconnectTimeDelay)

            if (isDetach) {
                SLog.i("ReconnectionManager already detached by framework.We decide gave up this reconnection mission!")
                shutdown()
                return
            }

            if (iConnectionManager?.isConnect == true) {
                shutdown()
                return
            }

            val isHolden = iConnectionManager?.option?.isConnectionHolden ?: false
            if (!isHolden) {
                detach()
                shutdown()
                return
            }

            iConnectionManager?.let { manager ->
                val info = manager.remoteConnectionInfo
                SLog.i("Reconnect the server " + info.ip + ":" + info.port + " ...")
                synchronized(manager) {
                    if (!manager.isConnect) manager.connect()
                    else shutdown()
                }
            }
        }

        override fun loopFinish(e: Exception?) {}
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return !(other == null || javaClass != other.javaClass)
    }

    override fun hashCode(): Int {
        var result = connectionFailedTimes
        result = 31 * result + maxConnectionFailedTimes
        result = 31 * result + reconnectTestingThread.hashCode()
        return result
    }
}