package com.youaji.libs.tcp.client.impl

import com.youaji.libs.tcp.client.impl.ability.IConnectionSwitchListener
import com.youaji.libs.tcp.client.sdk.ClientOption
import com.youaji.libs.tcp.client.sdk.ConnectionInfo
import com.youaji.libs.tcp.client.sdk.connection.IConnectionManager
import com.youaji.libs.tcp.core.io.interfaces.ICoreOption
import com.youaji.libs.tcp.core.utils.SLog
import com.youaji.libs.tcp.interfaces.common.server.IServerManager
import com.youaji.libs.tcp.interfaces.common.server.IServerManagerPrivate
import com.youaji.libs.tcp.interfaces.utils.SPIUtils

/**
 * @author youaji
 * @since 2024/01/11
 */
class ManagerHolder private constructor() {

    @Volatile
    private var connectionManagerMap = hashMapOf<ConnectionInfo, IConnectionManager>()

    @Volatile
    private var serverManagerMap = hashMapOf<Int, IServerManagerPrivate<*>>()

    companion object {
        val get: ManagerHolder by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { ManagerHolder() }
    }

    init {
        connectionManagerMap.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <ServerOption : ICoreOption> getServer(serverPort: Int): IServerManagerPrivate<ServerOption> {
        var manager = serverManagerMap[serverPort]
        if (manager == null) {
            manager = SPIUtils.load(IServerManager::class.java) as IServerManagerPrivate<ServerOption>?
            if (manager != null) {
                synchronized(serverManagerMap) {
                    serverManagerMap.put(serverPort, manager)
                }
                manager.initServerPrivate(serverPort)
                return manager
            }
            val err = "Socket.Server() load error. Server plug-in are required!"
            SLog.e(err)
            throw IllegalStateException(err)
        }
        return manager as IServerManagerPrivate<ServerOption>
    }

    fun getConnection(info: ConnectionInfo): IConnectionManager {
        return getConnection(info, connectionManagerMap[info]?.option ?: ClientOption.Builder().build())
    }

    fun getConnection(info: ConnectionInfo, option: ClientOption): IConnectionManager {
        val manager: IConnectionManager? = connectionManagerMap[info]
        return if (manager != null) {
            if (!option.isConnectionHolden) {
                synchronized(connectionManagerMap) { connectionManagerMap.remove(info) }
                return createNewManagerAndCache(info, option)
            } else {
                manager.option(option)
            }
            manager
        } else {
            createNewManagerAndCache(info, option)
        }
    }

    fun getConnections(): List<IConnectionManager?> {
        val list = mutableListOf<IConnectionManager?>()
        val map = HashMap<ConnectionInfo, IConnectionManager>(connectionManagerMap)
        val iterator = map.keys.iterator()
        while (iterator.hasNext()) {
            val info = iterator.next()
            val manager = map[info]
            val isHolden = manager?.option?.isConnectionHolden ?: false
            if (!isHolden) {
                iterator.remove()
                continue
            }
            list.add(manager)
        }
        return list
    }

    private fun createNewManagerAndCache(info: ConnectionInfo, okOptions: ClientOption): IConnectionManager {
        val manager = ConnectionManagerImpl(info)
        manager.option(okOptions)
        manager.setOnConnectionSwitchListener(object : IConnectionSwitchListener {
            override fun onSwitchConnectionInfo(manager: IConnectionManager, oldInfo: ConnectionInfo, newInfo: ConnectionInfo) {
                synchronized(connectionManagerMap) {
                    connectionManagerMap.remove(oldInfo)
                    connectionManagerMap.put(newInfo, manager)
                }
            }
        })
        synchronized(connectionManagerMap) { connectionManagerMap.put(info, manager) }
        return manager
    }
}