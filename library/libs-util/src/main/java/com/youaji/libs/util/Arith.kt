@file:Suppress("unused")
package com.youaji.libs.util

import java.math.BigDecimal
import java.math.RoundingMode


/**
 * 提供精确的加法运算。
 * @param value 加数
 * @return 两个参数的和
 */
fun Double.add(value: Double): Double {
    val b1 = BigDecimal(this.toString())
    val b2 = BigDecimal(value.toString())
    return b1.add(b2).toDouble()
}

/**
 * 提供精确的减法运算。
 * @param value 减数
 * @return 两个参数的差
 */
fun Double.sub(value: Double): Double {
    val b1 = BigDecimal(this.toString())
    val b2 = BigDecimal(value.toString())
    return b1.subtract(b2).toDouble()
}

/**
 * 提供精确的乘法运算。
 * @param value 乘数
 * @return 两个参数的积
 */
fun Double.mul(value: Double): Double {
    val b1 = BigDecimal(this.toString())
    val b2 = BigDecimal(value.toString())
    return b1.multiply(b2).toDouble()
}

/**
 * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指定精度，以后的数字四舍五入。
 * @param value 除数
 * @param scale 表示表示需要精确到小数点以后几位。默认：10
 * @return 两个参数的商
 */
fun Double.div(value: Double, scale: Int = 10): Double {
    if (scale < 0) {
        throw IllegalArgumentException("The scale must be a positive integer or zero")
    }
    val b1 = BigDecimal(this.toString())
    val b2 = BigDecimal(value.toString())
    return b1.divide(b2, scale, RoundingMode.HALF_UP).toDouble()
}

/**
 * 提供精确的小数位四舍五入处理。
 * @param scale 小数点后保留几位
 * @return 四舍五入后的结果
 */
fun Double.round(scale: Int): Double {
    if (scale < 0) {
        throw IllegalArgumentException("The scale must be a positive integer or zero")
    }
    val b1 = BigDecimal(this.toString())
    val one = BigDecimal("1")
    return b1.divide(one, scale, RoundingMode.HALF_UP).toDouble()
}