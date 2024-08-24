package com.youaji.libs.tcp.interfaces.utils

import com.youaji.libs.tcp.core.utils.SLog
import java.util.ServiceLoader

/**
 * @author youaji
 * @since 2024/01/11
 */
object SPIUtils {
    fun <E> load(clz: Class<E>?): E? {
        if (clz == null) {
            SLog.e("load null clz error!")
            return null
        }
        val serviceLoader = ServiceLoader.load(clz, clz.classLoader)
        val iterator = serviceLoader.iterator()
        try {
            if (iterator.hasNext()) {
                return iterator.next()
            }
        } catch (throwable: Throwable) {
            SLog.e("load " + clz.simpleName + " error! " + throwable.message)
        }
        return null
    }
}