package com.youaji.libs.tcp.core.utils

/**
 * @author youaji
 * @since 2024/01/11
 */
object BytesUtils {
    /**
     * 生成打印16进制日志所需的字符串
     *
     * @param data 数据源
     * @return 字符串给日志使用
     */
    fun toHexStringForLog(data: ByteArray?): String {
        val sb = StringBuilder()
        if (data != null) {
            for (i in data.indices) {
                var tempHexStr = Integer.toHexString(data[i].toInt() and 0xff) + " "
                tempHexStr = if (tempHexStr.length == 2) "0$tempHexStr" else tempHexStr
                sb.append(tempHexStr)
            }
        }
        return sb.toString()
    }
}