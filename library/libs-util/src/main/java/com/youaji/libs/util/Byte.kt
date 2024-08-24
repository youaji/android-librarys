@file:Suppress("unused")
package com.youaji.libs.util

/**@return byte转hex*/
fun Byte.toHex(): String {
    var hex = Integer.toHexString(this.toInt() and 0xFF)
    if (hex.length < 2) {
        hex = "0$hex"
    }
    return hex
}

/**@return 获取byte数组hex值*/
fun ByteArray.getHex(): String {
    val sb = StringBuffer()
    this.forEach {
        sb.append(it.toHex() + " ")
    }
    return sb.toString()
}

/**@return 小端序(little-endian)4字节转int*/
fun ByteArray.le4toInt(): Int {
    val int1: Int = get(0).toInt() and 0xff
    val int2: Int = get(1).toInt() and 0xff shl 8
    val int3: Int = get(2).toInt() and 0xff shl 16
    val int4: Int = get(3).toInt() and 0xff shl 24
    return int1 or int2 or int3 or int4
}

/**@return 大端序(big-endian)4字节转int*/
fun ByteArray.be4toInt(): Int {
    val int1: Int = get(3).toInt() and 0xff
    val int2: Int = get(2).toInt() and 0xff shl 8
    val int3: Int = get(1).toInt() and 0xff shl 16
    val int4: Int = get(0).toInt() and 0xff shl 24
    return int1 or int2 or int3 or int4
}

/**@return 小端序(little-endian)2字节转int*/
fun ByteArray.le2toInt(): Int {
    val int1: Int = get(0).toInt() and 0xff
    val int2: Int = get(1).toInt() and 0xff shl 8
    return int1 or int2
}

/**@return 大端序(big-endian)2字节转int*/
fun ByteArray.be2toInt(): Int {
    val int1: Int = get(1).toInt() and 0xff shl 8
    val int2: Int = get(0).toInt() and 0xff
    return int1 or int2
}

/** @return int转小端序(little-endian)2字节*/
fun Int.toLE2(): ByteArray {
    val byteArray = ByteArray(2)
    byteArray[0] = (this and 0xff).toByte()
    byteArray[1] = ((this shr 8) and 0xff).toByte()
    return byteArray
}

/** @return int转大端序(big-endian)2字节*/
fun Int.toBE2(): ByteArray {
    val byteArray = ByteArray(2)
    byteArray[0] = ((this shr 8) and 0xff).toByte()
    byteArray[1] = (this and 0xff).toByte()
    return byteArray
}